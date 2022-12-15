package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.dto.FlittingOrderDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.FlittingOrderMapper;
import cn.cuptec.faros.vo.ProductFlittingStockInfoVo;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FlittingOrderService extends ServiceImpl<FlittingOrderMapper, FlittingOrder> {

    @Resource
    private PurchaseOrdertService purchaseOrdertService;
    @Resource
    private PurchaseOrderItemService purchaseOrderItemService;
    @Resource
    private FlittingOrderItemService flittingOrderItemService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;

    @Resource
    private DeptService deptService;

    @Override
    @Transactional
    public boolean save(FlittingOrder entity) {
        List<FlittingOrderItem> flittingOrderItems = entity.getFlittingOrderItems();
        flittingOrderItems = flittingOrderItems.stream().filter(flittingOrderItem -> flittingOrderItem.getFlitCount()>0).collect(Collectors.toList());

        Assert.notEmpty(flittingOrderItems, "要调拨的产品不能为空");
        Assert.isTrue(entity.getLocatorId() != null, "接收仓库不能为空");
        //校验调拨数量
        List<ProductFlittingStockInfoVo> scopedStockInfos = purchaseOrdertService.getScopedStockInfo();
        flittingOrderItems.forEach( it -> {
            Optional<ProductFlittingStockInfoVo> productFlittingStockInfoVoOptional = scopedStockInfos.stream().filter(info -> info.getProductId() == it.getProductId().intValue()).findFirst();
            Assert.isTrue(productFlittingStockInfoVoOptional.isPresent(), "存在不可调拨产品");
            ProductFlittingStockInfoVo productFlittingStockInfoVo = productFlittingStockInfoVoOptional.get();
            int stockLeft = productFlittingStockInfoVo.getTotalCount() - productFlittingStockInfoVo.getDeliveryCount() - productFlittingStockInfoVo.getLockCount();
            Assert.isTrue(stockLeft >= it.getFlitCount(), "可调拨数量不足");
        });

        entity.setOrderNo(OrderNumberUtil.getLocalTrmSeqNum());
        entity.setCreateBy(SecurityUtils.getUser().getId());
        entity.setDeptId(SecurityUtils.getUser().getDeptId());
        entity.setStatus(0);
        super.save(entity);
        flittingOrderItems.forEach( it -> {
            it.setFlittingOrderId(entity.getId());
        });
        flittingOrderItemService.saveBatch(flittingOrderItems);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        FlittingOrder flittingOrder = super.getById(id);
        Assert.isTrue(flittingOrder.getStatus() == 0, "暂不允许删除");
        super.removeById(id);
        flittingOrderItemService.remove(Wrappers.<FlittingOrderItem>lambdaQuery().eq(FlittingOrderItem::getFlittingOrderId, id));
        return Boolean.TRUE;
    }

    @Override
    public FlittingOrder getById(Serializable id) {
        FlittingOrder flittingOrder = baseMapper.getDetail(id);
        Assert.notNull(flittingOrder, "调拨单不存在");
        List<FlittingOrderItem> flittingOrderItems = flittingOrderItemService.list(Wrappers.<FlittingOrderItem>lambdaQuery().eq(FlittingOrderItem::getFlittingOrderId, flittingOrder.getId()));
        flittingOrder.setFlittingOrderItems(flittingOrderItems);
        return flittingOrder;
    }

    public IPage pageScoped(IPage page, Wrapper wrapper){
        return baseMapper.pageScoped(page, wrapper, new DataScope());
    }

    /**
     * 查询全部数据权限中的调拨单
     * @param wrapper
     * @return
     */
    public List<FlittingOrder> listScoped(Wrapper wrapper){
        DataScope dataScope = new DataScope();
        User byId = userService.getById(SecurityUtils.getUser().getId());
        Boolean isAdmin = userRoleService.judgeUserIsAdmin(byId.getId());


        if(byId ==null ||(!isAdmin && byId.getDeptId() ==null )) return CollUtil.toList();

        Integer deptId =   isAdmin? 1:byId.getDeptId();

        List<Dept> subList = deptService.getSubList(deptId);
        if(isAdmin && CollUtil.isNotEmpty(subList)){
            subList = deptService.getSubList(subList.stream().map(Dept::getId).collect(Collectors.toList()));
        }


        if(CollUtil.isEmpty(subList)) return CollUtil.toList();
        dataScope.setDeptIds(subList.stream().map(Dept::getId).collect(Collectors.toList()));
        return baseMapper.listScoped(wrapper, dataScope);
    }

    /**
     * 调拨发货
     */
    @Transactional
    public void flit(FlittingOrderDTO dto) {
        //todo 此处直接调拨，暂时省略中间发货收货步骤
        Assert.notEmpty(dto.getProductStockIds(), "调拨发货信息不能为空");

        Assert.notNull(dto.getId(), "调拨单不能为空");
        FlittingOrder flittingOrder = super.getById(dto.getId());
        Assert.notNull(flittingOrder, "调拨单不存在");
        Assert.isTrue(flittingOrder.getStatus() == 0 || flittingOrder.getStatus() == 1, "该状态的调拨单无法发货");
        //校验调拨设备数量、产品
        Boolean isAdmin = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
        int status =  isAdmin?10:20;

        List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getStatus,status)
                .in(ProductStock::getId,dto.getProductStockIds()));
        Assert.isTrue(productStocks.size() ==dto.getProductStockIds().size(), "部分调拨设备状态变化，无法调拨，请重新选择调拨设备");

        List<FlittingOrderItem> flittingOrderItems = flittingOrderItemService.list(Wrappers.<FlittingOrderItem>lambdaQuery().eq(FlittingOrderItem::getFlittingOrderId, dto.getId()));
        List<Integer> updateProductStockIds = CollUtil.toList();

        AtomicInteger flittingOrderStatus = new AtomicInteger(20);
        flittingOrderItems.forEach( it -> {
            Integer productId = it.getProductId();
            if(it.getFlitCount().intValue() -it.getAlreadyFlitCount().intValue()>0){
                List<Integer> collect = productStocks.stream().filter(productStock -> productStock.getProductId().intValue() == productId.intValue()).map(ProductStock::getId).collect(Collectors.toList());
                Assert.isTrue(
                        (it.getFlitCount().intValue() -it.getAlreadyFlitCount().intValue()) >= collect.size(),
                        "选择的调拨产品与清单不匹配"
                );
                if(collect.size()>0){
                    updateProductStockIds.addAll(collect);
                    flittingOrderItemService.updateAlreadyFlitCount(it.getId(),collect.size());

                }
                if(it.getFlitCount().intValue() -it.getAlreadyFlitCount().intValue() > collect.size())
                    flittingOrderStatus.set(1);


            }

        });
        Assert.isTrue(updateProductStockIds.size() ==dto.getProductStockIds().size(), "选择的调拨产品与清单不匹配，无法调拨，请重新选择调拨设备");



        flittingOrder.setStatus(flittingOrderStatus.get());
        super.updateById(flittingOrder);

        //减少调拨剩余数量
        List<PurchaseOrder> purchaseOrders = purchaseOrdertService.listScoped(Wrappers.<PurchaseOrder>lambdaQuery().eq(PurchaseOrder::getStatus, 10));
        List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemService.list(Wrappers.<PurchaseOrderItem>lambdaQuery()
                .in(PurchaseOrderItem::getPurchaseOrderId, purchaseOrders.stream().map(PurchaseOrder::getId).collect(Collectors.toList()))
        );

        Set<Integer> productIdSet = productStocks.stream().map(ProductStock::getProductId).collect(Collectors.toSet());
        productIdSet.forEach(productId -> {
            int productIdCount = ((Long)productStocks.stream().filter(productStock -> productId.equals(productStock.getProductId())).count()).intValue();
            for(int i = 0 ; i < purchaseOrderItems.size(); i++){
                PurchaseOrderItem purchaseOrderItem = purchaseOrderItems.get(i);
                if (purchaseOrderItem.getProductId().intValue() == productId.intValue()){
                    int leftCount = purchaseOrderItem.getPurchaseCount() - purchaseOrderItem.getLockCount() - purchaseOrderItem.getDeliveryCount();
                    if(leftCount > 0){
                        if (productIdCount <= leftCount){
                            purchaseOrderItem.setDeliveryCount(purchaseOrderItem.getDeliveryCount() + productIdCount);
                            purchaseOrderItemService.updateById(purchaseOrderItem);
                            break;
                        }
                        else{
                            productIdCount = productIdCount - leftCount;
                            purchaseOrderItem.setDeliveryCount(purchaseOrderItem.getDeliveryCount() + leftCount);
                            purchaseOrderItemService.updateById(purchaseOrderItem);
                            continue;
                        }
                    }
                }
            }
        });



        //更新库存状态

        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<ProductStock>().lambda()
                .in(ProductStock::getId,updateProductStockIds)
                .eq(ProductStock::getStatus,status)
                .set(ProductStock::getStatus,20)
                .set(ProductStock::getSalesmanId,flittingOrder.getCreateBy())
                .set(ProductStock::getDeptId,flittingOrder.getDeptId())
                .set(ProductStock::getLocatorId,flittingOrder.getLocatorId());

        int update = productStockService.getBaseMapper().update(null, updateWrapper);
        Assert.isTrue(update==dto.getProductStockIds().size(),"部分调拨设备状态变化，无法调拨，请重新选择调拨设备");



    }
    @Transactional(rollbackFor = Exception.class)
    public void refuse(FlittingOrder flittingOrder) {

        FlittingOrder byId = super.getById(flittingOrder.getId());
        Assert.notNull(byId,"操作失败，调拨单不存在");
        Assert.isTrue(byId.getStatus() == 0,"操作失败，调拨单不是待发送状态无法修改");

        boolean update = super.update(new UpdateWrapper<FlittingOrder>().lambda().set(FlittingOrder::getStatus, 30).eq(FlittingOrder::getId, flittingOrder.getId()).eq(FlittingOrder::getStatus, 0));
        Assert.isTrue(update,"操作失败，调拨单已被修改，请刷新");
    }
}
