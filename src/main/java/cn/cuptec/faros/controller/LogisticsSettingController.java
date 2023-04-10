package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Form;
import cn.cuptec.faros.entity.LogisticsSetting;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.FormService;
import cn.cuptec.faros.service.LogisticsSettingService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流可选设置
 */
@RestController
@RequestMapping("/logisticsSetting")
public class LogisticsSettingController extends AbstractBaseController<LogisticsSettingService, LogisticsSetting> {
    @Resource
    private UserService userService;

    @PostMapping("/save")
    public RestResponse save(@RequestBody LogisticsSetting logisticsSetting) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        logisticsSetting.setDeptId(byId.getDeptId());
        service.save(logisticsSetting);

        return RestResponse.ok();
    }

    @GetMapping("/delete")
    public RestResponse delete(@RequestParam("id") Integer id) {
        service.removeById(id);

        return RestResponse.ok();
    }

    @GetMapping("/list")
    public RestResponse list() {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        List<LogisticsSetting> list = service.list(new QueryWrapper<LogisticsSetting>().lambda().eq(LogisticsSetting::getDeptId, byId.getDeptId()));


        return RestResponse.ok(list);
    }

    @Override
    protected Class<LogisticsSetting> getEntityClass() {
        return LogisticsSetting.class;
    }
}
