package space.ponyo.dc1.server.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zxq
 * @Date 2/14/21.
 * @Description:
 */
class GlobalExecutors {
    private final static ScheduledExecutorService instance = Executors.newScheduledThreadPool(10);

    public static ScheduledExecutorService getInstance() {
        return instance;
    }
}
