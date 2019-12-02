package space.ponyo.dc1.server.model.db;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

@DatabaseTable(tableName = "Dc1")
public class Dc1Bean {
    public static final String ATTR_ID = "id";

    /**
     * mac或者uuid
     */
    @DatabaseField(id = true, canBeNull = false, unique = true)
    private String id;
    /**
     * 实际状态
     */
    @DatabaseField
    private String status;

    @DatabaseField
    private int I;

    @DatabaseField
    private int V;

    @DatabaseField
    private int P;

    @DatabaseField
    private long updateTime;

    private boolean online;

    /**
     * 开始计算用电量的起始时间
     */
    @DatabaseField
    private long powerStartTime;
    @DatabaseField
    private long totalPower;

    /**
     * 插排名称，1-4开关名称
     */
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<String> names;



    public String getId() {
        return id;
    }

    public Dc1Bean setId(String mac) {
        this.id = mac;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Dc1Bean setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getI() {
        return I;
    }

    public Dc1Bean setI(int i) {
        I = i;
        return this;
    }

    public int getV() {
        return V;
    }

    public Dc1Bean setV(int v) {
        V = v;
        return this;
    }

    public int getP() {
        return P;
    }

    public Dc1Bean setP(int p) {
        P = p;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public Dc1Bean setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public Dc1Bean setNames(ArrayList<String> names) {
        this.names = names;
        return this;
    }

    public boolean isOnline() {
        return online;
    }

    public Dc1Bean setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public long getPowerStartTime() {
        return powerStartTime;
    }

    public Dc1Bean setPowerStartTime(long powerStartTime) {
        this.powerStartTime = powerStartTime;
        return this;
    }

    public long getTotalPower() {
        return totalPower;
    }

    public Dc1Bean setTotalPower(long totalPower) {
        this.totalPower = totalPower;
        return this;
    }

    @Override
    public String toString() {
        return "Dc1Bean{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", I=" + I +
                ", V=" + V +
                ", P=" + P +
                ", updateTime=" + updateTime +
                ", online=" + online +
                ", powerStartTime=" + powerStartTime +
                ", totalPower=" + totalPower +
                ", names=" + names +
                '}';
    }
}
