package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 设备订单数
 */
@Data
public class Rehabilitation {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer airTrainOrder; //气动训练订单数量
    @TableField(exist = false)
    private String airTrainMacAdd;//气动mac地址
    private Integer lineTrainOrder;//线驱订单数量
    private Integer balanceTrainOrder;// 下肢订单数量
    @TableField(exist = false)
    private String balanceMacAdd;//下肢mac地址
    private Integer limbTrainOrder;//  床旁下肢订单数量
    private Integer swallowTrainOrder;//吞咽订单数量
    private Integer skullTrainOrder;// 经颅磁治疗订单数量
    private Integer electricalTrainOrder;//电刺激订单数量

}
