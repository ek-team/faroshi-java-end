package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SalesmanRetrieveAddress{

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer salesmanId;
    private Integer deptId;
    private Integer defaultStatus; //0默认 1-不默认
    private String phone;
    @JSONField(serialize = false)
    private String retrieveRegionIds;

    private String retrieveRegion;

    private String retrieveDetailAddress;

    @TableField(exist = false)
    private Integer[] retrieveRegions;

    public Integer[] getRetrieveRegions() {
        if (StringUtils.isNotEmpty(this.retrieveRegionIds)){
            String[] split = this.retrieveRegionIds.split(CommonConstants.VALUE_SEPARATOR);
            List<Integer> integers = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
              integers.add(Integer.parseInt(split[i]));
            }
            return integers.toArray(new Integer[0]);
        }
        return new Integer[]{};
    }

    public void setRetrieveRegions(Integer[] retrieveRegions) {
        this.retrieveRegions = retrieveRegions;
        if (retrieveRegions != null && retrieveRegions.length > 0){
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < retrieveRegions.length; i++) {
              strList.add(retrieveRegions[i].toString());
            }
            this.retrieveRegionIds = StringUtils.join(strList, CommonConstants.VALUE_SEPARATOR);
        }
    }
}
