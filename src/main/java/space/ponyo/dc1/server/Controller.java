package space.ponyo.dc1.server;

import org.springframework.web.bind.annotation.*;
import space.ponyo.dc1.server.bean.MyHttpResponse;
import space.ponyo.dc1.server.model.DataPool;
import space.ponyo.dc1.server.model.PlanPool;
import space.ponyo.dc1.server.model.db.PlanBean;
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
            @RequestParam(name = "deviceId") String deviceId) {

        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        return MyHttpResponse.success(PlanDao.getInstance().queryAllByDeviceId(deviceId));
    }

    @RequestMapping(value = "/api/addPlan",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String addPlan(
            @RequestParam(name = "token") String token,
            @RequestBody PlanBean planBean) {

        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        boolean b = PlanPool.getInstance().addPlan(planBean);
        if (b) {
            return MyHttpResponse.success("添加成功");
        } else {
            return MyHttpResponse.error("添加失败");
        }
    }


    private boolean checkToken(String token) {
        if (token == null || "".equals(token) || !ConnectionManager.getInstance().token.equals(token)) {
            LogUtil.warning("tip token验证失败！");
            return true;
        }
        return false;
    }
}
