package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.CustomProduct;
import cn.cuptec.faros.service.CustomProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customProduct")
public class CustomProductController extends AbstractBaseController<CustomProductService, CustomProduct> {

    @PostMapping
    public RestResponse save(@RequestBody CustomProduct customProduct){
        service.save(customProduct);
        return RestResponse.ok();
    }

    @DeleteMapping("/{id}")
    public RestResponse deleteById(@PathVariable int id){
        service.removeById(id);
        return RestResponse.ok();
    }

    @PutMapping
    public RestResponse updateById(@RequestBody CustomProduct customProduct){
        service.updateById(customProduct);
        return RestResponse.ok();
    }

    @GetMapping("/pageScoped")
    public RestResponse pageScoped(){
        Page<CustomProduct> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage pageResult = service.pageScopeCustomProduct(page, queryWrapper);
        return RestResponse.ok(pageResult);
    }

    @Override
    protected Class<CustomProduct> getEntityClass() {
        return CustomProduct.class;
    }
}
