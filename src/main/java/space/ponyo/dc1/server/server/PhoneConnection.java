package space.ponyo.dc1.server.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import space.ponyo.dc1.server.model.DataPool;
import space.ponyo.dc1.server.model.PlanPool;
import space.ponyo.dc1.server.model.db.PlanBean;
import space.ponyo.dc1.server.model.db.PlanDao;
import space.ponyo.dc1.server.util.LogUtil;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * channel的管理
 */

public class PhoneConnection implements IConnection {

    //周期消息发送间隔时间（ms）
    private final static int DEFAULT_TIME = 100;
    private Channel channel;

    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    // 消息队列
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static ScheduledExecutorService sendMessageScheduleThread;

    private Pattern setPattern = Pattern.compile("^id=(?<id>[A-Fa-f0-9:|\\-]{17,18}) status=(?<status>[0|1]{4})$");
    private Pattern changeNamePattern = Pattern.compile("^id=(?<id>[A-Fa-f0-9:|\\-]{17,18}) names=(?<names>.+)$");
    private Pattern resetPowerPattern = Pattern.compile("^id=(?<id>[A-Fa-f0-9:|\\-]{17,18})$");

    public PhoneConnection() {
        sendMessageScheduleThread = Executors.newScheduledThreadPool(5);
        sendMessageScheduleThread.scheduleWithFixedDelay(new SendTask(), 0, DEFAULT_TIME, TimeUnit.MILLISECONDS);
        sendMessageScheduleThread.scheduleWithFixedDelay(new QueryTask(), 0, 2, TimeUnit.MINUTES);
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

    /**
     * 推送消息
     *
     * @param msg
     */
    public void pushMessage(String msg) {
        appendMsgToQueue(msg);
    }

    /**
     * 收到手机端消息，处理逻辑
     *
     * @param msg
     */
    public void processMessage(String msg) {
        LogUtil.info("phone|receive id=" + channel.id() + " message=" + msg);
        msg = msg.replace("\n", "");
        final String[] split = msg.split(" ", 3);
        String action = split[0];
        if (action == null || "".equals(action) || split.length < 2) {
            return;
        }
        String token = split[1];
        if ("".equals(token) || !ConnectionManager.getInstance().token.equals(token)) {
            appendMsgToQueue("tip token验证失败！");
            LogUtil.warning("tip token验证失败！");
            return;
        }
        switch (action) {
            //查询
            case "queryDevice": {
                appendMsgToQueue("queryDevice " + gson.toJson(DataPool.dc1Map.values()));
                break;
            }
            case "queryPlan": {
                String deviceId = split[2];
                sendMessageScheduleThread.execute(() -> {
                    appendMsgToQueue("queryPlan " + gson.toJson(PlanDao.getInstance().queryAllByDeviceId(deviceId)));
                });
                break;
            }
            //设置
            case "set": {
                Matcher matcher = setPattern.matcher(split[2]);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    String status = matcher.group("status");
                    ConnectionManager.getInstance().setDc1Status(id, status);
                }
                break;
            }
            //改名字
            case "changeName": {
                Matcher matcher = changeNamePattern.matcher(split[2]);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    String names = matcher.group("names");
                    ArrayList<String> nameList = gson.fromJson(names, new TypeToken<ArrayList<String>>() {
                    }.getType());
                    DataPool.updateName(id, nameList);
                    ConnectionManager.getInstance().refreshPhoneDeviceData();
                }
                break;
            }
            //重置电量
            case "resetPower": {
                Matcher matcher = resetPowerPattern.matcher(split[2]);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    DataPool.resetPower(id);
                    ConnectionManager.getInstance().refreshPhoneDeviceData();
                }
                break;
            }
            case "addPlan": {
                String json = split[2];
                PlanBean plan = gson.fromJson(json, PlanBean.class);
                PlanPool.getInstance().addPlan(plan);
                break;
            }
            case "deletePlan": {
                String id = split[2];
                PlanPool.getInstance().deletePlan(id);
                break;
            }
            case "enablePlanById": {
                String body = split[2];
                String[] strs = body.split(" ", 2);
                boolean enable = Boolean.parseBoolean(strs[1]);
                PlanPool.getInstance().enablePlan(strs[0], enable);
                break;
            }
        }
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
                    LogUtil.info("phone|send id=" + channel.id() + " message=" + message);
                    channel.writeAndFlush(message + "\n");
                } catch (InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 定时任务 心跳
    private class QueryTask implements Runnable {
        @Override
        public void run() {
            appendMsgToQueue("-");
        }
    }

    @Override
    public String toString() {
        return this.hashCode() + channel.toString();
    }
}
