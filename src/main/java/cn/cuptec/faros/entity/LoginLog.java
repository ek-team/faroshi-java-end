package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String data;
    private LocalDateTime createTime;
}
