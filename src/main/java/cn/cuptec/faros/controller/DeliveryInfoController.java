package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DeliveryInfo;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.DeliveryInfoService;
import cn.cuptec.faros.service.UserOrdertService;
import cn.cuptec.faros.service.UserRoleService;
import cn.cuptec.faros.service.UserService;
import com.alipay.api.domain.ZhimaCreditPeUserOrderSyncModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/deliveryInfo")
public class DeliveryInfoController extends AbstractBaseController<DeliveryInfoService, DeliveryInfo> {
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;

    /**
     * 分页查询运单记录
     */
    @GetMapping("/page")
    public RestResponse pageDeliveryInfo(@RequestParam(value = "deliverySn",required = false) String deliverySn, @RequestParam(value = "userOrderNo",required = false)
            String userOrderNo, @RequestParam(value = "receiverName",required = false) String receiverName) {
        Page<DeliveryInfo> page = getPage();
        LambdaQueryWrapper<DeliveryInfo> lambda = new QueryWrapper<DeliveryInfo>().lambda();
        User userDept = userService.getById(SecurityUtils.getUser().getId());
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(userDept.getId());
        if (!aBoolean) {
            lambda.eq(DeliveryInfo::getDeptId, userDept.getDeptId());

        }
        if (!StringUtils.isEmpty(deliverySn)) {
            lambda.like(DeliveryInfo::getDeliverySn, deliverySn);

        }
        if (!StringUtils.isEmpty(userOrderNo)) {
            lambda.like(DeliveryInfo::getUserOrderNo, userOrderNo);
        }
        if (!StringUtils.isEmpty(receiverName)) {
            List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().lambda()
                    .like(UserOrder::getReceiverName, receiverName));
            if (!CollectionUtils.isEmpty(userOrders)) {
                List<String> userOrderNos = userOrders.stream().map(UserOrder::getOrderNo)
                        .collect(Collectors.toList());
                lambda.in(DeliveryInfo::getUserOrderNo, userOrderNos);
            }

        }
        IPage<DeliveryInfo> page1 = service.page(page,lambda);
        List<DeliveryInfo> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<String> userOrderNos = records.stream().map(DeliveryInfo::getUserOrderNo)
                    .collect(Collectors.toList());
            List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().lambda()
                    .in(UserOrder::getOrderNo, userOrderNos));
            Map<String, UserOrder> orderMap = userOrders.stream()
                    .collect(Collectors.toMap(UserOrder::getOrderNo, t -> t));
            for (DeliveryInfo deliveryInfo : records) {
                deliveryInfo.setUserOrder(orderMap.get(deliveryInfo.getUserOrderNo()));
            }
        }
        return RestResponse.ok(page1);

    }


    @Override
    protected Class<DeliveryInfo> getEntityClass() {
        return DeliveryInfo.class;
    }
}
