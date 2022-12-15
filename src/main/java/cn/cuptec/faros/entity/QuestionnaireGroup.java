package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionnaireGroup {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userId;//用户id
    private Integer score;
    private LocalDateTime createTime;
}
