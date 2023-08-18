package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TrainNumber {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer airTrainNumber;//气动训练次数
    @TableField(exist = false)
    private String airTrainMacAdd;//气动mac地址
    private Integer lineTrainNumber;//线驱使用次数

    private Integer balanceTrainNumber;//下肢使用次数
    @TableField(exist = false)
    private String balanceMacAdd;//下肢mac地址
    private Integer limbTrainNumber;//床旁下肢训练次数
    private Integer swallowTrainNumber;//吞咽训练次数
    private Integer skullTrainNumber;//经颅磁治疗次数
    private Integer electricalTrainNumber;//电刺激次数
}
