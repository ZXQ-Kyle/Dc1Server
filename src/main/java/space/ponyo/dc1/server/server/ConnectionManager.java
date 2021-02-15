package space.ponyo.dc1.server.server;

import io.netty.channel.Channel;
import space.ponyo.dc1.server.model.DataPool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 只管理和硬件dc1的连接
 */
public class ConnectionManager {
    private static final ConnectionManager instance = new ConnectionManager();
    public String token = "";

    public static ConnectionManager getInstance() {
        return instance;
    }

    //key:channelId value:connection
    private ConcurrentHashMap<String, DeviceConnection> mDeviceConnectionMap = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void dispatchMsg(Channel channel, String msg) {
        executorService.execute(() -> {
            DeviceConnection dc = mDeviceConnectionMap.get(channel.id().asLongText());
            if (dc != null) {
                dc.processMessage(msg);
            }
        });
    }

    public IConnection addChannel(Channel channel) {
        DeviceConnection connection = mDeviceConnectionMap.get(channel.id().asLongText());
        if (connection == null) {
            connection = new DeviceConnection();
            mDeviceConnectionMap.put(channel.id().asLongText(), connection);
        }
        connection.setChannel(channel);
        return connection;
    }

    public void removeChannel(Channel channel) {
        DeviceConnection deviceConnection = mDeviceConnectionMap.get(channel.id().asLongText());
        if (deviceConnection == null || deviceConnection.isActive()) {
            return;
        }
        DataPool.offline(deviceConnection.getId());
        mDeviceConnectionMap.remove(channel.id().asLongText());
    }

    public void setDc1Status(String id, String status) {
        mDeviceConnectionMap
                .values()
                .parallelStream()
                .filter(connection -> connection.getId().equals(id))
                .forEach(connection -> connection.setStatus(status));
    }
}
