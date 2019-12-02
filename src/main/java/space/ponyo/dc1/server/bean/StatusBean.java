package space.ponyo.dc1.server.bean;

/**
 * 状态查询
 */
public class StatusBean {
    /**
     * "status" : 1011
     * "I" : 119
     * "V" : 219
     * "P" : 10
     */

    private int status;
    private int I;
    private int V;
    private int P;

    public String getStatus() {
        return parseToPhoneStatus(status);
    }

    public void setStatus(String status) {
        this.status = parseToDc1Status(status);
    }

    public int getI() {
        return I;
    }

    public void setI(int I) {
        this.I = I;
    }

    public int getV() {
        return V;
    }

    public void setV(int V) {
        this.V = V;
    }

    public int getP() {
        return P;
    }

    public void setP(int P) {
        this.P = P;
    }

    /**
     * 转换成dc1的数据格式
     *
     * @param status
     * @return
     */
    public static int parseToDc1Status(String status) {
        if (status.startsWith("0")) {
            return 0;
        }
        if (status.equals("1011")) {
            return Integer.parseInt("1101");
        }
        if (status.equals("1101")) {
            return Integer.parseInt("1011");
        }
        while (status.endsWith("0")) {
            status = status.substring(0, status.length() - 1);
        }
        if (status.equals("")) {
            return Integer.parseInt("0");
        }
        return Integer.parseInt(status);
    }

    /**
     * 转换成手机端数据，标准数据
     *
     * @param sta
     * @return
     */
    public static String parseToPhoneStatus(int sta) {
        String status = String.valueOf(sta);
        if (status.equals("1011")) {
            return "1101";
        }
        if (status.equals("1101")) {
            return "1011";
        }
        StringBuilder sb = new StringBuilder(String.valueOf(sta));
        while (sb.length() < 4) {
            sb.append("0");
        }
        return sb.toString();
    }
}
