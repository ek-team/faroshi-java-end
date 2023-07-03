package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.entity.DeliverySetting;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.DeliverySettingService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 代理商快递发货设置
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/deliverySetting")
public class DeliverySettingController {
    @Resource
    private DeliverySettingService deliverySettingService;
    @Resource
    private UserService userService;

    /**
     * 开启或者关闭
     *
     * @param status
     * @return
     */
    @GetMapping("/open")
    public RestResponse getByOrderNo(@RequestParam("status") Integer status) {
        User myUSer = userService.getById(SecurityUtils.getUser().getId());
        DeliverySetting deliverySetting = deliverySettingService.getOne(new QueryWrapper<DeliverySetting>().lambda().eq(DeliverySetting::getDeptId, myUSer.getDeptId()));
        deliverySetting.setStatus(status);
        deliverySettingService.updateById(deliverySetting);
        return RestResponse.ok();
    }

    /**
     * 查询
     *
     * @return
     */
    @GetMapping("/get")
    public RestResponse get() {
        User myUSer = userService.getById(SecurityUtils.getUser().getId());
        DeliverySetting deliverySetting = deliverySettingService.getOne(new QueryWrapper<DeliverySetting>().lambda().eq(DeliverySetting::getDeptId, myUSer.getDeptId()));
        if (deliverySetting == null) {
            deliverySetting=new DeliverySetting();
            deliverySetting.setDeptId(myUSer.getDeptId());
            deliverySetting.setAddress("安徽省安庆市潜山市开发区皖水路008号3幢");
            deliverySetting.setName("方坤");
            deliverySetting.setPhone("13862406341");
            deliverySetting.setStatus(0);
            deliverySettingService.save(deliverySetting);
        }
        return RestResponse.ok(deliverySetting);
    }

    @PostMapping("/update")
    public RestResponse save(@RequestBody DeliverySetting deliverySetting) {
        deliverySettingService.updateById(deliverySetting);
        return RestResponse.ok();
    }
}
