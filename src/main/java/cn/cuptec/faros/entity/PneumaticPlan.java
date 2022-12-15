package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 气动设备用户训练计划
 */
@Data
public class PneumaticPlan implements Comparable<PneumaticPlan> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String startWeekTime;//开始周的时间
    private String endWeekTime;//开始周的时间
    private String userId;
    private String idCard;
    private String name;
    private Integer time;
    private LocalDateTime dayTime;//每天的时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;//每天的时间
    private Integer type;
    private Integer planType = 0;
    private Integer sort;
    @TableField(exist = false)
    private List<PneumaticPlan> pneumaticPlanList;
    @TableField(exist = false)
    private List<Integer> deleteIds;

    @Override
    public int compareTo(PneumaticPlan o) {
        return this.sort.compareTo(o.sort);//排序
    }

}
