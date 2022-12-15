package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/23 13:13
 */
@Data
@TableName("`open_id_user_relation`")
public class OpenIdUserRelation {
    @TableId(type = IdType.AUTO)
    private String id;

    private String openId;
    private int uid;
    private String mpAppId;
}
