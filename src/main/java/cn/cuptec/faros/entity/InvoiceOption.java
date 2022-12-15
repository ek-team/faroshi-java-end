package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;


/**
 * 发票设置
 */
@Data
@TableName(value = "invoice_option")
public class InvoiceOption implements Serializable {
    @TableId(value = "dept_id", type = IdType.INPUT)
    private Integer deptId;

    /**
     * 是否打开  0.打开 1.关闭
     */
    @TableField(value = "is_open")
    private Integer isOpen;

    private static final long serialVersionUID = 1L;
}