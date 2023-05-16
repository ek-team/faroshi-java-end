package cn.cuptec.faros.task;

import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.UserOrdertService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 服务周期使用通知
 */
@Component
public class ServiceNotice {
//    @Resource
//    private UserOrdertService userOrdertService;
//
//    @Scheduled(cron = "0 15 01 L-3 * ?") //每月最后3天执行
//    public void notice1() {
//        List<UserOrder> list = userOrdertService.list(new QueryWrapper<UserOrder>().lambda().gt(UserOrder::getStatus, 1)
//                .lt(UserOrder::getStatus, 5));
//        if(!CollectionUtils.isEmpty(list)){
//            for(UserOrder userOrder:list){
//                Integer orderType = userOrder.getOrderType();//1-租用 2-购买
//                if(orderType.equals(1)){//租赁和订单，从设备发货，确定“开始使用时间”
//                    Date deliveryTime = userOrder.getDeliveryTime();
//                    if(deliveryTime!=null){
//                        Date date = new Date();
//
//                        long beforeTime = deliveryTime.getTime();
//                        long dateNextTime = date.getTime();
//                        long delta = dateNextTime - beforeTime;
//                        long day = delta / (24 * 60 * 60 * 1000);
//                        if(day<30){
//
//                        }
//                        if(day<60){
//
//                        }
//                        if(day<90){
//
//                        }
//                    }
//
//                }else{
//                    //购买的订单，从微信训练小程序第一次提交评估、训练记录，确定“开始使用时间”
//                }
//            }
//
//
//        }
//    }
//    @Scheduled(cron = "0 15 01 L-5 * ?") //每月最后5天执行
//    public void notice2() {
//        List<UserOrder> list = userOrdertService.list(new QueryWrapper<UserOrder>().lambda().gt(UserOrder::getStatus, 1)
//                .lt(UserOrder::getStatus, 5));
//        if(CollectionUtils.isEmpty(list)){
//
//
//
//        }
//    }
//    @Scheduled(cron = "0 15 01 L-7 * ?") //每月最后7天执行
//    public void notice3() {
//        List<UserOrder> list = userOrdertService.list(new QueryWrapper<UserOrder>().lambda().gt(UserOrder::getStatus, 1)
//                .lt(UserOrder::getStatus, 5));
//        if(CollectionUtils.isEmpty(list)){
//
//
//
//        }
//    }
}
