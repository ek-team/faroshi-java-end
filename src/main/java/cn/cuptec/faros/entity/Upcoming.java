package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待办事项
 */
@Data
public class Upcoming extends Model<Upcoming> {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer doctorId;
    private String title;

    private String content;
    private LocalDateTime createTime;
    private int redStatus=0;//0-未读 1-已读
    @TableField(exist = false)
    private User user;
    private String type;//待办类型1-新增患者 2-图文咨询 3-新消息
}
