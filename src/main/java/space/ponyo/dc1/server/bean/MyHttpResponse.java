package space.ponyo.dc1.server.bean;

import com.google.gson.Gson;

public class MyHttpResponse<T> {
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_FAILED = 403;
    private static final Gson gson = new Gson();

    public static String error(String message) {
        return gson.toJson(new MyHttpResponse<String>()
                .setCode(MyHttpResponse.CODE_FAILED)
                .setData(null)
                .setMessage(message));
    }

    public static <T> String success(T data) {
        return gson.toJson(new MyHttpResponse<T>()
                .setCode(MyHttpResponse.CODE_SUCCESS)
                .setData(data)
                .setMessage("请求成功"));
    }

    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public MyHttpResponse<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MyHttpResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public MyHttpResponse<T> setData(T data) {
        this.data = data;
        return this;
    }
}
