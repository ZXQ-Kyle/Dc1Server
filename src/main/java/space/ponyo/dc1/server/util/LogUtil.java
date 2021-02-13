package space.ponyo.dc1.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final boolean isRelease = true;

    public static void warning(String msg) {
        Date date = new Date();
        System.out.println("##################" + sdf.format(date) + "##############################");
        System.out.println("#[WARNING]# " + msg);
        System.out.println("########################################################################");
    }

    public static void notice(String msg) {
        System.out.println(sdf.format(new Date()) + "   [NOTICE]: " + msg);
    }

    public static void info(String msg) {
        if (isRelease) {
            return;
        }
        System.out.println(sdf.format(new Date()) + "   [iNFO]: " + msg);
    }
}
