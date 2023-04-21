package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.ReviewRefundOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RetrieveAmountDto {
    private BigDecimal amount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private String reviewData;//设备描述
    private List<ReviewRefundOrder> reviewRefundOrders;//退款审核描述
    private LocalDateTime payTime;
    private Integer rentDay;
}
