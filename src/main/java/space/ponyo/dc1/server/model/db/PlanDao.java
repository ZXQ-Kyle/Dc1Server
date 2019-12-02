package space.ponyo.dc1.server.model.db;


import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlanDao extends BaseDaoImpl<PlanBean, String> {

    private static PlanDao instance;

    public static PlanDao getInstance() {
        return instance;
    }

    static void init(ConnectionSource connectionSource) throws SQLException {
        instance = new PlanDao(connectionSource, PlanBean.class);
    }

    private PlanDao(ConnectionSource connectionSource, Class<PlanBean> dataClass) throws SQLException {
        super(connectionSource, dataClass);
        // create table
        TableUtils.createTableIfNotExists(connectionSource, PlanBean.class);
    }

    /**
     * @return 按Id排序
     */
    public List<PlanBean> queryAll() {
        try {
            return queryBuilder()
                    .orderBy(PlanBean.ATTR_TRIGGER_TIME, true)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    public void updateAll(Collection<PlanBean> values) {
        values.forEach(bean -> {
            try {
                createOrUpdate(bean);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void clearAndAdd(List<PlanBean> list) {
        try {
            delete(queryForAll());
            create(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean add(PlanBean plan) {
        try {
            int i = create(plan);
            return i == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PlanBean enablePlanById(String id, boolean enable) {
        try {
            PlanBean planBean = queryForId(id);
            planBean.setEnable(enable);
            update(planBean);
            return planBean;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public PlanBean queryById(String id) {
        try {
            return queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PlanBean> queryAllByDeviceId(String deviceId) {
        try {
            return queryBuilder()
                    .orderBy(PlanBean.ATTR_TRIGGER_TIME, true)
                    .where().eq(PlanBean.ATTR_DEVICE_ID, deviceId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateOne(PlanBean bean){
        try {
            return update(bean);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
