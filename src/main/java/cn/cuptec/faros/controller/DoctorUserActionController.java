package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.DoctorUserActionService;
import cn.cuptec.faros.service.DoctorUserServiceSetUpService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医生可以设置的服务
 */
@RestController
@RequestMapping("/doctorUserAction")
public class DoctorUserActionController extends AbstractBaseController<DoctorUserActionService, DoctorUserAction> {
    @Resource
    private DoctorUserServiceSetUpService doctorUserServiceSetUpService;//医生可以设置哪些服务

    /**
     * 查询医生可以设置的服务
     *
     * @return
     */
    @GetMapping("/queryService")
    public RestResponse queryService() {
        List<DoctorUserServiceSetUp> list = doctorUserServiceSetUpService.list();
        List<Integer> doctorUserServiceSetUpIds = list.stream().map(DoctorUserServiceSetUp::getId)
                .collect(Collectors.toList());
        List<DoctorUserAction> doctorUserActions = service.list(new QueryWrapper<DoctorUserAction>().lambda()
                .in(DoctorUserAction::getDoctorUserServiceSetUpId, doctorUserServiceSetUpIds)
                .eq(DoctorUserAction::getUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(doctorUserActions)) {
            Map<Integer, DoctorUserAction> doctorUserActionMap = doctorUserActions.stream()
                    .collect(Collectors.toMap(DoctorUserAction::getDoctorUserServiceSetUpId, t -> t));
            for (DoctorUserServiceSetUp doctorUserServiceSetUp : list) {
                doctorUserServiceSetUp.setDoctorUserAction(doctorUserActionMap.get(doctorUserServiceSetUp.getId()));
            }

        }

        return RestResponse.ok(list);
    }

    /**
     * 开通服务
     *
     * @return
     */
    @PostMapping("/openService")
    public RestResponse openService(@RequestBody DoctorUserAction doctorUserAction) {
        doctorUserAction.setUserId(SecurityUtils.getUser().getId());
        service.save(doctorUserAction);
        return RestResponse.ok();
    }

    /**
     * 修改服务
     *
     * @return
     */
    @PostMapping("/updateService")
    public RestResponse updateService(@RequestBody DoctorUserAction doctorUserAction) {
        service.updateById(doctorUserAction);
        return RestResponse.ok();
    }

    /**
     * 查询服务详情
     *
     * @return
     */
    @GetMapping("/getServiceDetail")
    public RestResponse getServiceDetail(@RequestParam("id") String id) {
        DoctorUserAction byId = service.getById(id);
        return RestResponse.ok(byId);
    }

    /**
     * 查询医生团队服务详情
     *
     * @return
     */
    @GetMapping("/getTeamServiceDetail")
    public RestResponse getTeamServiceDetail(@RequestParam("teamId") Integer teamId) {
        DoctorUserAction byId = service.getOne(new QueryWrapper<DoctorUserAction>()
                .lambda().eq(DoctorUserAction::getTeamId, teamId));
        if (byId == null) {
            byId = new DoctorUserAction();
            byId.setHour(0);
            byId.setCount(0);
            byId.setPrice(0.0);
            byId.setDoctorUserServiceSetUpId(3);
        }
        return RestResponse.ok(byId);
    }

    @Override
    protected Class<DoctorUserAction> getEntityClass() {
        return DoctorUserAction.class;
    }


}
