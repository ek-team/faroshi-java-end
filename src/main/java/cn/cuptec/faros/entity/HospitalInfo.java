package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医院表
 */
@Data
@TableName(value = "hospital_info")
public class HospitalInfo extends Model<HospitalInfo> implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @JSONField(deserialize = false)
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(deserialize = false)
    private Date updateTime;

    /**
     * 逻辑删除 0.未删 1.已删
     */
    @TableLogic
    @JSONField(deserialize = false, serialize = false)
    private Integer isDel;

    /**
     * 省
     */
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String province;

    /**
     * 市
     */
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String city;

    /**
     * 区
     */
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String area;

    /**
     * 名字
     */

    private String name;
    /**
     * 省市区字符串拼接
     */
    @TableField(exist = false)
    private String str;

    private static final long serialVersionUID = 1L;
    //所在城市编码
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String locatorRegionIds;

    @TableField(exist = false)
    private Integer[] locatorRegions;

    public Integer[] getLocatorRegions() {
        if (StringUtils.isNotEmpty(this.locatorRegionIds)) {
            String[] split = this.locatorRegionIds.split(CommonConstants.VALUE_SEPARATOR);
            List<Integer> integers = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                integers.add(Integer.parseInt(split[i]));
            }
            return integers.toArray(new Integer[0]);
        }
        return new Integer[]{};
    }

}