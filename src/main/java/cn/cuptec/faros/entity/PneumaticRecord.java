package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 气动训练记录表
 */
@Data
public class PneumaticRecord  implements Comparable<PneumaticRecord> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userId;//用户唯一id 静态方法获取唯一id编号
    private String idCard;//身份证
    private Integer count;
    private String keyId;
    private String planDayTime;//训练日期
    private String planId;
    private String planName;// 训练名称
    private Integer planTime;//用户计划训练时间
    private Integer planTimeDone;//用户已完成时间
    private Integer type;//训练类型
    private String macAdd;
    private LocalDateTime updateTime;//上传时间
    @TableField(exist = false)
    private String userName;//设备用户名字
    @Override
    public int compareTo(PneumaticRecord o) {
        if(o.getUpdateTime()!=null && this.updateTime!=null){
            return o.getUpdateTime().compareTo(this.updateTime);//根据时间降序
        }else {
            return o.getPlanDayTime().compareTo(this.planDayTime);//根据时间降序
        }

    }
}
