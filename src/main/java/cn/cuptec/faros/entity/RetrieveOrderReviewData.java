package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收单审核内容描述
 */
@Data
public class RetrieveOrderReviewData {
    @TableId
    private Integer id;
    private Integer retrieveOrderId;
    private Integer status;//审核 状态 1-完整 2-损坏
    private String reviewData;//描述
    private LocalDateTime createTime;
}
