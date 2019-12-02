package space.ponyo.dc1.server;

import org.springframework.stereotype.Component;
import space.ponyo.dc1.server.model.db.SqliteOpenHelper;
import space.ponyo.dc1.server.server.ConnectionManager;
import space.ponyo.dc1.server.server.NettySocketServer;
import space.ponyo.dc1.server.util.LogUtil;
import space.ponyo.dc1.server.util.MD5;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
class NettyServer {

    public static final String CONF_NAME = "/opt/dc1.conf";

    public void startNetty() {
        Properties properties = getProperties(CONF_NAME);
        if (properties == null) {
            ConnectionManager.getInstance().token = MD5.getMD5("dc1server");
            LogUtil.notice("token:dc1server");
        } else {
            String property = properties.getProperty("token", "dc1server");
            LogUtil.notice("token:" + property);
            ConnectionManager.getInstance().token = MD5.getMD5(property);
        }
        SqliteOpenHelper.connectSqlite("jdbc:sqlite:/opt/dc1.conf");
        new NettySocketServer().start();
    }

    private Properties getProperties(String propertyFile) {
        try {
            InputStream is = new FileInputStream(propertyFile);
            Properties pros = new Properties();
            pros.load(is);
            return pros;
        } catch (IOException e) {
            System.out.println(propertyFile + " is can not read");
        }
        File file = new File("/opt/dc1.conf");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
