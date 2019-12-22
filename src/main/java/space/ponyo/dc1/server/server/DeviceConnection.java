package space.ponyo.dc1.server.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import space.ponyo.dc1.server.bean.*;
import space.ponyo.dc1.server.model.DataPool;
import space.ponyo.dc1.server.util.LogUtil;

import java.lang.reflect.Type;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * channel的管理
 */

public class DeviceConnection implements IConnection {
    /**
     * 设备上线1
     */
    public static final String ACTIVATE = "activate=";
    /**
     * 设备上线2
     */
    public static final String IDENTIFY = "identify";
    /**
     * 每增加50kwh，自动上报
     */
    public static final String DETAL_KWH = "kWh+";
    /**
     * 查询设备状态
     */
    public static final String DATAPOINT = "datapoint";
    /**
     * 设置设备开关
     */
    public static final String SET_DATAPOINT = "datapoint=";

    public static final int CODE_SUCCESS = 200;

    //周期消息发送间隔时间（ms）
    private final static int DEFAULT_TIME = 100;

    private Channel channel;
    /**
     * dc1的mac，唯一标识
     */
    private String id;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    /**
     * 临时状态，用于判断设备掉线
     */
    private FixedList online = new FixedList(5);
    /**
     * 查询状态正则
     */
    private Pattern pattern = Pattern.compile("\\{\"uuid\":\"\\w{0,14}\",\"status\":\\d{1,3},\"result\":\\{\"status\":[0|1]{1,4},\"I\":\\d{1,5},\"V\":\\d{1,3},\"P\":\\d{1,4}},\"msg\":\".+\"}[\r|\n]{0,2}");
    private Pattern patternTwo = Pattern.compile("\\{\"uuid\":\"\\w{0,14}\",\"status\":\\d{1,3},\"result\":\\{\"status\":[0|1]{1,4},\"I\":\\d{1,5},\"V\":\\d{1,3},\"P\":\\d{1,4}},\"msg\":\".+[\r|\n]{0,2}");
    private Pattern patternThree = Pattern.compile("\\{\"status\":[0|1]{1,4},\"I\":\\d{1,5},\"V\":\\d{1,3},\"P\":\\d{1,4}}");
    //TODO 查询逻辑合并
    /**
     * 设置开关回复，
     */
    private Pattern statusPattern = Pattern.compile("\\{\"uuid\":\"\\w{0,14}\",\"status\":\\d{1,3},\"result\":\\{\"status\":[0|1]{1,4}},\"msg\":\".+\"}[\r|\n]{0,2}");
    // 消息队列
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static ScheduledExecutorService sendMessageScheduleThread;

    public DeviceConnection() {
        sendMessageScheduleThread = Executors.newScheduledThreadPool(5);
        sendMessageScheduleThread.scheduleWithFixedDelay(new SendTask(), 0, DEFAULT_TIME, TimeUnit.MILLISECONDS);
    }

    public void setChannel(Channel channel) {
        close();
        this.channel = channel;
    }


    private void appendMsgToQueue(String msg) {
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processMessage(String msg) {
        LogUtil.info("device|receive id=" + id + " message=" + msg);
        if (msg.contains("action")) {
            if (msg.contains(ACTIVATE)) {
                //收到dc1上线数据
                Type type = new TypeToken<AskBean<ActivateBean>>() {
                }.getType();
                AskBean<ActivateBean> askBean = gson.fromJson(msg, type);
                id = askBean.getParams().getMac();
                sendMessageScheduleThread.scheduleWithFixedDelay(new QueryTask(), 0, 1, TimeUnit.MINUTES);
            } else if (msg.contains(IDENTIFY)) {
                //收到dc1上线数据 第二种数据格式
                Type type = new TypeToken<AskBean<IdentifyBean>>() {
                }.getType();
                AskBean<IdentifyBean> askBean = gson.fromJson(msg, type);
                id = askBean.getParams().getDeviceId();
                AnswerBean<Object> answerBean = new AnswerBean<>();
                answerBean.setUuid(askBean.getUuid())
                        .setResult(new Object())
                        .setStatus(200)
                        .setMsg("device identified");
                appendMsgToQueue(gson.toJson(answerBean));
                sendMessageScheduleThread.scheduleWithFixedDelay(new QueryTask(), 0, 1, TimeUnit.MINUTES);
            } else if (msg.contains(DETAL_KWH)) {
                //收到用电量增加
                Type type = new TypeToken<AskBean<DetalKwhBean>>() {
                }.getType();
                AskBean<DetalKwhBean> askBean = gson.fromJson(msg, type);
                int detalKWh = askBean.getParams().getDetalKWh();
                DataPool.update(id, detalKWh);
                ConnectionManager.getInstance().refreshPhoneDeviceData();
            } else {
                LogUtil.warning(msg);
            }
        } else {
            if (id == null) {
                return;
            }
            if (pattern.matcher(msg).matches()) {
                Type type = new TypeToken<AnswerBean<StatusBean>>() {
                }.getType();
                AnswerBean<StatusBean> answerBean = gson.fromJson(msg, type);
                if (answerBean.getStatus() == CODE_SUCCESS) {
                    DataPool.update(id, answerBean.getResult());
                }
            } else if (statusPattern.matcher(msg).matches()) {
                Type type = new TypeToken<AnswerBean<SwitchSetBean>>() {
                }.getType();
                AnswerBean<SwitchSetBean> answerBean = gson.fromJson(msg, type);
                if (answerBean.getStatus() == CODE_SUCCESS) {
                    DataPool.update(id, answerBean.getResult());
                }
            } else if (patternTwo.matcher(msg).matches()) {
                Matcher matcher = patternThree.matcher(msg);
                while (matcher.find()) {
                    msg = matcher.group(0);
                    StatusBean statusBean = gson.fromJson(msg, StatusBean.class);
                    DataPool.update(id, statusBean);
                    break;
                }
            } else {
                LogUtil.warning(msg);
            }
        }
        online.add(true);
        DataPool.online(id);
        ConnectionManager.getInstance().refreshPhoneDeviceData();
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            if (isActive()) {
                try {
                    //阻塞线程
                    String message = messageQueue.take();
                    LogUtil.info("device|send id=" + id + " message=" + message);
                    channel.writeAndFlush(message + "\n");
                } catch (InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 定时任务 发送请求dc1的状态，把请求数据添加到任务队列
    private class QueryTask implements Runnable {
        @Override
        public void run() {
            if (online.isAllFalse()) {
                DataPool.offline(id);
            }
            AskBean<String> askBean = new AskBean<>();
            String uuid = String.format("T%d", System.currentTimeMillis());
            askBean.setAction(DATAPOINT)
                    .setAuth("")
                    .setParams("")
                    .setUuid(uuid);
            String msg = gson.toJson(askBean);
            appendMsgToQueue(msg);
        }
    }

    public void setStatus(String status) {
        AskBean<SwitchSetBean> askBean = new AskBean<>();
        String uuid = String.format("T%d", System.currentTimeMillis());
        askBean.setAction(SET_DATAPOINT)
                .setAuth("")
                .setParams(new SwitchSetBean().setStatus(status))
                .setUuid(uuid);
        String msg = gson.toJson(askBean);
        appendMsgToQueue(msg);
    }

    @Override
    public String toString() {
        return this.hashCode() + channel.toString();
    }

    public String getId() {
        return id;
    }
}
