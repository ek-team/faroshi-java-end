package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.DateTimeUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("product_stock")
public class ProductStock {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String targetMacAdd;
    private String sourceProductSn;
    @Queryable(queryLogical = QueryLogical.EQUAL)
    @NotNull(message = "产品不能为空")
    //产品id
    private Integer productId;
    private Integer tag = 0;//设备标签
    private String ipAdd;
    private String currentUserId;
    private String productDeviceType;
    private String productSerialNumber;
    private String iccId;
    //医院id
    private Integer hospitalId;
    @TableField(exist = false)
    private String hospitalName;
    @Queryable(queryLogical = QueryLogical.LIKE)
    //产品序列号
    private String productSn;
    @TableField(exist = false)
    private int productLockNum;
    @Queryable(queryLogical = QueryLogical.LIKE)
    //mac地址
    private String macAddress;
    //系统版本号
    private String systemVersion;
    //设备生产日期
    private Date productProductionDate;
    //产品激活码
    private String activationCode;
    //产品激活的结束日期
    private LocalDate activationDate;
    //1:正常 2-删除
    private Integer del = 1;
    //合同日期
    private LocalDateTime contractDate;
    //产品唯一码
    private String liveQrCodeId;
    private Integer upload = 0;//0-未上传备份数据 1-已上传备份数据
    //纳你二维码地址
    private String naniQrCodeUrl;
    @TableField(exist = false)
    private String locatorName;
    //当前设备在哪个仓库
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer locatorId;

    //当前设备属于哪个部门
    private Integer deptId;
    private Integer servicePackId;//服务包id
    //当前产品属于哪个销售 为空时未绑定到相关用户
    private Integer salesmanId;
    //当前产品属于哪个销售 名字
    @TableField(exist = false)
    private String salesmanName;
    //当前产品在哪个终端用户手里 为空表示未绑定
    private Integer userId;
    @TableField(exist = false)
    private String userName;
    //最后一次绑定用户的时间，即当前用户购买时间
    private Date lastBindUserTime;


    private String versionStr;
    private Integer version; //升级版本号
    // 产品状态
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;

    //创建时间(即生产日期)
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
    private String urlName;
    @TableField(exist = false)
    private DateTimeUtil.YearMonthDayCount yearMonthDayCount;

//    {
//        label: "生产中",
//                value: 0
//    },
//    {
//        label: "库存",
//                value: 10
//    },
//    {
//        label: "业务销售锁定",
//                value: 11
//    },
//    {
//        label: "业务库存",
//                value: 20
//    },
//    {
//        label: "终端销售锁定",
//                value : 21
//    },
//    {
//        label: "终端销售期",
//                value: 30
//    },
//    {
//        label: "终端回收中",
//                value: 31
//    }

}
