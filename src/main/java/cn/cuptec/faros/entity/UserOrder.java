package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private Integer chatUserId;//聊天id
    private String remark;//备注
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer orderType;//订单类型 1-租用 2-购买
    @TableField(exist = false)
    private String patientUserName;//就诊人名称
    @TableField(exist = false)
    private List<ServicePackageInfo> servicePackageInfos;//赠送的服务信息
    @TableField(exist = false)
    private PatientUser patientUser;
    @TableField(exist = false)
    private List<UserServicePackageInfo> userServicePackageInfos;//服务信息
    @TableField(exist = false)
    private Integer isForm;//判断订单是否有表单
    @TableField(exist = false)
    private ServicePack servicePack;//服务包信息
    @TableField(exist = false)
    private SaleSpec saleSpec;//销售规格信息

    //服务包id
    private Integer servicePackId;
    private String transactionId;
    private Integer doctorId;//支付成功随机选择的医生id
    private Integer formId;//表单id
    private Integer doctorTeamId;//服务团队id
    private Integer saleSpecId;//销售规格id
    private String productSpec;//选择的产品规格信息 字符串拼接多个
    private Integer addressId;//收货人地址id
    //快递单号
    private String deliveryNumber;
    //收货人姓名
    private String receiverName;
    //收货人电话
    private String receiverPhone;
    //收货人省市区地址
    private String receiverRegion;
    @TableField(exist = false)
    public String city;//市
    @TableField(exist = false)
    public String area;//区
    @TableField(exist = false)
    public String province;//省
    //收货人详细地址
    private String receiverDetailAddress;
    //期望送货时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    private LocalDateTime payTime;//支付时间
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
