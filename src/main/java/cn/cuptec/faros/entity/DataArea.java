package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class DataArea {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer type;
    @TableField(exist = false)
    private List<TimeData> timeData;
}
