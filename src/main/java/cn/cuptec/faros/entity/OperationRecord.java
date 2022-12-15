package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作记录
 */
@Data
@TableName("operation_record")
public class OperationRecord {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String pathUrl;
    private Date createTime;
    private String userId;
    private String productStockId;
    private String macAdd;
    private String oldMacAdd;
    private String productSn;
    private String text;
    @TableField(exist = false)
    private String userName;
}
