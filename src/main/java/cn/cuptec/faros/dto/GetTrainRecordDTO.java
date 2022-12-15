package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.TbUserTrainRecord;
import lombok.Data;

import java.util.List;

@Data
public class GetTrainRecordDTO implements Comparable<GetTrainRecordDTO>{
     private String dateStr;
    private int maxTargetLoad;//负重大次数
    private int miniTargetLoad;//负重最小次数
    private int totalStepCount;//所有踩踏次数
    private List<TbUserTrainRecord> tbUserTrainRecordList;
    private int averageTargetLoad;//平均负重
    private Integer planStepCount;//计划踩踏次数

    @Override
    public int compareTo(GetTrainRecordDTO o) {
        return o.getDateStr().compareTo(this.dateStr);//根据时间降序
    }
}
