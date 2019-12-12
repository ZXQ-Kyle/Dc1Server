package space.ponyo.dc1.server.util;

public class LogUtil {

    public static final boolean isRelease = true;

    public static void warning(String msg) {
        System.out.println("########################################################################");
        System.out.println("#[WARNING]# " + msg);
        System.out.println("########################################################################");
    }
    public static void notice(String msg) {
        System.out.println("///////////////////////////////////////////////////////////////////////");
        System.out.println("/[NOTICE]/ " + msg);
        System.out.println("///////////////////////////////////////////////////////////////////////");
    }

    public static void info(String msg) {
        if (isRelease) {
            return;
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.println("-[iNFO]- " + msg);
        System.out.println("------------------------------------------------------------------------");
    }
}
