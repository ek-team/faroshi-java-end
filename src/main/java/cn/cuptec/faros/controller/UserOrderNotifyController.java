package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.UserOrderNotify;
import cn.cuptec.faros.service.UserOrderNotifyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/userOrderNotify")
public class UserOrderNotifyController extends AbstractBaseController<UserOrderNotifyService, UserOrderNotify> {

    @PostMapping("/save")
    public RestResponse save(@RequestBody UserOrderNotify userOrderNotify) {


        return RestResponse.ok(service.save(userOrderNotify));
    }

    @GetMapping("/page")
    public RestResponse page() {
        Page<UserOrderNotify> page = getPage();
        return RestResponse.ok(service.page(page, new QueryWrapper<>()));
    }

    @Override
    protected Class<UserOrderNotify> getEntityClass() {
        return UserOrderNotify.class;
    }
}
