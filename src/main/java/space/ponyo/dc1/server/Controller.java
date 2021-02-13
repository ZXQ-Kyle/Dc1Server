package space.ponyo.dc1.server;

import org.springframework.web.bind.annotation.*;
import space.ponyo.dc1.server.bean.MyHttpResponse;
import space.ponyo.dc1.server.model.DataPool;
import space.ponyo.dc1.server.model.PlanPool;
import space.ponyo.dc1.server.model.db.PlanBean;
import space.ponyo.dc1.server.model.db.PlanDao;
import space.ponyo.dc1.server.server.ConnectionManager;
import space.ponyo.dc1.server.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    @RequestMapping(value = "/api/setDeviceStatus",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String setDeviceStatus(@RequestParam(name = "token") String token,
                                  @RequestParam(name = "deviceId") String id,
                                  @RequestParam(name = "status") String status) {
        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        ConnectionManager.getInstance().setDc1Status(id, status);
        return MyHttpResponse.success(true);
    }

    @RequestMapping(value = "/api/updateNames",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String updateNames(@RequestParam(name = "token") String token,
                              @RequestParam(name = "deviceId") String id,
                              @RequestParam(name = "names") String names) {
        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        if (names == null) {
            names = "";
        }
        String[] split = names.split(",");
        String[] target = new String[5];
        System.arraycopy(split, 0, target, 0, Math.min(split.length, 5));

        for (int i = 0; i < target.length; i++) {
            if (target[i] == null || target[i].isEmpty()) {
                target[i] = "开关";
            }
        }
        boolean update = DataPool.updateName(id, new ArrayList<>(Arrays.asList(target)));
        if (update) {
            ConnectionManager.getInstance().pushPhoneDeviceDataChanged();
        }
        return MyHttpResponse.success(true);
    }

    @RequestMapping(value = "/api/resetPower",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String resetPower(@RequestParam(name = "token") String token,
                             @RequestParam(name = "deviceId") String id) {
        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        boolean update = DataPool.resetPower(id);
        if (update) {
            ConnectionManager.getInstance().pushPhoneDeviceDataChanged();
        }
        return MyHttpResponse.success(true);
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
        List<PlanBean> data = PlanDao.getInstance().queryAllByDeviceId(deviceId);
        return data == null ? MyHttpResponse.error("查询失败") : MyHttpResponse.success(data);
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
        return b ? MyHttpResponse.success("添加成功") : MyHttpResponse.error("添加失败");
    }

    @RequestMapping(value = "/api/deletePlan",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String deletePlan(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "planId") String id) {

        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        boolean success = PlanPool.getInstance().deletePlan(id);
        return success ? MyHttpResponse.success("删除成功") : MyHttpResponse.error("删除失败");
    }

    @RequestMapping(value = "/api/enablePlanById",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=utf-8"})
    public String enablePlanById(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "planId") String id,
            @RequestParam(name = "enable") boolean enable) {

        if (checkToken(token)) {
            return MyHttpResponse.error("token验证失败！");
        }
        boolean success = PlanPool.getInstance().enablePlan(id, enable);
        return success ? MyHttpResponse.success("设置成功") : MyHttpResponse.error("设置失败");
    }

    private boolean checkToken(String token) {
        if (token == null || "".equals(token) || !ConnectionManager.getInstance().token.equals(token)) {
            LogUtil.warning("tip token验证失败！");
            return true;
        }
        return false;
    }
}
