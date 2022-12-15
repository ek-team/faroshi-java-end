package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 服务简介
 */
@Data
public class Introduction {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer servicePackId;
    private String image;
    private String url;
}
