package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备回收订单
 */
@Data
public class RetrieveOrder {

    @TableId
    private Integer id;
    @TableField(exist = false)
    private String reviewData;//回收单厂家审核信息
    private String deliveryName;//发货人姓名
    private String deliveryPhone;//发货人手机号
    private String deliveryAddress;//发货人地址
    private String orderId;
    private String naLiOrderId;
    private String orderNo;
    //服务包id
    private Integer servicePackId;
    private Integer saleSpecId;//销售规格id
    //设备是否能开机
    private Boolean isCanOpen;

    //设备外观是否正常 1-完好 2-轻微损坏 3-严重损坏
    private Integer coverStatu;
    private String setMealName;
    //配件是否完整
    private Boolean isPjwz;
    //预约送货时间
    private Date deliveryDate;
    //设备外观图
    private String albumPics;
    private Date automaticCreateTime;
    private String productSn;

    private String productName;

    private String productPic;

    private Integer userId;

    private Integer deptId;

    private Integer salesmanId;

    private Date createTime;

    //发货时间
    private Date deliveryTime;

    //收到或的时间
    private Date receieveTime;

    //快递公司编码
    private String deliveryCompanyCode;

    //送货方式 1快递2自送
    private int deliveryMethod = 1;

    //快递公司名称
    private String deliveryCompanyName;

    private String deliverySn;

    //打款事件
    private Date confirmPostMoneyTime;

    //确认收款时间
    private Date confirmRevMoneyTime;
    //状态 0-待邮寄 1-待收货 2-待审核 3-待打款 4-待收款 5-回收完成
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;

    //回收价格
    private BigDecimal retrieveAmount;
    //建议扣款金额
    private BigDecimal deductionAmount;

    //实际回收价格
    private BigDecimal actualRetrieveAmount;

    //回收价格用户确认状态 true-用户已确认/暂不需要确认  false-需要用户确认
    private Boolean confirmRetrieveAmountStatu;

    //收货人姓名
    private String receiverName;

    //收货人电话
    private String receiverPhone;

    //收货人省市区地址
    private String receiverRegion;
    //用户银行卡号
    private String bankCard;
    //用户开户行
    private String accountBank;
    //用户名
    private String userName;
    @TableField(exist = false)
    private String patientUserName;
    //收货人详细地址
    private String receiverDetailAddress;

    private String postName;

    private String postPhone;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String salesmanName;
    @TableField(exist = false)
    private ServicePack servicePack;//服务包信息
    @TableField(exist = false)
    private SaleSpec saleSpec;//销售规格信息
    @TableField(exist = false)
    private String salesmanPhone;

    //设备外观图
    @TableField(exist = false)
    private String[] albumPic;

    //region
    public String[] getAlbumPic() {
        if (StringUtils.isNotEmpty(this.albumPics))
            return this.albumPics.split(CommonConstants.VALUE_SEPARATOR);
        return new String[]{};
    }

    public void setAlbumPic(String[] albumPic) {
        this.albumPic = albumPic;
        albumPics = StringUtils.join(albumPic, CommonConstants.VALUE_SEPARATOR);
    }

    //endregion
    //打款方式
    @TableField(exist = false)
    private Integer[] paymentMethod;

}
