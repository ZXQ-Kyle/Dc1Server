package space.ponyo.dc1.server.model;

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
        List<Dc1Bean> list = Dc1Dao.getInstance().queryAll();
        list.forEach(bean -> dc1Map.put(bean.getId(), bean));
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleWithFixedDelay(() -> {
            if (dc1Map.isEmpty()) {
                return;
            }
            Dc1Dao.getInstance().updateAll(dc1Map.values());
        }, 60, 60, TimeUnit.SECONDS);
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
     */
    public static void update(String id, StatusBean statusBean) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        }
        dc1Bean.setStatus(statusBean.getStatus())
                .setI(statusBean.getI())
                .setV(statusBean.getV())
                .setP(statusBean.getP())
                .setUpdateTime(System.currentTimeMillis());
    }

    /**
     * 更新开关状态
     *
     * @param id
     * @param switchSetBean
     */
    public static void update(String id, SwitchSetBean switchSetBean) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        }
        dc1Bean.setStatus(switchSetBean.getStatus())
                .setUpdateTime(System.currentTimeMillis());
    }

    /**
     * 更新开关命名
     *
     * @param id
     * @param nameList
     */
    public static void updateName(String id, ArrayList<String> nameList) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            dc1Bean.setNames(nameList);
        }
    }

    /**
     * 更新用电量
     *
     * @param id
     * @param detalKWh
     */
    public static void update(String id, int detalKWh) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            if (dc1Bean.getPowerStartTime() == 0) {
                dc1Bean.setPowerStartTime(System.currentTimeMillis());
                dc1Bean.setTotalPower(0);
            } else {
                dc1Bean.setTotalPower(dc1Bean.getTotalPower() + detalKWh);
            }
        }
    }

    /**
     * 重置用电量
     *
     * @param id
     */
    public static void resetPower(String id) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            dc1Bean.setTotalPower(0);
            dc1Bean.setPowerStartTime(0);
        }
    }

    /**
     * 设备上线
     *
     * @param id
     */
    public static void online(String id) {
        if (id == null) {
            return;
        }
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            dc1Bean.setOnline(true);
        }
    }

    public static void offline(String id) {
        if (id == null) {
            return;
        }
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean != null) {
            dc1Bean.setOnline(false);
        }
    }
}
