package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.ProductType;
import cn.cuptec.faros.service.ProductService;
import cn.cuptec.faros.service.ProductTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController extends AbstractBaseController<ProductService, Product> {
    @Resource
    private ProductTypeService productTypeService;

    /**
     * 根据type查询产品
     */
    @GetMapping("/getByType")
    public RestResponse getByType(@RequestParam("type") int type, @RequestParam("salesmanId") int salesmanId) {
        List<Product> records = service.list(Wrappers.<Product>lambdaQuery().eq(Product::getDelFlag, 0).eq(Product::getProductType, type));
        if (CollectionUtils.isEmpty(records)) {
            return RestResponse.ok();
        }
        List<Object> result = new ArrayList<>();
        for (Product product : records) {
            Object productDetail = service.getProductDetail(product.getId(), salesmanId);
            result.add(productDetail);
        }
        return RestResponse.ok(result);

    }

    /**
     * 产品列表
     *
     * @param
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageList() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<Product> page = getPage();
        IPage page1 = service.page(page, queryWrapper);
        List<Product> records = page1.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return RestResponse.ok(page1);
        }
        List<Integer> productTypeIds = records.stream().map(Product::getProductType)
                .collect(Collectors.toList());
        List<ProductType> productTypes = (List<ProductType>) productTypeService.listByIds(productTypeIds);
        if (!CollectionUtils.isEmpty(productTypes)) {
            Map<Integer, ProductType> productTypeMap = productTypes.stream()
                    .collect(Collectors.toMap(ProductType::getId, t -> t));
            for (Product product : records) {
                ProductType productType = productTypeMap.get(product.getProductType());
                if (productType != null) {
                    product.setProductTypeName(productType.getName());
                }
            }
        }
        page1.setRecords(records);
        return RestResponse.ok(page1);
    }

    @GetMapping("/listAll")
    public RestResponse listAll() {
        return RestResponse.ok(service.list());
    }

    @GetMapping("queryByCategory")
    public RestResponse queryByCategory(@RequestParam("category") int category) {
        return RestResponse.ok(service.queryByCategory(category));
    }


    @GetMapping("/getById/{id}")
    public RestResponse getById(@PathVariable int id) {
        Product product = service.getById(id);
        if (product != null) {
            return RestResponse.ok(product);
        } else {
            return RestResponse.failed(DATA_QUERY_FAILED, null);
        }
    }

    /**
     * 产品的增加
     *
     * @param product
     * @return
     */
    @PostMapping("/add")
    public RestResponse<Product> addProduct(@RequestBody Product product) {
        product.setDelFlag("0");
        service.save(product);
        return RestResponse.ok();
    }

    /**
     * 产品的删除  del_flag == 0 表示删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    public RestResponse productDel(@PathVariable int id) {
        return RestResponse.ok(service.deleteByIdCascade(id) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    /**
     * 产品的修改
     *
     * @param product
     * @return
     */
    @PutMapping("/update")
    public RestResponse updateProduct(@Valid @RequestBody Product product) {
        return service.updateById(product) ? RestResponse.ok(DATA_UPDATE_SUCCESS, null) : RestResponse.failed(DATA_UPDATE_FAILED, null);
    }

    @PutMapping("/updateDetail")
    public RestResponse updateDetail(@Valid @RequestBody Product product) {
        service.updateDetailById(product);
        return RestResponse.ok();
    }


    /**
     * 用户获取产品信息
     *
     * @param productId
     * @param salesmanId
     * @return
     */
    @GetMapping("/user/getById")
    public RestResponse userGetById(@RequestParam int productId, @RequestParam(required = false) Integer salesmanId) {
        Object productDetail = service.getProductDetail(productId, salesmanId);
        if (productDetail != null) {
            return RestResponse.ok(productDetail);
        } else {
            return RestResponse.failed(DATA_QUERY_FAILED);
        }
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }
}
