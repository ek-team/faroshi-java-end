package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String id;

    private String userId;

    private String title;

    private String content;
    private LocalDateTime createTime;
    private int redStatus=0;//0-未读 1-已读

    private String type;//待办类型
}
