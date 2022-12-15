package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/5 17:59
 */
@Data
public class Config implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer type;

    private Integer dayTime;
}
