package cn.cuptec.faros.task;

import cn.cuptec.faros.entity.RetrieveOrder;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.RetrieveOrderService;
import cn.cuptec.faros.service.UserOrdertService;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @Description:
 * @Author mby 预售单和回收单显示自动结束时间
 * @Date 2021/8/5 17:35
 */
@Slf4j
@Component
public class OrderEndTime {

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusDays(7);
        System.out.println(localDateTime);
    }

    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private RetrieveOrderService retrieveOrderService;

    @SneakyThrows
    public String initUserOrderEndTime(String day) {
        Integer days = Integer.valueOf(day);
        //预售单在超过规定时间自动更新状态
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusDays(days);
        LambdaQueryWrapper<UserOrder> lambdaQueryUserOrder = new LambdaQueryWrapper();
        lambdaQueryUserOrder.eq(UserOrder::getStatus, 3);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strEnd = df.format(localDateTime);

        lambdaQueryUserOrder.apply("UNIX_TIMESTAMP(automatic_create_time) <= UNIX_TIMESTAMP('" + strEnd + "')");


        List<UserOrder> userOrders = userOrdertService.list(lambdaQueryUserOrder);
        if (!CollectionUtils.isEmpty(userOrders)) {
            for (UserOrder userOrder : userOrders) {
                userOrder.setStatus(4);
            }
            //批量更改状态
            userOrdertService.updateBatchById(userOrders);
        }

        return "0";
    }

    @SneakyThrows
    public String initRetrieveEndTime(String day) {
        Integer days = Integer.valueOf(day);
        //预售单在超过规定时间自动更新状态
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusDays(days);
        LambdaQueryWrapper<UserOrder> lambdaQueryUserOrder = new LambdaQueryWrapper();
        lambdaQueryUserOrder.eq(UserOrder::getStatus, 3);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strEnd = df.format(localDateTime);

        //回收单在超过规定时间自动更新状态
        LambdaQueryWrapper<RetrieveOrder> lambdaQueryRetrieveOrder = new LambdaQueryWrapper();
        lambdaQueryRetrieveOrder.eq(RetrieveOrder::getStatus, 1);

        lambdaQueryRetrieveOrder.apply("UNIX_TIMESTAMP(automatic_create_time) <= UNIX_TIMESTAMP('" + strEnd + "')");

        List<RetrieveOrder> retrieveOrders = retrieveOrderService.list(lambdaQueryRetrieveOrder);
        if (!CollectionUtils.isEmpty(retrieveOrders)) {
            for (RetrieveOrder retrieveOrder : retrieveOrders) {
                retrieveOrder.setStatus(2);
            }
            //批量更改状态
            retrieveOrderService.updateBatchById(retrieveOrders);
        }

        return "0";
    }

//    private void userOrderTask(List<UserOrder> userOrders, String day) {
//        Integer days = Integer.valueOf(day);
//        //预售单在超过规定时间自动更新状态
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime localDateTime = now.minusDays(days);
//
//        Long diffTime = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
//        Queue<UserOrder> orderQueue = new LinkedList<>();
//        if (userOrders != null && !userOrders.isEmpty()) {
//            userOrders.forEach(userOrder -> {
//                orderQueue.offer(userOrder);
//            });
//            UserOrder sysOrder = orderQueue.peek();
//            while (sysOrder != null) {
//                Long diff = sysOrder.getAutomaticCreateTime().getTime();
//                //如果订单超时，取消订单
//                if (diff <= diffTime) {
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//                    String currentTimeStr = df.format(new Date()).toString();
//                    log.info(currentTimeStr + "订单超时，即将关闭订单。订单id:" + sysOrder.getOrderNo());
//                    sysOrder.setStatus(4);
//                    userOrdertService.updateById(sysOrder);
//                    orderQueue.poll();//弹出队列
//                    sysOrder = orderQueue.peek();
//                } else if (diff > diffTime) {
//                    //线程等待
//                    try {
//                        Thread.sleep(diff - diffTime);
//                    } catch (InterruptedException e) {
//                        log.error("等待检测未付款订单的线程异常停止，{}", e);
//                    }
//                }
//            }
//        }
//
//
//    }
//

    private void retrieveOrderTask(List<RetrieveOrder> retrieveOrders, String day) {
        Integer days = Integer.valueOf(day);
        //回收单在超过规定时间自动更新状态
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusDays(days);

        Long diffTime = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        Queue<RetrieveOrder> orderQueue = new LinkedList<>();
        if (retrieveOrders != null && !retrieveOrders.isEmpty()) {
            retrieveOrders.forEach(userOrder -> {
                orderQueue.offer(userOrder);
            });
            RetrieveOrder sysOrder = orderQueue.peek();
            while (sysOrder != null) {
                Long diff = sysOrder.getAutomaticCreateTime().getTime();
                //如果订单超时，取消订单
                if (diff <= diffTime) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    String currentTimeStr = df.format(new Date()).toString();
                    log.info(currentTimeStr + "订单超时，即将关闭订单。订单id:" + sysOrder.getProductSn());
                    sysOrder.setStatus(4);
                    retrieveOrderService.updateById(sysOrder);
                    orderQueue.poll();//弹出队列
                    sysOrder = orderQueue.peek();
                } else if (diff > diffTime) {
                    //线程等待
                    try {
                        Thread.sleep(diff - diffTime);
                    } catch (InterruptedException e) {
                        log.error("等待检测未付款订单的线程异常停止，{}", e);
                    }
                }
            }
        }


    }
}
