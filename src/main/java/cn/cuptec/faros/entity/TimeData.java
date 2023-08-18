package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TimeData {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String time;
    private String data;
    private Integer dataAreaId;
}
