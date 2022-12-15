package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 设备版本号apk
 */
@Data
@TableName(value = "device_version")
public class DeviceVersion extends Model<DeviceVersion> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */

    private Date createTime;

    /**
     * 修改时间
     */

    private Date updateTime;


    private Integer userId;


    private String url;

    @Queryable(queryLogical = QueryLogical.LIKE)
    private String version;

    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer type;
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer category; //1-院内版 2-家庭版
    @TableLogic
    @JSONField(deserialize = false, serialize = false)
    private Integer isDel;
}