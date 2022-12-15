package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调查问卷答案
 */
@Data
public class Questionnaire {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer answer;//答案下标
    private Integer sort;//题目排序
    private String userId;//用户id
    private Integer groupId;//卷子id
    private LocalDateTime createTime;
}
