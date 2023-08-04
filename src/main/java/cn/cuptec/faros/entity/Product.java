package cn.cuptec.faros.entity;


import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

//系统产品库
@Data
public class Product {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //产品名称
    @Queryable(queryLogical = QueryLogical.LIKE)
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String videoUrl;
    //产品类型
    private Integer productType;//1院内版 2家庭版 其它
    @TableField(exist = false)
    private String productTypeName;
    //产品主图
    @NotBlank(message = "产品主图不能为空")
    private String productPic;
    //详情图片
    private String detailPic;
    //详情图片价格
    private String detailPicPrice;
    //排序
    private Integer sort;
    //产品分类 1 主产品 2，附属产品
    private Integer category;
    //画册图片，连产品图片限制为5张，以逗号分割
    @JSONField(serialize = false, deserialize = false)
    private String albumPics;

    //产品详情
    private String detailHtml;

    private Integer brandId;

    private Date createDate;

    //产品售价
    private BigDecimal salePrice;

    //删除标记
    @TableLogic
    @JSONField(deserialize = false, serialize = false)
    private String delFlag;

    //画册图片
    @TableField(exist = false)
    private String[] albumPic;

    @TableField(exist = false)
    private List<ProductRetrieveRuleItem> productRetrieveRuleItems;

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

}
