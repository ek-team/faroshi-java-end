package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.DateTimeUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 活码
 */
@Data
public class LiveQrCode {

    @Queryable(queryLogical = QueryLogical.EQUAL)
    @TableId(type = IdType.UUID)
    private String id;

    /**
     * 活码类型 1-产品二维码 2-销售二维码 3-自定义二维码 4-医生信息编辑二维码 5 -纳里二维码
     * 6 -纳里二维码配置   7 -红小豆二维码 8 -红小豆二维码配置
     */
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer type;
    //跳转url
    private String url;
    //名称
    private String urlName;
    //名称
    private String name;
    //图标
    private String icon;

    //二维码图片
    private String qrCodePic;
    //是否已绑定
    @TableField(exist = false)
    private Boolean bindStatus;
    //已绑定的产品序列号
    @TableField(exist = false)
    private String productSn;

    //已绑定的产品序列号
    @TableField(exist = false)
    private String macAddress;
    @TableField(exist = false)
    private String currentUserId;
    @TableField(exist = false)
    private String ipAdd;
    //设备生产日期
    @TableField(exist = false)
    private Date productProductionDate;
    //销售员id
    private Integer salesmanId;
    private Integer productId;

    //当前设备属于哪个部门
    private Integer deptId;
    private String servicePackId;
    private String hospitalName;
    @TableField(exist = false)
    private String versionStr;
    @TableField(exist = false)
    private String iccId;
    //系统版本号
    @TableField(exist = false)
    private String systemVersion;
    @TableField(exist = false)
    private ProductStock productStock;






//============================================设备信息



    @TableField(exist = false)
    private Integer productStockId;
    @TableField(exist = false)
    private String targetMacAdd;

    @TableField(exist = false)
    private String productDeviceType;
    @TableField(exist = false)
    private String productSerialNumber;
    //医院id
    @TableField(exist = false)
    private Integer hospitalId;


    @TableField(exist = false)
    private int productLockNum;

    //产品激活码
    @TableField(exist = false)
    private String activationCode;
    //产品激活的结束日期
    @TableField(exist = false)
    private LocalDate activationDate;
    //1:正常 2-删除
    @TableField(exist = false)
    private Integer del = 1;
    //合同日期
    @TableField(exist = false)
    private LocalDateTime contractDate;
    //产品唯一码
    @TableField(exist = false)
    private String liveQrCodeId;
    @TableField(exist = false)
    private Integer upload = 0;//0-未上传备份数据 1-已上传备份数据
    //纳你二维码地址
    @TableField(exist = false)
    private String naniQrCodeUrl;
    @TableField(exist = false)
    private String locatorName;
    //当前设备在哪个仓库
    @TableField(exist = false)
    private Integer locatorId;


    //当前产品属于哪个销售 名字
    @TableField(exist = false)
    private String salesmanName;
    //当前产品在哪个终端用户手里 为空表示未绑定
    @TableField(exist = false)
    private Integer userId;
    @TableField(exist = false)
    private String userName;
    //最后一次绑定用户的时间，即当前用户购买时间
    @TableField(exist = false)
    private Date lastBindUserTime;
    @TableField(exist = false)
    private Integer version; //升级版本号
    // 产品状态
    @TableField(exist = false)
    private Integer status;

    //创建时间(即生产日期)
    @TableField(exist = false)
    private Date createDate;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private Integer productType;

    @TableField(exist = false)
    private String productPic;

    @TableField(exist = false)
    private Object productInfo;

    @TableField(exist = false)
    private DateTimeUtil.YearMonthDayCount yearMonthDayCount;

}
