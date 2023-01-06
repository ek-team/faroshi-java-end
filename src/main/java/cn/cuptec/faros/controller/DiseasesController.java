package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DiseasesService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 病种 管理
 */
@RestController
@RequestMapping("/diseases")
public class DiseasesController extends AbstractBaseController<DiseasesService, Diseases> {
    @Resource
    private UserService userService;

    /**
     * 添加病种
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse save(@RequestBody Diseases diseases) {
        User user = userService.getById(SecurityUtils.getUser().getId());
        diseases.setDeptId(user.getDeptId());
        diseases.setCreateTime(LocalDateTime.now());
        diseases.setCreateUserId(SecurityUtils.getUser().getId());
        diseases.setStatus(0);
        return RestResponse.ok(service.save(diseases));
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Diseases diseases) {

        return RestResponse.ok(service.updateById(diseases));
    }

    /**
     * 分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageScoped() {
        User user = userService.getById(SecurityUtils.getUser().getId());
        Page<Diseases> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("dept_id", user.getDeptId());
        IPage<Diseases> followUpPlanIPage = service.page(page, queryWrapper);
        List<Diseases> records = followUpPlanIPage.getRecords();
        return RestResponse.ok(followUpPlanIPage);
    }


    @Override
    protected Class<Diseases> getEntityClass() {
        return Diseases.class;
    }
}
