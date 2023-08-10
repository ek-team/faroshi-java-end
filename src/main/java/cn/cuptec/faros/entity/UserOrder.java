package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer test = 0;//0-非测试订单 1-测试订单
    private LocalDateTime moveTime;//运行时间
    private String taskId;//快递任务id
    private String message;//快递下单返回错误信息
    private String label;//快递面单链接
    private Integer payType;//1-微信 2-支付宝
    private Integer billId;
    private String saleSpecDescIdList;
    private Integer userServicePackageInfoId;
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String orderNo;
    private Integer patientUserId;//就诊人id
    private String querySaleSpecIds;//查询规格值
    private Double saleSpecRecoveryPrice;//规格值回收价格
    private String recyclingRuleList;//回收规则计算
    private Integer saleSpecServiceEndTime;//订单的服务结束时间 取自 规格里面的服务周期 使用周期
    private Integer chatUserId;//聊天id
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime operationTime;//手术时间
    private Integer diseasesId;//病种id
    private String remark;//备注
    private int rentDay = 1;//租用天数
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer orderType;//订单类型 1-租用 2-购买
    @TableField(exist = false)
    private String patientUserName;//就诊人名称
    @TableField(exist = false)
    private List<ServicePackageInfo> servicePackageInfos;//赠送的服务信息
    @TableField(exist = false)
    private PatientUser patientUser;
    @TableField(exist = false)
    private UserServicePackageInfo userServicePackageInfo;//服务信息
    @TableField(exist = false)
    private Integer isForm;//判断订单是否有表单
    @TableField(exist = false)
    private ServicePack servicePack;//服务包信息

    //服务包id
    private Integer servicePackId;
    private String transactionId;
    private Integer doctorId;//支付成功随机选择的医生id
    private Integer formId;//表单id
    private Integer doctorTeamId;//服务团队id
    @TableField(exist = false)
    private String doctorTeamName;//团队名称
    @TableField(exist = false)
    private DoctorTeam doctorTeam;//团队
    private String saleSpecId;//销售规格id
    @TableField(exist = false)
    private List<Integer> saleSpecDescIds;
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private SaleSpecGroup saleSpecGroup;
    @TableField(exist = false)
    private List<RentRuleOrder> rentRuleOrderList;
    @TableField(exist = false)
    private String patientUserIdCard;//就诊人身份证号
    @TableField(exist = false)
    private String patientUserPhone;//就诊人手机号
    @TableField(exist = false)
    private Integer serviceCount;//图文咨询次数
    @TableField(exist = false)
    private String hospitalName;//医院名字
    @TableField(exist = false)
    private Bill bill;
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
    public String city;//市
    private String billImage;//发票照片
    public String area;//区
    public String province;//省
    //收货人详细地址
    private String receiverDetailAddress;
    //期望送货时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate deliveryDate;
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

    //订单状态 0-待确认 1-待付款 2-待发货 3-待收货 4-已收货 5-已回收  7-已取消
     //回收单状态 8-回收单待收货 9-回收单待审核 10-回收单待打款 11回收单收款 12-回收单回收完成 13-回收单退款待审核  14-回收单退款拒绝
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

    //物流实际 发货时间
    private LocalDateTime logisticsDeliveryTime;

    //确认收货时间
    private LocalDateTime revTime;
    //产品图片
    private String productPic;

    private Integer useDay;//使用天数

    //实际回收价格  退款金额
    private BigDecimal actualRetrieveAmount;
    private BigDecimal settlementAmount;//结算金额 预付款-退款金额
    private LocalDateTime recycleTime;//回收揽件时间
    private LocalDateTime acceptanceTime;//厂家验收时间
    private LocalDateTime refundInitiationTime;//退款发起时间
    private Integer reviewRefundOrderId;//退款审核记录id
    @TableField(exist = false)
    private ReviewRefundOrder reviewRefundOrder;
    private LocalDateTime refundReviewTime;//平台退款审核时间
    private String productSn1;//设备序列号1
    private String productSn2;//设备序列号2
    private String productSn3;//设备序列号3
    @TableField(exist = false)
    private String url;
}
