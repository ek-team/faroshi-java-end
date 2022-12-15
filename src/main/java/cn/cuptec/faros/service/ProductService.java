package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Resource
    private ProductRetrieveRuleItemService productRetrieveRuleItemService;
    @Resource
    private CustomProductService customProductService;
    @Resource
    private DeptRelationService deptRelationService;
    @Resource
    private UserService userService;
    @Resource
    private ProductStockService productStockService;

    @Override
    public Product getById(Serializable id) {
        Product product = super.getById(id);
        if (product != null) {
            List<ProductRetrieveRuleItem> productRetrieveRuleItems = productRetrieveRuleItemService.list(Wrappers.<ProductRetrieveRuleItem>lambdaQuery().eq(ProductRetrieveRuleItem::getProductId, id));
            product.setProductRetrieveRuleItems(productRetrieveRuleItems);
        }
        return product;
    }

    //获取产品详情，若业务员不为空，则获取业务员自己的产品详情，否则获取厂家产品详情
    public Object getProductDetail(int id, Integer salesmanId) {
        //焦恒注释 所有都走厂家不走业务员自己
//        User user = null;
//        if (salesmanId != null) {
//            user = userService.getById(salesmanId);
//        }
//        if (user != null && user.getDeptId() != null) {
//            CustomProduct customProduct = customProductService.getDetailByProductIdAndDeptIdCascade(id, user.getDeptId());
//            if (customProduct != null) {
//                return customProduct;
//            }
//        }
        return getById(id);
    }

    //根据category查询产品
    public List<Product> queryByCategory(int category) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategory, category);
        List<Product> list = list(queryWrapper);
        return list;
    }

    @Transactional
    @Override
    public boolean save(Product entity) {
//        List<ProductRetrieveRuleItem> productRetrieveRuleItems = entity.getProductRetrieveRuleItems();
//        //校验产品回收规则
//        if (productRetrieveRuleItems == null || productRetrieveRuleItems.size() == 0){
//            throw new RuntimeException("产品回收规则不能为空!");
//        }
//        productRetrieveRuleItemService.saveBatch(productRetrieveRuleItems);
        super.save(entity);
        return Boolean.TRUE;
    }

    @Transactional
    public boolean updateDetailById(Product entity) {
        List<ProductRetrieveRuleItem> productRetrieveRuleItems = entity.getProductRetrieveRuleItems();
        //校验产品回收规则
        if (productRetrieveRuleItems == null || productRetrieveRuleItems.size() == 0) {
            throw new RuntimeException("产品回收规则不能为空!");
        }
        productRetrieveRuleItemService.remove(Wrappers.<ProductRetrieveRuleItem>lambdaQuery().eq(ProductRetrieveRuleItem::getProductId, entity.getId()));
        productRetrieveRuleItemService.saveBatch(productRetrieveRuleItems);
        super.updateById(entity);
        return Boolean.TRUE;
    }

    @Transactional
    public boolean deleteByIdCascade(int productId) {
        int count = productStockService.count(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getProductId, productId));
        Assert.isTrue(count == 0, "产品库存不为空，请先清空库存");
        super.removeById(productId);
        productRetrieveRuleItemService.remove(Wrappers.<ProductRetrieveRuleItem>lambdaQuery().eq(ProductRetrieveRuleItem::getProductId, productId));
        customProductService.deleteByProductIdCascade(productId);
        return Boolean.TRUE;
    }

}
