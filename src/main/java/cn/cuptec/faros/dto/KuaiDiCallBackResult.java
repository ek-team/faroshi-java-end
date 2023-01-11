package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class KuaiDiCallBackResult {
    private Boolean result;
    private String returnCode;
    private String message;
}