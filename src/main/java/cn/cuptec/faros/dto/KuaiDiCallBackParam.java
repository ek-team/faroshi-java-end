package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class KuaiDiCallBackParam {
    private String kuaidicom;
    private String kuaidinum;
    private String status;
    private String message;
    private KuaiDiCallBackData data;
}