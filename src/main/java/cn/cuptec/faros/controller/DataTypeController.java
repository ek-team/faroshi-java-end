package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Article;
import cn.cuptec.faros.entity.DataCount;
import cn.cuptec.faros.entity.DataType;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.DataCountService;
import cn.cuptec.faros.service.DataTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/dataType")
public class DataTypeController extends AbstractBaseController<DataTypeService, DataType> {

    @PostMapping("/save")
    public RestResponse<Boolean> save(@RequestBody DataType dataType) {

        return RestResponse.ok(service.save(dataType));
    }
    @GetMapping("/removeById")
    public RestResponse<Boolean> removeById(@RequestParam("id") Integer id) {
        return RestResponse.ok(service.removeById(id));
    }
    @GetMapping("/page")
    public RestResponse page() {
        Class<DataType> entityClass = getEntityClass();
        Page<DataType> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(entityClass);
        return RestResponse.ok(service.page(page, queryWrapper));
    }
    @Override
    protected Class<DataType> getEntityClass() {
        return DataType.class;
    }
}
