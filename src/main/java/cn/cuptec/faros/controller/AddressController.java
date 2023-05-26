package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Address;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户地址管理
 */
@RestController
@RequestMapping("/address")
public class AddressController {
    @Resource
    private AddressService addressService;

    /**
     * 添加地址
     *
     * @param address
     * @return
     */
    @PostMapping("/saveAddress")
    public RestResponse saveAddress(@RequestBody Address address) {
        address.setPatientId(SecurityUtils.getUser().getId());
        if (address.getIsDefault() == 1) {
            // 将当前账号下其他默认地址全部修改为 o
            addressService.update(Wrappers.<Address>lambdaUpdate()
                    .eq(Address::getPatientId, address.getPatientId())
                    .set(Address::getIsDefault, 0));

        }
        addressService.save(address);
        return RestResponse.ok();
    }

    /**
     * 列表查询
     */
    @GetMapping("/getAddressList")
    public RestResponse getAddressList() {

        return RestResponse.ok(addressService.list(new QueryWrapper<Address>().lambda().eq(Address::getPatientId, SecurityUtils.getUser().getId())));
    }

    /**
     * =删除
     */
    @GetMapping("/deleteAddress")
    public RestResponse deleteAddress(@RequestParam("id") int id) {

        addressService.removeById(id);
        return RestResponse.ok();
    }

    @PostMapping("/updateAddress")
    public RestResponse updateAddress(@RequestBody Address address) {
        if (address.getIsDefault() == 1) {
            // 将当前账号下其他默认地址全部修改为 o
            addressService.update(Wrappers.<Address>lambdaUpdate()
                    .eq(Address::getPatientId, address.getPatientId())
                    .set(Address::getIsDefault, 0));

        }

        addressService.updateById(address);
        return RestResponse.ok();

    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {
        return RestResponse.ok(addressService.getById(id));

    }

    /**
     * 查询用户默认收货地址
     *
     * @return
     */
    @GetMapping("/getDefalut")
    public RestResponse getDefalut() {
        List<Address> list = addressService.list(new QueryWrapper<Address>().lambda()
                .eq(Address::getPatientId, SecurityUtils.getUser().getId())
                .eq(Address::getIsDefault, 1));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        return RestResponse.ok(list.get(0));

    }
}
