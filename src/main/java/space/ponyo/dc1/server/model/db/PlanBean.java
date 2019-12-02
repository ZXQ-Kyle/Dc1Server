package space.ponyo.dc1.server.model.db;


import com.j256.ormlite.field.DatabaseField;

public class PlanBean {
    public static final String ATTR_EXPIRE = "expire";
    public static final String ATTR_TRIGGER_TIME = "triggerTime";
    public static final String ATTR_DEVICE_ID = "deviceId";

    public static final String REPEAT_ONCE = "repeat_once";
    public static final String REPEAT_EVERYDAY = "repeat_everyday";
    public static final String REPEAT_AT_FIXED_RATE = "repeat_at_fixed_rate";

    public static final String DAY_MONDAY = "1";
    public static final String DAY_TUESDAY = "2";
    public static final String DAY_WEDNESDAY = "3";
    public static final String DAY_THURSDAY = "4";
    public static final String DAY_FRIDAY = "5";
    public static final String DAY_SATURDAY = "6";
    public static final String DAY_SUNDAY = "7";

    public static final String SWITCH_INDEX_MAIN = "0";
    public static final String SWITCH_INDEX_FIRST = "1";
    public static final String SWITCH_INDEX_SECOND = "2";
    public static final String SWITCH_INDEX_THIRD = "3";

    /**
     * 周期任务id的后缀
     */
    public static final String TASK_SUFFIX_START = "start";
    public static final String TASK_SUFFIX_STOP = "stop";


    /**
     * uuid
     */
    @DatabaseField(id = true, canBeNull = false, unique = true)
    private String id;

    /**
     * 触发的设备Id
     */
    @DatabaseField(canBeNull = false)
    private String deviceId;

    /**
     * 触发的设备名称
     */
    @DatabaseField
    private String deviceName;

    /**
     * 任务最新修改或添加时间
     */
    @DatabaseField
    private long updateTime;

    /**
     * 设备开关指令(例如：1101)
     */
    @Deprecated
    @DatabaseField
    private String status;

    /**
     * 设备单控指令，例如：1开，0关
     */
    @DatabaseField
    private String command;

    /**
     * 设备单控指令:控制开关对象
     *
     * @see #SWITCH_INDEX_MAIN
     * @see #SWITCH_INDEX_FIRST
     * @see #SWITCH_INDEX_SECOND
     * @see #SWITCH_INDEX_THIRD
     */
    @DatabaseField
    private String switchIndex;

    /**
     * 触发时间,
     * 格式 05:43:22
     */
    @DatabaseField
    private String triggerTime;

    /**
     * 重复状态:一次/每天/星期自定义[1,2,3,4]/定时周期性重复
     */
    @DatabaseField
    private String repeat;

    /**
     * 定时周期性重复时，存储的数据:"30,5"(每30分钟执行一次，每次5分钟)
     */
    @DatabaseField
    private String repeatData;

    /**
     * 是否开启,开启/关闭控制
     */
    @DatabaseField
    private boolean enable;


    public String getId() {
        return id;
    }

    public PlanBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public PlanBean setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public PlanBean setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public PlanBean setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PlanBean setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTriggerTime() {
        return triggerTime;
    }

    public PlanBean setTriggerTime(String triggerTime) {
        this.triggerTime = triggerTime;
        return this;
    }

    public String getRepeat() {
        return repeat;
    }


    public PlanBean setRepeat(String repeat) {
        this.repeat = repeat;
        return this;
    }

    public boolean isEnable() {
        return enable;
    }

    public PlanBean setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public String getRepeatData() {
        return repeatData;
    }

    public PlanBean setRepeatData(String repeatData) {
        this.repeatData = repeatData;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public PlanBean setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getSwitchIndex() {
        return switchIndex;
    }

    public PlanBean setSwitchIndex(String switchIndex) {
        this.switchIndex = switchIndex;
        return this;
    }
}
