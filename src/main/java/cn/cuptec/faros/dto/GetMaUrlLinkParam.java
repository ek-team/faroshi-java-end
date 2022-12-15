package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class GetMaUrlLinkParam {
    private String path;
    private boolean is_expire;
    private int expire_type;
    private int expire_time;
    private int expire_interval;
}
