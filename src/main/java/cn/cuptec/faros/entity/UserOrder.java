package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 购买者订单
 */
@Data
public class UserOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Queryable(queryLogical = QueryLogical.LIKE)
    private String orderNo;
    private Integer patientUserId;//就诊人id
    //服务包id
    private Integer servicePackId;
    private String transactionId;
    private Integer doctorId;//支付成功随机选择的医生id
    private Integer formId;//表单id
    private Integer doctorTeamId;//服务团队id
    private Integer saleSpecId;//销售规格id
    private Integer productSpecId;//产品规格id
    //快递单号
    private String deliveryNumber;
    @Queryable(queryLogical = QueryLogical.EQUAL)
    //收货人姓名
    private String receiverName;

    @Queryable(queryLogical = QueryLogical.EQUAL)
    //收货人电话
    private String receiverPhone;
    //收货人省市区地址
    private String receiverRegion;
    //收货人详细地址
    private String receiverDetailAddress;
    //预约送货时间
    private LocalDateTime deliveryDate;
    //发货地址
    private String deliveryAddress;


    //实际付款
    private BigDecimal payment;

    //订单所属部门
    private Integer deptId;

    //销售员id
    private Integer salesmanId;

    //终端用户id
    private Integer userId;

    //订单状态 0-待确认 1-待付款 2-待发货 3-待收货 4-已收货 5-已回收 6-已取消
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;

    //订单生成时间
    private LocalDateTime createTime;

    //订单自动收货时间
    private LocalDateTime automaticCreateTime;

    //确认付款时间
    private Date confirmPayTime;

    //快递公司编码
    private String deliveryCompanyCode;

    //快递公司名称
    private String deliveryCompanyName;

    //快递单号
    private String deliverySn;

    //发货时间
    private Date deliveryTime;

    //确认收货时间
    private LocalDateTime revTime;

}
