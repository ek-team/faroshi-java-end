package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 气动评估记录
 */
@Data
public class PneumaticEvaluationRecords extends Model<PneumaticEvaluationRecords> implements Comparable<PneumaticEvaluationRecords> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private LocalDateTime createTime;

    private String userId;            //用户唯一ID
    private String keyId;             //唯一ID
    private int  type; //评估类型  默认0
    private int grade;//级别   0-5级别
    private String updateTime;//更新时间
    private int hurt; //患侧手  左手0 右手1
    private int handMovement;//评测姿势  握拳0 伸直1
    private int healthyHand;//评测手 健康手0  患侧手1
    private String remark;//备注信息
    @TableField(exist = false)
    private List<String> airList; //气压记录（100组数据）
    @TableField(exist = false)
    private  List<PneumaticEvaluationRecordsAirList> airLists;

    @Override
    public int compareTo(PneumaticEvaluationRecords o) {
        return this.updateTime.compareTo(o.updateTime);//根据时间降序
    }
}
