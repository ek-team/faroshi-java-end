package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户填写申请开发票实体类
 */
@Data
public class Bill extends Model<Bill> {

    @TableId(type = IdType.AUTO)
    private String id;
    private Integer userId;
    private String orderNo;
    private Integer category;//0-个人 1-公司
    private String name;//接受人
    private String phone;//联系电话
    private String email;//电子邮件
    private String remark;//发票备注
    private String company;//发票抬头
    private String taxNumber;//税号
    private Double amount;//发票金额
    private LocalDateTime createTime;
}
