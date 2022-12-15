package cn.cuptec.faros.entity;

import cn.hutool.db.DaoTemplate;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/16 11:54
 */
@Data
@TableName("activation_code_record")
public class ActivationCodeRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    //设备id
    private Integer productStockId;
    //创建人id
    private Integer createId;
    //创建人昵称
    @TableField(exist = false)
    private String createName;
    //激活码
    private String code;
    //记录创建时间
    private Date createDate;
    //激活码结束时间
    private LocalDate codeEndTime;
    //激活码开始时间
    private LocalDate codeStartTime;
}
