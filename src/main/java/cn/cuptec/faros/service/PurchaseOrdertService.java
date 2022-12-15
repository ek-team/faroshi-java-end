package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.PurchaseOrder;
import cn.cuptec.faros.entity.PurchaseOrderItem;
import cn.cuptec.faros.mapper.PurchaseOrderMapper;
import cn.cuptec.faros.vo.ProductFlittingStockInfoVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PurchaseOrdertService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    @Resource
    private PurchaseOrderItemService purchaseOrderItemService;
    @Resource
    private ProductService productService;

    /**
     * 分页查找当前用户的订单
     *
     * @param page
     * @param wrapper
     * @return
     */
    public IPage pageMy(IPage page, QueryWrapper wrapper) {
        wrapper.eq("purchaser_id", SecurityUtils.getUser().getId());
        IPage<PurchaseOrder> pageResult = super.page(page, wrapper);
        List<PurchaseOrder> records = pageResult.getRecords();
        records = setRecordsItems(records);
        pageResult.setRecords(records);
        return pageResult;
    }

    //根据数据权限查询采购单列表
    public IPage<PurchaseOrder> pageScoped(Page<PurchaseOrder> page, Wrapper queryWrapper) {
        IPage<PurchaseOrder> pageResult = baseMapper.pageScoped(page, queryWrapper, new DataScope());
        List<PurchaseOrder> records = pageResult.getRecords();
        records = setRecordsItems(records);
        pageResult.setRecords(records);
        return pageResult;
    }

    @Override
    @Transactional
    public boolean save(PurchaseOrder entity) {
        Assert.noNullElements(entity.getPurchaseOrderItemList(), "采购的产品不能为空");
        entity.setPurchaserId(SecurityUtils.getUser().getId());
        entity.setDeptId(SecurityUtils.getUser().getDeptId());
        entity.setCreateTime(new Date());
        entity.setOrderNo(OrderNumberUtil.getLocalTrmSeqNum());
        entity.setStatus(0);
        entity.setFlitStatu(0);
        super.save(entity);
        List<PurchaseOrderItem> orderItemList = entity.getPurchaseOrderItemList();
        orderItemList.forEach(orderItem -> {
            orderItem.setPurchaseOrderId(entity.getId());
            orderItem.setLockCount(0);
            orderItem.setDeliveryCount(0);
        });
        purchaseOrderItemService.saveBatch(orderItemList);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public boolean updateById(PurchaseOrder entity) {
        Assert.noNullElements(entity.getPurchaseOrderItemList(), "采购的产品不能为空");
        super.updateById(entity);
        purchaseOrderItemService.remove(Wrappers.<PurchaseOrderItem>lambdaQuery().eq(PurchaseOrderItem::getPurchaseOrderId, entity.getId()));
        purchaseOrderItemService.saveBatch(entity.getPurchaseOrderItemList());
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        PurchaseOrder purchaseOrder = super.getById(id);
        Assert.isTrue(purchaseOrder.getStatus() == 0 || purchaseOrder.getStatus() == 20, "已确认采购单不允许删除");
        super.removeById(id);
        purchaseOrderItemService.remove(Wrappers.<PurchaseOrderItem>lambdaQuery().eq(PurchaseOrderItem::getPurchaseOrderId, id));
        return Boolean.TRUE;
    }

    /**
     * 批量设置采购单的采购产品列表
     *
     * @param records
     * @return
     */
    private List<PurchaseOrder> setRecordsItems(List<PurchaseOrder> records) {
        List<Integer> orderIdList = new ArrayList<>();
        records.forEach(order -> {
            orderIdList.add(order.getId());
        });
        if (orderIdList.size() > 0) {
            Collection<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemService.listDetail(Wrappers.<PurchaseOrderItem>lambdaQuery().in(PurchaseOrderItem::getPurchaseOrderId, orderIdList));
            records.forEach(order -> {
                if (order.getPurchaseOrderItemList() == null) {
                    order.setPurchaseOrderItemList(new ArrayList<>());
                }
                order.getPurchaseOrderItemList().addAll(
                        purchaseOrderItems.stream().filter(orderItem -> orderItem.getPurchaseOrderId().intValue() == order.getId().intValue()).collect(Collectors.toList())
                );
            });
        }
        return records;
    }

    public void confirmPurchase(int id) {
        PurchaseOrder purchaseOrder = super.getById(id);
        Assert.isTrue(purchaseOrder.getStatus() == 0, "数据状态错误，请刷新后重试");
        purchaseOrder.setStatus(10);
        purchaseOrder.setConfirmTime(new Date());
        super.updateById(purchaseOrder);
    }

    public void cancelPurchase(int id) {
        PurchaseOrder purchaseOrder = super.getById(id);
        Assert.isTrue(purchaseOrder.getStatus() == 0, "已确认采购单不可以取消");
        purchaseOrder.setStatus(20);
        super.updateById(purchaseOrder);
    }

    /**
     * 查询当前用户组织内的产品库存信息
     * 用于调拨
     */
    public List<ProductFlittingStockInfoVo> getScopedStockInfo() {
        List<PurchaseOrder> purchaseOrders = listScoped(Wrappers.<PurchaseOrder>lambdaQuery().eq(PurchaseOrder::getStatus, 10));
        List<Integer> purchaseOrderIds = purchaseOrders.stream().map(purchaseOrder -> purchaseOrder.getId()).collect(Collectors.toList());
        if (purchaseOrderIds.size() > 0) {
            List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemService.list(Wrappers.<PurchaseOrderItem>lambdaQuery().in(PurchaseOrderItem::getPurchaseOrderId, purchaseOrderIds));
            List<ProductFlittingStockInfoVo> vos = new ArrayList<>();
            purchaseOrderItems.forEach(it -> {
                Optional<ProductFlittingStockInfoVo> first = vos.stream().filter(vo -> vo.getProductId() == it.getProductId().intValue()).findFirst();
                if (first.isPresent()) {
                    ProductFlittingStockInfoVo vo = first.get();
                    vo.setTotalCount(vo.getTotalCount() + it.getPurchaseCount());
                    vo.setDeliveryCount(vo.getDeliveryCount() + it.getDeliveryCount());
                    vo.setLockCount(vo.getLockCount() + it.getLockCount());
                } else {
                    ProductFlittingStockInfoVo vo = new ProductFlittingStockInfoVo();
                    vo.setProductId(it.getProductId());
                    vo.setTotalCount(it.getPurchaseCount());
                    vo.setDeliveryCount(it.getDeliveryCount());
                    vo.setLockCount(it.getLockCount());
                    vos.add(vo);
                }
            });
            System.out.println(vos);
            List<Integer> productIds = vos.stream().map(vo -> vo.getProductId()).collect(Collectors.toList());
            List<Product> products = (List<Product>) productService.listByIds(productIds);
            vos.forEach(vo -> {
//                Stream<Product> productStream = products.stream().filter(product -> product.getId().intValue() == vo.getProductId());
//                Optional<Product> first = productStream.findFirst();
                Product product1 = null;
                for (Product product : products) {
                    if (product.getId().intValue() == vo.getProductId()) {
                        product1 = product;
                        break;
                    }
                }
                if (product1 != null) {
                    vo.setProductName(product1.getProductName());

                }
            });
            return vos;
        }
        return new ArrayList<>();
    }

    /**
     * 查询全部采购单
     *
     * @return
     */
    public List<PurchaseOrder> listScoped(Wrapper wrapper) {
        return baseMapper.listScoped(wrapper, new DataScope());
    }
}
