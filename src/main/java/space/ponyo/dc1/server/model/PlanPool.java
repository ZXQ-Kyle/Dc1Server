package space.ponyo.dc1.server.model;


import space.ponyo.dc1.server.model.db.PlanBean;
import space.ponyo.dc1.server.model.db.PlanDao;
import space.ponyo.dc1.server.server.ConnectionManager;
import space.ponyo.dc1.server.util.LogUtil;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlanPool {

    private static PlanPool instance = new PlanPool();

    /**
     * 一天的秒
     */
    private static final long ONE_DAY_SECOND = 24 * 60 * 60;

    public static PlanPool getInstance() {
        return instance;
    }

    /**
     * KEY : planId<br/>
     * VALUE : ScheduledFuture
     */
    private ConcurrentHashMap<String, ScheduledFuture> mTaskMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(5);

    public void init() {
        mExecutorService.execute(() -> {
            List<PlanBean> list = PlanDao.getInstance().queryAll();
            list.forEach(this::convert);
        });
    }

    /**
     * 将任务转换成计划任务
     *
     * @param bean
     */
    private void convert(PlanBean bean) {
        if (bean == null) {
            return;
        }
        if (!bean.isEnable()) {
            return;
        }
        //自定义周期时间任务单独逻辑
        if (PlanBean.REPEAT_AT_FIXED_RATE.equals(bean.getRepeat())) {
            repeatAtFixedRate(bean);
            return;
        }
        long diffSecond = getDiffSecond(bean);
        if (!bean.isEnable()) {
            return;
        }
        ScheduledFuture<?> future = mExecutorService.schedule(() -> {
            mTaskMap.remove(bean.getId());
            ConnectionManager.getInstance().setDc1Status(bean.getDeviceId(), calcStatus(bean, bean.getCommand()));
            if (PlanBean.REPEAT_ONCE.equals(bean.getRepeat())) {
                bean.setEnable(false);
                PlanDao.getInstance().updateOne(bean);
            }
            //执行完成后添加下一次触发的任务
            convert(bean);
        }, diffSecond, TimeUnit.SECONDS);
        mTaskMap.put(bean.getId(), future);
    }

    /**
     * 自定义周期时间任务单独逻辑
     *
     * @param planBean
     * @return
     */
    private void repeatAtFixedRate(PlanBean planBean) {
        ScheduledFuture start = mTaskMap.get(planBean.getId() + PlanBean.TASK_SUFFIX_START);
        if (start != null) {
            start.cancel(true);
        }
        ScheduledFuture stop = mTaskMap.get(planBean.getId() + PlanBean.TASK_SUFFIX_STOP);
        if (stop != null) {
            stop.cancel(true);
        }
        String repeatData = planBean.getRepeatData();
        if (repeatData == null) {
            LogUtil.warning("周期设置异常！ repeatData=null");
            setPlanDisable(planBean);
            return;
        }
        String[] split = repeatData.split(",");
        if (split.length != 2) {
            LogUtil.warning("周期设置异常！ repeatData格式错误");
            setPlanDisable(planBean);
            return;
        }
        //存储的数据:"30,5"(每30分钟执行一次，每次开启5分钟)
        long period = Integer.parseInt(split[0]) * 60L;
        long workTime = Integer.parseInt(split[1]) * 60L;

        ScheduledFuture<?> startFuture = mExecutorService
                .scheduleAtFixedRate(() -> repeatRunnable(planBean, "1"), 0, period, TimeUnit.SECONDS);
        mTaskMap.put(planBean.getId() + PlanBean.TASK_SUFFIX_START, startFuture);

        ScheduledFuture<?> stopFuture = mExecutorService
                .scheduleAtFixedRate(() -> repeatRunnable(planBean, "0"), workTime, period, TimeUnit.SECONDS);
        mTaskMap.put(planBean.getId() + PlanBean.TASK_SUFFIX_STOP, stopFuture);
    }

    private void repeatRunnable(PlanBean planBean, String command) {
        String status = calcStatus(planBean, command);
        ConnectionManager.getInstance().setDc1Status(planBean.getDeviceId(), status);
        LogUtil.info("repeatAtFixedRate 发送指令：" + status);
    }

    private String calcStatus(PlanBean planBean, String command) {
        String[] status = DataPool.dc1Map.get(planBean.getDeviceId()).getStatus().split("");
        String switchIndex = planBean.getSwitchIndex();
        switch (switchIndex) {
            case PlanBean.SWITCH_INDEX_MAIN:
                status[0] = command;
                break;
            case PlanBean.SWITCH_INDEX_FIRST:
                status[1] = command;
                break;
            case PlanBean.SWITCH_INDEX_SECOND:
                status[2] = command;
                break;
            case PlanBean.SWITCH_INDEX_THIRD:
                status[3] = command;
                break;
            default:
                break;
        }
        if ("1".equals(command)) {
            status[0] = "1";
        }
        StringBuilder sb = new StringBuilder();
        Stream.of(status).forEachOrdered(sb::append);
        return sb.toString();
    }

    /**
     * @param bean
     * @return 计算下一次执行时间差，单位：秒
     */
    private long getDiffSecond(PlanBean bean) {
        long diffSecond;

        //当天或者第二天执行
        LocalTime triggerTime = LocalTime.parse(bean.getTriggerTime());
        LocalTime now = LocalTime.now();
        final long diff = Duration.between(now, triggerTime).toMillis() / 1000;

        switch (bean.getRepeat()) {
            case PlanBean.REPEAT_ONCE:
            case PlanBean.REPEAT_EVERYDAY: {
                if (diff > 1) {
                    diffSecond = diff;
                } else {
                    diffSecond = ONE_DAY_SECOND + diff;
                }
                LogUtil.info("下次运行时间 :" + diff / 3600f + "小时");
                return diffSecond;
            }

            case PlanBean.REPEAT_AT_FIXED_RATE: {
                //按时间的周期性执行
                LogUtil.warning("周期设置异常！ 不是周期任务执行入口");
                setPlanDisable(bean);
                return 0;
            }

            default: {
                if (bean.getRepeat() == null || bean.getRepeat().equals("")) {
                    LogUtil.warning("周期设置异常！！");
                    setPlanDisable(bean);
                    return 0;
                }
                //选星期执行
                String repeat = bean.getRepeat();
                int[] array = Arrays.stream(repeat.split(","))
                        .mapToInt(value -> {
                            try {
                                return Integer.parseInt(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return 0;
                            }
                        })
                        .filter(integer -> integer > 0)
                        .toArray();
                if (array.length == 0) {
                    setPlanDisable(bean);
                    return 0;
                }
                if (array.length == 7) {
                    bean.setRepeat(PlanBean.REPEAT_EVERYDAY);
                    PlanDao.getInstance().updateOne(bean);
                    if (diff > 1) {
                        diffSecond = diff;
                    } else {
                        diffSecond = ONE_DAY_SECOND + diff;
                    }
                    return diffSecond;
                }

                LocalDateTime localDateTime = LocalDateTime.now();
                int dayOfWeekNow = localDateTime.getDayOfWeek().getValue();

                IntPredicate predicate;
                if (diff > 1) {
                    //当天可执行
                    predicate = i -> i >= dayOfWeekNow;
                } else {
                    predicate = i -> i > dayOfWeekNow;
                }
                Integer triggerDay = IntStream.of(array)
                        .filter(predicate)
                        .findFirst()
                        .orElseGet(() -> array[0]);

                int i = triggerDay - dayOfWeekNow;
                if (diff <= 1 && i <= 0) {
                    i = i + 7;
                }
                diffSecond = i * ONE_DAY_SECOND + diff;
                LogUtil.info("下次运行时间 i:" + i + "天  偏移小时数:" + diff / 3600f);
                return diffSecond;
            }
        }
    }

    /**
     * 异常状态纠正
     *
     * @param bean
     */
    private void setPlanDisable(PlanBean bean) {
        bean.setEnable(false)
                .setRepeat(PlanBean.REPEAT_ONCE);
        PlanDao.getInstance().updateOne(bean);
    }

    private boolean canclePlan(String planId) {
        //自定义周期时间任务单独逻辑
        ScheduledFuture startFuture = mTaskMap.get(planId + PlanBean.TASK_SUFFIX_START);
        ScheduledFuture stopFuture = mTaskMap.get(planId + PlanBean.TASK_SUFFIX_STOP);
        if (startFuture != null) {
            startFuture.cancel(true);
            mTaskMap.remove(planId + PlanBean.TASK_SUFFIX_START);
        }
        if (stopFuture != null) {
            stopFuture.cancel(true);
            mTaskMap.remove(planId + PlanBean.TASK_SUFFIX_STOP);
            return startFuture != null && startFuture.isCancelled() && stopFuture.isCancelled();
        }

        ScheduledFuture future = mTaskMap.get(planId);
        if (future == null || future.isCancelled()) {
            return true;
        }
        future.cancel(true);
        mTaskMap.remove(planId);
        return future.isCancelled();
    }

    public boolean addPlan(PlanBean plan) {
        boolean add = PlanDao.getInstance().add(plan);
        mExecutorService.execute(() -> {
            if (add) {
                convert(plan);
            }
        });
        return add;
    }

    public boolean deletePlan(String planId) {
        canclePlan(planId);
        try {
            PlanDao.getInstance().deleteById(planId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean enablePlan(String id, boolean enable) {
        PlanBean planBean = PlanDao.getInstance().enablePlanById(id, enable);
        mExecutorService.execute(() -> {
            if (enable) {
                convert(planBean);
            } else {
                canclePlan(id);
            }
        });
        return planBean != null;
    }
}
