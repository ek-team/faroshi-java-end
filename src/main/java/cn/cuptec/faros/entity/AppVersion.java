package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class AppVersion {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer apkVersion;
    private String apkUrl;
    private Integer wgtVersion;
    private String wgtUrl;
    private Integer model;
}
