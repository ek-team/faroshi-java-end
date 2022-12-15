package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class GetMaUrlLinkResult {
    private int errcode;
    private String errmsg;
    private String url_link;
}
