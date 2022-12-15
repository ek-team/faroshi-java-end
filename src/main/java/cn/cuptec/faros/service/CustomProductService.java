package cn.cuptec.faros.service;

import cn.cuptec.faros.common.exception.CheckedException;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.CustomProduct;
import cn.cuptec.faros.entity.CustomProductRetrieveRuleItem;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.mapper.CustomProductMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomProductService extends ServiceImpl<CustomProductMapper, CustomProduct> {

    @Resource
    private CustomProductRetrieveRuleItemService customProductRetrieveRuleItemService;
    @Resource
    private DeptService deptService;
    @Resource
    private ProductService productService;

    @Transactional
    @Override
    public synchronized boolean save(CustomProduct entity) {
        List<CustomProduct> list = list(Wrappers.<CustomProduct>lambdaQuery()
                .eq(CustomProduct::getDeptId, SecurityUtils.getUser().getDeptId())
                .eq(CustomProduct::getProductId, entity.getProductId())
        );
        if (list.size() > 0){
            throw new RuntimeException("您所在部门已有对应回收规则，请不要重复添加");
        }

        List<CustomProductRetrieveRuleItem> customProductRetrieveRuleItems = entity.getCustomProductRetrieveRuleItems();
        //校验产品回收规则
        if (customProductRetrieveRuleItems == null || customProductRetrieveRuleItems.size() == 0){
            throw new RuntimeException("产品回收规则不能为空!");
        }
        entity.setDeptId(SecurityUtils.getUser().getDeptId());
        entity.setCreateBy(SecurityUtils.getUser().getId());
        super.save(entity);
        customProductRetrieveRuleItems.forEach(customProductRetrieveRuleItem -> customProductRetrieveRuleItem.setCustomProductId(entity.getProductId()));
        customProductRetrieveRuleItemService.saveBatch(customProductRetrieveRuleItems);
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public boolean updateById(CustomProduct entity) {
        List<CustomProductRetrieveRuleItem> customProductRetrieveRuleItems = entity.getCustomProductRetrieveRuleItems();
        //校验产品回收规则
        if (customProductRetrieveRuleItems == null || customProductRetrieveRuleItems.size() == 0){
            throw new RuntimeException("产品回收规则不能为空!");
        }
        customProductRetrieveRuleItems.forEach(customProductRetrieveRuleItem -> customProductRetrieveRuleItem.setCustomProductId(entity.getId()));
        customProductRetrieveRuleItemService.remove(Wrappers.<CustomProductRetrieveRuleItem>lambdaQuery().eq(CustomProductRetrieveRuleItem::getCustomProductId, entity.getId()));
        customProductRetrieveRuleItemService.saveBatch(customProductRetrieveRuleItems);
        super.updateById(entity);
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        super.removeById(id);
        customProductRetrieveRuleItemService.remove(Wrappers.<CustomProductRetrieveRuleItem>lambdaQuery().eq(CustomProductRetrieveRuleItem::getCustomProductId, id));
        return Boolean.TRUE;
    }

    @Transactional
    public void deleteByProductIdCascade(int productId) {
        List<CustomProduct> customProducts = list(Wrappers.<CustomProduct>lambdaQuery().eq(CustomProduct::getProductId, productId));
        if (customProducts != null && customProducts.size() > 0){
            super.remove(Wrappers.<CustomProduct>lambdaQuery().eq(CustomProduct::getProductId, productId));
            customProductRetrieveRuleItemService.remove(Wrappers.<CustomProductRetrieveRuleItem>lambdaQuery().in(CustomProductRetrieveRuleItem::getCustomProductId, customProducts.stream().map( customProduct -> customProduct.getId()).collect(Collectors.toList())));
        }
    }

    public IPage<CustomProduct> pageScopeCustomProduct(IPage page, Wrapper<CustomProduct> queryWrapper) {
        IPage<CustomProduct> customProductIPage = baseMapper.pageScopedCustomProduct(page, queryWrapper, new DataScope());
        List<CustomProduct> records = customProductIPage.getRecords();
        records.forEach( customProduct -> {
            List<CustomProductRetrieveRuleItem> customProductRetrieveRuleItems = customProductRetrieveRuleItemService.list(Wrappers.<CustomProductRetrieveRuleItem>lambdaQuery()
                    .eq(CustomProductRetrieveRuleItem::getCustomProductId, customProduct.getId()));
            customProduct.setCustomProductRetrieveRuleItems(customProductRetrieveRuleItems);
        });
        return customProductIPage;
    }

    //级联获取自定义产品信息（本级没有定义，则往上级查找）
    public CustomProduct getDetailByProductIdAndDeptIdCascade(int productId, int deptId){
        CustomProduct customProduct = null;
        while (customProduct == null){
            Dept dept = deptService.getById(deptId);
            if (dept != null){
                customProduct = getOne(Wrappers.<CustomProduct>lambdaQuery()
                        .eq(CustomProduct::getProductId, productId)
                        .eq(CustomProduct::getDeptId, dept.getId())
                );
                if (dept.getParentId() != null && dept.getParentId() != -1){
                    deptId = dept.getParentId();
                }
                else{
                    break;
                }
            }
            else {
                break;
            }
        }
        if (customProduct != null){
            //设置产品信息
            Product product = productService.getById(customProduct.getProductId());
            customProduct.setProductName(product.getProductName());
            customProduct.setProductPic(product.getProductPic());
            customProduct.setProductType(product.getProductType());
            if (StringUtils.isEmpty(customProduct.getDetailHtml())){
                customProduct.setDetailHtml(product.getDetailHtml());
            }
            //设置产品回收规则信息
            List<CustomProductRetrieveRuleItem> customProductRetrieveRuleItems = customProductRetrieveRuleItemService.list(Wrappers.<CustomProductRetrieveRuleItem>lambdaQuery().eq(CustomProductRetrieveRuleItem::getCustomProductId, customProduct.getId()));
            customProduct.setCustomProductRetrieveRuleItems(customProductRetrieveRuleItems);
        }
        return customProduct;
    }

}
