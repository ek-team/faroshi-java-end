package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class GetTrainRecordData {
    private List<PneumaticRecord> pneumaticRecordList;
    private List<TbUserTrainRecord> tbUserTrainRecords;
}

