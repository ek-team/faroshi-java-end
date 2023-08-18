package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 设备用户数量
 */
@Data
public class ProductStockUserCount {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer airTrainCount;//气动设备用户数量
    @TableField(exist = false)
    private String airTrainMacAdd;//气动设备mac地址
    private Integer lineTrainCount;// 线驱设备用户数量
    private Integer balanceTrainCount;// 下肢设备用户数量
    @TableField(exist = false)
    private String balanceMacAdd;// 下肢设备mac地址
    private Integer limbTrainCount;// 床旁下肢用户数量
    private Integer swallowTrainCount;// 吞咽设备用户数量
    private Integer skullTrainCount;//经颅磁治疗设备用户数量
    private Integer electricalTrainCount;// 电刺激设备用户数量
}
