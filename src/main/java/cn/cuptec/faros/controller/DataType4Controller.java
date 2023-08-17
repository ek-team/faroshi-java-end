package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DataCount;
import cn.cuptec.faros.entity.DataType;
import cn.cuptec.faros.entity.DataType4;
import cn.cuptec.faros.service.DataCountService;
import cn.cuptec.faros.service.DataType4Service;
import cn.cuptec.faros.service.DataTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dataType4")
public class DataType4Controller extends AbstractBaseController<DataType4Service, DataType4> {

    @PostMapping("/save")
    public RestResponse<Boolean> save(@RequestBody DataType4 dataType) {

        return RestResponse.ok(service.save(dataType));
    }
    @GetMapping("/removeById")
    public RestResponse<Boolean> removeById(@RequestParam("id") Integer id) {
        return RestResponse.ok(service.removeById(id));
    }
    @GetMapping("/page")
    public RestResponse page() {
        Class<DataType4> entityClass = getEntityClass();
        Page<DataType4> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(entityClass);
        return RestResponse.ok(service.page(page, queryWrapper));
    }
    @Override
    protected Class<DataType4> getEntityClass() {
        return DataType4.class;
    }
}
