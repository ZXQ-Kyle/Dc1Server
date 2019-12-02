package space.ponyo.dc1.server.bean;

/**
 * 设置开关状态
 */
public class SwitchSetBean {

    /**
     * "status" : 1011
     */
    private int status;

    public String getStatus() {
        return StatusBean.parseToPhoneStatus(status);
    }

    public SwitchSetBean setStatus(String status) {
        this.status = StatusBean.parseToDc1Status(status);
        return this;
    }
}
