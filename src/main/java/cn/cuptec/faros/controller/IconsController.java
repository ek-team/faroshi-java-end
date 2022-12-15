package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Icons;
import cn.cuptec.faros.service.IconsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/icons")
public class IconsController extends AbstractBaseController<IconsService, Icons> {

    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper<Icons> queryWrapper = getQueryWrapper(getEntityClass());
        Page<Icons> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public RestResponse listByDoctor() {
        return RestResponse.ok(service.list());
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Icons icons) {
        return RestResponse.ok(service.updateById(icons));
    }

    @PostMapping("/save")
    public RestResponse save(@RequestBody Icons icons) {
        return RestResponse.ok(service.save(icons));
    }

    @GetMapping("/deleteById")
    public RestResponse delById(@RequestParam("id") int id) {
        return RestResponse.ok(service.removeById(id) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    @Override
    protected Class<Icons> getEntityClass() {
        return Icons.class;
    }
}
