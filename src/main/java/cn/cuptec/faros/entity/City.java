package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 城市
 */
@Data
public class City implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    //城市名称
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String name;

    //父级id
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer parentId;

    //城市简称
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String shortName;

    //城市全称
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String fullName;

    //城市级别 0-国 1-省 2-市 3-区
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer levelType;

    //邮政编码
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String phoneCode;

    //城市编码
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String code;

    //经度
    private Double lng;

    //纬度
    private Double lat;

    //城市全拼
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String pinyin;

    //首字母
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String firstChar;

    //是否是热门城市
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Boolean isHot;

    @TableField(exist = false)
    private List<City> subModelList;

}
