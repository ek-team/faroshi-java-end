package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class Brand {

    @Queryable(queryLogical = QueryLogical.EQUAL)
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String brandName;

    private String brandLogo;
    //跳转url
    private String brandUrl;

    private Date createTime;

    //排序
    private Integer sort;

}
