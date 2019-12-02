package space.ponyo.dc1.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import space.ponyo.dc1.server.bean.MyHttpResponse;
import space.ponyo.dc1.server.model.DataPool;
import space.ponyo.dc1.server.model.db.PlanDao;
import space.ponyo.dc1.server.server.ConnectionManager;
import space.ponyo.dc1.server.util.LogUtil;


@RestController
public class Controller {

    @RequestMapping(value = "/api/queryDeviceList",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String queryDeviceList(
            @RequestParam(name = "token") String token) {
        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        return MyHttpResponse.success(DataPool.dc1Map.values());
    }


    @RequestMapping(value = "/api/queryPlanList",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String queryPlanList(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "token") String deviceId) {

        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        return MyHttpResponse.success(PlanDao.getInstance().queryAllByDeviceId(deviceId));
    }


    private boolean checkToken(String token) {
        if (token == null || "".equals(token) || !ConnectionManager.getInstance().token.equals(token)) {
            LogUtil.warning("tip token验证失败！");
            return true;
        }
        return false;
    }
}
