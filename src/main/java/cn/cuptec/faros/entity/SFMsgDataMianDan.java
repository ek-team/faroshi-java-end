package cn.cuptec.faros.entity;

import lombok.Data;

@Data
public class SFMsgDataMianDan {
    private String templateCode;
    private String[] documents;
    private String version="2.0";
    private Boolean sync=true;

}
