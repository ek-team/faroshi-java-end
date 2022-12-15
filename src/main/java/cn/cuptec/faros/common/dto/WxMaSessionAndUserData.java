package cn.cuptec.faros.common.dto;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxMaSessionAndUserData extends WxMaUserInfo{

    /**
     * encryptedData
     */
    @NotBlank(message = "encryptedData不能为空")
    private String encryptedData;

    /**
     * iv
     */
    @NotBlank(message = "iv不能为空")
    private String iv;

    /**
     * sessionKey
     */
    @NotBlank(message = "sessionKey不能为空")
    private String sessionKey;

    /**
     * 公众号openId
     */
    private String mpOpenId;

}
