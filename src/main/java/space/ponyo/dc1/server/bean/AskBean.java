package space.ponyo.dc1.server.bean;

public class AskBean<T> {

    /**
     * action : activate=
     * uuid : activate=386
     * auth :
     * params : {"device_type":"PLUG_DC1_7","mac":"84:F3:EB:56:38:21"}
     */

    private String action;
    private String uuid;
    private String auth;
    private T params;

    public String getAction() {
        return action;
    }

    public AskBean setAction(String action) {
        this.action = action;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public AskBean setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getAuth() {
        return auth;
    }

    public AskBean setAuth(String auth) {
        this.auth = auth;
        return this;
    }

    public T getParams() {
        return params;
    }

    public AskBean setParams(T params) {
        this.params = params;
        return this;
    }
}
