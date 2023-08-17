package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DataType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String type1;
    private String type2;
    private String type3;
    private String type4;
    private String type5;
}
