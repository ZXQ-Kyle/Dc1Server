package space.ponyo.dc1.server.model;

import org.springframework.lang.NonNull;
import space.ponyo.dc1.server.bean.StatusBean;
import space.ponyo.dc1.server.bean.SwitchSetBean;
import space.ponyo.dc1.server.model.db.Dc1Bean;
import space.ponyo.dc1.server.model.db.Dc1Dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataPool {
    public static void init() {
        //设备数据缓存在内存中，定期更新到本地数据库
        List<Dc1Bean> list = Dc1Dao.getInstance().queryAll();
        list.forEach(bean -> {
            bean.setOnline(false);
            dc1Map.put(bean.getId(), bean);
        });
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleWithFixedDelay(() -> {
            if (dc1Map.isEmpty()) {
                return;
            }
            Dc1Dao.getInstance().updateAll(dc1Map.values());
        }, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * key dc1的id
     * value dc1
     * 数据库缓存，每分钟写入数据库
     */
    public static ConcurrentHashMap<String, Dc1Bean> dc1Map = new ConcurrentHashMap<>();

    /**
     * 更新设备电流电压功率和开关状态
     *
     * @param id
     * @param statusBean
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean update(String id, StatusBean statusBean) {
        boolean result;
        Dc1Bean dc1Bean = dc1Map.get(id);

        String newStatus = statusBean.getStatus();
        int newP = statusBean.getP();

        if (dc1Bean == null) {
            //原来无数据，需要推送，类型为强制更新
            result = true;
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        } else {
            //原来有数据，进行比较
            String oriStatus = dc1Bean.getStatus();
            int oriP = dc1Bean.getP();
            result = !oriStatus.equals(newStatus) || (oriP - newP) > 10;
        }
        dc1Bean.setStatus(newStatus)
                .setI(statusBean.getI())
                .setV(statusBean.getV())
                .setP(newP)
                .setUpdateTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 更新开关状态
     *
     * @param id
     * @param switchSetBean
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean update(String id, SwitchSetBean switchSetBean) {
        boolean result;
        Dc1Bean dc1Bean = dc1Map.get(id);
        String newStatus = switchSetBean.getStatus();
        if (dc1Bean == null) {
            //原来无数据，需要推送，类型为强制更新
            result = true;
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        } else {
            //原来有数据，进行比较
            String oriStatus = dc1Bean.getStatus();
            result = !oriStatus.equals(newStatus);
        }
        dc1Bean.setStatus(newStatus)
                .setUpdateTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 更新开关命名
     *
     * @param id
     * @param nameList
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean updateName(String id, @NonNull ArrayList<String> nameList) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            boolean equals = dc1Bean.getNames().toString().equals(nameList.toString());
            if (!equals) {
                dc1Bean.setNames(nameList);
                return true;
            }
        }
        return false;
    }

    /**
     * 更新用电量
     *
     * @param id
     * @param deltaKWh
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean update(String id, int deltaKWh) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            if (dc1Bean.getPowerStartTime() == 0) {
                dc1Bean.setPowerStartTime(System.currentTimeMillis());
                dc1Bean.setTotalPower(0);
            } else {
                dc1Bean.setTotalPower(dc1Bean.getTotalPower() + deltaKWh);
                return true;
            }
        }
        return false;
    }

    /**
     * 重置用电量
     *
     * @param id
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean resetPower(String id) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            dc1Bean.setTotalPower(0);
            dc1Bean.setPowerStartTime(0);
            return true;
        }
        return false;
    }

    /**
     * 设备上线
     *
     * @param id
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean online(String id) {
        if (id == null) {
            return false;
        }
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null && !dc1Bean.isOnline()) {
            dc1Bean.setOnline(true);
            return true;
        }
        return false;
    }

    /**
     * @param id
     * @return 需要推送更新，返回true，否则false
     */
    public static boolean offline(String id) {
        if (id == null) {
            return false;
        }
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null && dc1Bean.isOnline()) {
            dc1Bean.setOnline(false);
            return true;
        }
        return false;
    }
}
