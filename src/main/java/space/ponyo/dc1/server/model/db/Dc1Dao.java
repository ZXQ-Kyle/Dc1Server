package space.ponyo.dc1.server.model.db;


import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Dc1Dao extends BaseDaoImpl<Dc1Bean, String> {

    private static Dc1Dao instance;

    public static Dc1Dao getInstance() {
        return instance;
    }

    static void init(ConnectionSource connectionSource) throws SQLException {
        instance = new Dc1Dao(connectionSource, Dc1Bean.class);
    }

    private Dc1Dao(ConnectionSource connectionSource, Class<Dc1Bean> dataClass) throws SQLException {
        super(connectionSource, dataClass);
        // create table
        TableUtils.createTableIfNotExists(connectionSource, Dc1Bean.class);
    }

    /**
     * @return 按Id排序
     */
    public List<Dc1Bean> queryAll() {
        try {
            return queryBuilder().orderBy(Dc1Bean.ATTR_ID, true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    public void updateAll(Collection<Dc1Bean> values) {
        values.forEach(bean -> {
            try {
                createOrUpdate(bean);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void clearAndAdd(List<Dc1Bean> list) {
        try {
            delete(queryForAll());
            create(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
