package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class PneumaticRecordResult {
    private String planDayTime;
    private List<PneumaticRecord> records;
}
