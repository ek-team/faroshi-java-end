package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_log")
public class DeviceLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String macAdd;
    private String logUrl;
    private LocalDateTime createTime;
}
