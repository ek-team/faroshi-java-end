package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.utils.DateTimeUtil;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.RetrieveOrderMapper;
import cn.cuptec.faros.vo.RetrieveOrderCountVo;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RetrieveOrderService extends ServiceImpl<RetrieveOrderMapper, RetrieveOrder> {

    @Resource
    private ProductStockService productStockService;
    @Resource
    private UserService userService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private MobileService mobileService;
    @Resource
    private SaleSpecGroupService saleSpecGroupService;
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private SaleSpecService saleSpecService;

    @Resource
    private WxMpService wxMpService;

    @Transactional
    public boolean saveRetrieveOrder(RetrieveOrder entity) {
        UserOrder userOrder = userOrdertService.getById(entity.getOrderId());
        userOrder.setStatus(5);
        userOrdertService.updateById(userOrder);
        List<ServicePackProductPic> list = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                .eq(ServicePackProductPic::getServicePackId, userOrder.getServicePackId()));
        entity.setProductPic(list.get(0).getImage());
        entity.setSaleSpecId(entity.getSaleSpecId());

        SaleSpecGroup saleSpecGroup = saleSpecGroupService.getOne(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getQuerySaleSpecIds, userOrder.getQuerySaleSpecIds()));
        entity.setRetrieveAmount(new BigDecimal(saleSpecGroup.getRecoveryPrice()));//回收价格
        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());

        entity.setProductName(servicePack.getName());
        entity.setDeptId(userOrder.getDeptId());
        entity.setSaleSpecId(userOrder.getSaleSpecId());
        entity.setProductSpec(userOrder.getProductSpec());
        entity.setServicePackId(userOrder.getServicePackId());
        super.save(entity);

        return Boolean.TRUE;
    }

    public IPage pageRetrieveOrder(IPage page, Wrapper wrapper) {
        return baseMapper.pageRetrieveOrder(page, wrapper);
    }

    public IPage pageScoped(IPage page, Wrapper wrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        return baseMapper.pageScoped(page, wrapper, dataScope);
    }

    public RetrieveOrderCountVo countScoped() {
        List<RetrieveOrder> tbUserOrders = baseMapper.listScoped(Wrappers.<RetrieveOrder>lambdaQuery(), new DataScope());
        RetrieveOrderCountVo vo = new RetrieveOrderCountVo();
        if (!CollectionUtils.isEmpty(tbUserOrders)) {
            long count0 = tbUserOrders.stream().filter(it -> it.getStatus()!=null && it.getStatus() == 0).count();
            long count9 = tbUserOrders.stream().filter(it -> it.getStatus()!=null && it.getStatus() == 9).count();

            vo.setStatu0(count0);
            vo.setStatu9(count9);

        }
        return vo;
    }

    @Transactional
    public void confirmRetrieved(int id) {
        RetrieveOrder retrieveOrder = getById(id);
        Assert.isTrue(retrieveOrder.getStatus() == 3 || retrieveOrder.getStatus() == 2, "数据状态不匹配，请刷新后重试，或联系管理员");
        ProductStock productStock = productStockService.getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getProductSn, retrieveOrder.getProductSn()));
        if (productStock != null) {
            Assert.isTrue(productStock.getStatus() == 31, "设备状态非回收中，请刷新后重试，或联系管理员");
            productStock.setStatus(20); //状态设置为业务库存
            productStockService.updateById(productStock);
            productStockService.clearOrderUid(productStock.getId());
        }

        retrieveOrder.setStatus(9);
        retrieveOrder.setConfirmRevMoneyTime(new Date());
        updateById(retrieveOrder);
    }

    //回收单发货
    public void confirmDelivery(int id, String deliveryCompanyCode, String deliveryCompanyName, String deliverySn) {
        RetrieveOrder retrieveOrder = super.getById(id);
        Assert.isTrue(retrieveOrder != null, "回收单不存在");
        Assert.isTrue(retrieveOrder.getStatus().intValue() == 0, "订单非待邮寄状态");
        retrieveOrder.setStatus(1);
        retrieveOrder.setDeliveryCompanyCode(deliveryCompanyCode);
        retrieveOrder.setDeliveryCompanyName(deliveryCompanyName);
        retrieveOrder.setDeliverySn(deliverySn);
        retrieveOrder.setDeliveryTime(new Date());
        super.updateById(retrieveOrder);
    }

    //确认设备已收到
    public void confirmReceived(int id) {
        RetrieveOrder retrieveOrder = super.getById(id);
        Assert.isTrue(retrieveOrder != null, "回收单不存在");
        Assert.isTrue(retrieveOrder.getStatus().intValue() == 1, "非待收货状态回收单");
        retrieveOrder.setStatus(2);
        retrieveOrder.setReceieveTime(new Date());

//        if (cn.cuptec.faros.common.utils.StringUtils.isNotEmpty(confirmReceivedParam.getPicUrls())) {
//            List<String> picUrls = confirmReceivedParam.getPicUrls();
//            retrieveOrder.setAlbumPic(new String[picUrls.size()]);
//        }

        super.updateById(retrieveOrder);
    }

    //确认打款了
    public void confirmPostMoney(int id) {
        RetrieveOrder retrieveOrder = super.getById(id);
        Assert.isTrue(retrieveOrder != null, "回收单不存在");
        Assert.isTrue(retrieveOrder.getStatus().intValue() == 2, "非待打款状态回收单");
        retrieveOrder.setStatus(3);
        retrieveOrder.setConfirmPostMoneyTime(new Date());
        if (retrieveOrder.getOrderId() != null) {
            UserOrder byId = userOrdertService.getById(retrieveOrder.getOrderId());
            if (byId != null && byId.getReceiverPhone() != null) {
                //发送短信
                try {
                    mobileService.sendRetrievedMoneySms(byId.getReceiverPhone());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        super.updateById(retrieveOrder);
    }

    //修改回收价格
    public void modifyRetrieveAmount(int id, BigDecimal actualRetriieveAmount) {
        RetrieveOrder retrieveOrder = super.getById(id);
        Assert.isTrue(retrieveOrder != null, "回收单不存在");
        Assert.isTrue(retrieveOrder.getStatus().intValue() == 2, "回收单不是待打款状态，不可更改回收价格");
        retrieveOrder.setActualRetrieveAmount(actualRetriieveAmount);
        retrieveOrder.setConfirmRetrieveAmountStatu(false);
        retrieveOrder.setStatus(4);
        super.updateById(retrieveOrder);


    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmRetrieveAmount(int id) {
        this.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                .set(RetrieveOrder::getStatus, 2)
                .eq(RetrieveOrder::getId, id)
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
        );
        RetrieveOrder retrieveOrder = this.getById(id);
        //发送公众号消息提醒 业务员
        User user = userService.getById(retrieveOrder.getSalesmanId());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                String url = "http://pharos.ewj100.com/index.html#/salesmanOption/retrieveOrderDetail/" + retrieveOrder.getId();
                wxMpService.sendTopic(user.getMpOpenId(), "订单状态变更", localTime, "用户确认订单回收价格", "用户确认订单回收价格", url);
            }
        }

    }

    public IPage<RetrieveOrder> canRefundList(IPage<RetrieveOrder> page) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        if (byId == null || byId.getDeptId() == null)
            return AbstractBaseController.getNullablePage();
        IPage<RetrieveOrder> retrieveOrderIPage = this.baseMapper.canRefundList(page, byId.getDeptId());
        if (CollUtil.isNotEmpty(retrieveOrderIPage.getRecords())) {
            List<RetrieveOrder> records = retrieveOrderIPage.getRecords();
            List<Integer> collect = records.stream().map(RetrieveOrder::getSalesmanId).collect(Collectors.toList());
            Collection<User> users = userService.listByIds(collect);
            if (CollUtil.isNotEmpty(users)) {
                for (RetrieveOrder retrieveOrder : records) {
                    for (User user : users) {
                        if (retrieveOrder.getSalesmanId().equals(user.getId())) {
                            retrieveOrder.setSalesmanName(user.getNickname());
                        }
                    }
                }
            }

        }


        return retrieveOrderIPage;
    }
}
