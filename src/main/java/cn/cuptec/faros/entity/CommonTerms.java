package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 常用语
 */
@Data
public class CommonTerms extends Model<CommonTerms> {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String text;
}
