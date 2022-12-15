package cn.cuptec.faros.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/10 14:25
 */
@Data
public class ProductStockHistory {
    //设备id
    private int productStockId;
    //类型
    private String type;
    //业务员名称
    private String saleName;
    //业务员名称
    private Integer saleId;
    //预约时间
    private Date operateTime;
    //总购买次数
    private int totalBuyNum = 0;
    //总回收次数
    private int totalRetrieveNum = 0;
    //当前使用客户
    private String userName;
    private int userId;
    private int doctorId;
    private int doctorName;
    //设备生产日期
    private Date productStockCreateTime;
    //回收日期
    private Date retrieveTime;
}
