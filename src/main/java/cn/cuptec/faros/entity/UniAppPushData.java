package cn.cuptec.faros.entity;

import lombok.Data;

@Data
public class UniAppPushData {
    private String cids;
    private String title;
    private String content;
    private String request_id;
}
