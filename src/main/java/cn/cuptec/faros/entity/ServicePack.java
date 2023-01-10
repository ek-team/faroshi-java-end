package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务包
 */
@Data
public class ServicePack extends Model<ServicePack> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String mpQrCode; //公众号二维码 永久
    private Integer buy; //是否是购买 1
    private Integer rent; //是否是租用 1
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String name;
    private Integer deptId;
    private Integer createUserId;
    private LocalDateTime createTime;
    private String preSaleMobile;//售前手机号
    private String preSaleText;//售前手机号提示文本
    private String afterSaleMobile;//售后手机号
    private String afterSaleText;//售后手机号提示文本
    private String introductionsImage;//服务简介图片
    private String introductionsContent;//服务简介富文本
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer hospitalId;//医院id
    @TableField(exist = false)
    private String hospitalName;//医院名称
    private String productName;//产品名称
    private Double productPrice;//产品购买价格
    @TableField(exist = false)
    private List<ServicePackProductPic> servicePackProductPics;//产品图片
    @TableField(exist = false)
    private List<Integer> productSpecs;//产品规格
    @TableField(exist = false)
    private List<ProductSpec> productSpec;//产品规格

    @TableField(exist = false)
    private List<SaleSpec> saleSpec;//租用规格
    @TableField(exist = false)
    private List<SaleSpec> buySaleSpec;//购买规格
    @TableField(exist = false)
    private List<ServicePackageInfo> servicePackageInfos;//服务信息
    private Integer showIntroduction;//是否展示服务简介 0-不展示 1-展示
    @TableField(exist = false)
    private List<Introduction> introductions;//服务简介数据 弃用
    private String width;
    @TableField(exist = false)
    private List<ServicePackDetail> servicePackDetails;//服务详情
    private Integer protocolType;//1-勾选即可 2-弹窗阅读至底部
    private Integer protocolId;
}
