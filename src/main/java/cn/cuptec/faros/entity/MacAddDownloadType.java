package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MacAddDownloadType {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String macAdd;
    /**
     * _user.json
     * _plan.json
     * _subPlan.json
     * _trainRecord.json
     * _trainData.json
     * _evaluateRecord.json
     */
    private String downloadType;
}
