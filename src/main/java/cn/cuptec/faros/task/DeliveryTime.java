package cn.cuptec.faros.task;

import cn.cuptec.faros.entity.RetrieveOrder;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.ExpressService;
import cn.cuptec.faros.service.RetrieveOrderService;
import cn.cuptec.faros.service.UserOrdertService;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DeliveryTime {
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private ExpressService expressService;

    /**
     * 每天凌晨1点 执行 查看实际发货时间
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cancelTime() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<UserOrder> list = userOrdertService.list(new QueryWrapper<UserOrder>().lambda()
                .ge(UserOrder::getStatus, 3)
                .eq(UserOrder::getTest,0)
                .isNotNull(UserOrder::getDeliverySn)
                .isNull(UserOrder::getLogisticsDeliveryTime));
        if (!CollectionUtils.isEmpty(list)) {
            for (UserOrder userOrder : list) {
                MapExpressTrackVo userOrderMapTrace = expressService.getUserOrderMapTraceNoMessage(userOrder.getId());
                if(userOrderMapTrace!=null){
                    MapExpressTrackVo.ExpressData[] data = userOrderMapTrace.getData();
                    MapExpressTrackVo.ExpressData datum = data[data.length - 1];
                    String time = datum.getTime();
                    LocalDateTime ldt = LocalDateTime.parse(time, df);
                    userOrder.setLogisticsDeliveryTime(ldt);

                }
            }
            userOrdertService.updateBatchById(list);
        }
    }

}
