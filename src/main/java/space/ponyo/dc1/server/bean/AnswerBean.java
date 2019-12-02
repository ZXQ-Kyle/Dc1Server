package space.ponyo.dc1.server.bean;

public class AnswerBean<T> {

    /**
     * uuid : 111111
     * status : 200
     * result : {"status":1011,"I":119,"V":219,"P":10}
     * msg : get datapoint success
     */

    private String uuid;
    private int status;
    private T result;
    private String msg;

    public String getUuid() {
        return uuid;
    }

    public AnswerBean<T> setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public AnswerBean<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public T getResult() {
        return result;
    }

    public AnswerBean<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public AnswerBean<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}
