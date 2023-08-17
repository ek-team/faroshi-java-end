package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DataType4 {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String sex;
    private String address;

}
