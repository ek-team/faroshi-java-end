package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议
 */
@Data
public class Protocol {

    @TableId
    private Integer id;

    private String name;

    private String content;

    private int createUserId;
    public Integer isDefault;//1-默认 0-不默认
    private Integer deptId;
    private LocalDateTime createDate;
}
