package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "icons")
public class Icons extends Model<Icons> implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String url;
}
