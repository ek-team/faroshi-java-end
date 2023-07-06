package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class SFMsgDataMianDan {
    private String templateCode;
    private List<Sfdocuments> documents;
    private String version="2.0";
    private Boolean sync=true;

}
