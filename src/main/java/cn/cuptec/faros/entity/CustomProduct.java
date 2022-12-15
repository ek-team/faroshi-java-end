package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class CustomProduct {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer productId;

    //部门id
    private Integer deptId;

    //创建者id
    private Integer createBy;

    private BigDecimal salePrice;

    //自定义产品详情
    private String detailHtml;

    private Date createTime;

    @TableField(exist = false)
    private String productPic;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private Integer productType;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private List<CustomProductRetrieveRuleItem> customProductRetrieveRuleItems;

}
