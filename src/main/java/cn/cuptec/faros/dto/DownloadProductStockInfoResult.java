package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class DownloadProductStockInfoResult {
    private String user;
    private String plan;
    private String subPlan;
    private String trainRecord;
    private String trainData;
    private String evaluateRecord;
}
