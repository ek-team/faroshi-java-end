package cn.cuptec.faros.entity;

import lombok.Data;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/18 16:56
 */
@Data
public class Oauth2Result {
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private Integer errcode;
    private String errmsg;

}
