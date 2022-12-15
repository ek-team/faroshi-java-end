package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.entity.ProductType;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.WechatAccountConfig;
import cn.cuptec.faros.service.ProductTypeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/24 16:28
 */
@RestController
@RequestMapping("/producttype")
public class ProductTypeController {
    @Resource
    private ProductTypeService productTypeService;

    @PostMapping("/add")
    public RestResponse<ProductType> user(@RequestBody ProductType productType) {
        productTypeService.save(productType);
        return RestResponse.ok();
    }

    @SysLog("删除")
    @GetMapping("/del")
    public RestResponse del(@RequestParam("id") int id) {
        return productTypeService.removeById(id) ? RestResponse.ok() : RestResponse.failed();
    }

    @SysLog("查询")
    @GetMapping("/query")
    public RestResponse<List<ProductType>> query() {
        List<ProductType> list = productTypeService.list(Wrappers.<ProductType>lambdaQuery().eq(ProductType::getStatus, 1));

        return RestResponse.ok(list);
    }

    @PostMapping("/update")
    public RestResponse update(@RequestBody ProductType productType) {
        return RestResponse.ok(productTypeService.updateById(productType));
    }
}
