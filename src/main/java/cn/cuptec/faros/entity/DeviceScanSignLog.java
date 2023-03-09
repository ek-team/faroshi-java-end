package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
@TableName(value = "device_scan_sign_log")
public class DeviceScanSignLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    private String userId;
    @TableField(exist = false)
    private String userName;
    /**
     * 创建时间
     */

    private Date createTime;

    @Queryable(queryLogical = QueryLogical.LIKE)
    private String macAddress;


    private Integer isDel;

    private static final long serialVersionUID = 1L;
}