package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DeliveryTime;
import cn.cuptec.faros.entity.DoctorTeam;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.DeliveryTimeService;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 期望发货时间设置
 */
@RestController
@AllArgsConstructor
@RequestMapping("/deliveryTime")
public class DeliveryTimeController extends AbstractBaseController<DeliveryTimeService, DeliveryTime> {
    @Resource
    private UserService userService;

    @GetMapping("/queryDeliveryTime")
    public RestResponse queryDeliveryTime() {
        User user = userService.getById(SecurityUtils.getUser().getId());
        DeliveryTime one = service.getOne(new QueryWrapper<DeliveryTime>().lambda()
                .eq(DeliveryTime::getDeptId, user.getDeptId()));
        if (one == null) {
            one = new DeliveryTime();
            one.setDeptId(user.getDeptId());
            service.save(one);
        }
        return RestResponse.ok(one);
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody DeliveryTime deliveryTime) {
        service.updateById(deliveryTime);
        return RestResponse.ok(deliveryTime);
    }

    @GetMapping("/getByDeptId")
    public RestResponse getByDeptId(@RequestParam("deptId") Integer deptId) {
        DeliveryTime one = service.getOne(new QueryWrapper<DeliveryTime>().lambda()
                .eq(DeliveryTime::getDeptId, deptId));
        if (one == null) {
            one = new DeliveryTime();
            one.setDeptId(deptId);
            service.save(one);
        }
        return RestResponse.ok(one);
    }

    @Override
    protected Class<DeliveryTime> getEntityClass() {
        return DeliveryTime.class;
    }

}
