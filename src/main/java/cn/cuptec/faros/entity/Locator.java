package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Locator {

    private Integer id;

    //仓库名称
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String locatorName;

    //仓库类型 1-中转仓 2-私有仓
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer locatorType;

    //所在城市编码
    private String locatorRegionIds;

    //城市
    private String locatorRegion;
    //仓库产品数量
    private int productNum;

    //仓库产品锁定数量
    private int productLockNum;

    private String locatorDetailAddress;

    //仓库所属组织
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer deptId;

    private Date createTime;

    @TableField(exist = false)
    private String deptName;

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

    public void setLocatorRegions(Integer[] locatorRegions) {
        this.locatorRegions = locatorRegions;
        if (locatorRegions != null && locatorRegions.length > 0) {
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < locatorRegions.length; i++) {
                strList.add(locatorRegions[i].toString());
            }
            this.locatorRegionIds = StringUtils.join(strList, CommonConstants.VALUE_SEPARATOR);
        }
    }

}
