package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackSaleSpec;
import cn.cuptec.faros.service.ServicePackService;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号二维码接口
 */
@Slf4j
@RestController
@RequestMapping("/mp/qrcode")
public class WxMpQrCodeController {
    @Resource
    private ServicePackService servicePackService;

    /**
     * 关注公众号的二维码 临时二维码 场景值 是服务包id
     *
     * @param servicePackId 服务包id
     * @return 二维码url
     * @throws WxErrorException
     */
    @GetMapping("/introduceSubscribeQrCode")
    public RestResponse getProductQrcode(@RequestParam("servicePackId") int servicePackId,@RequestParam(value = "token",required = false) String token) throws WxErrorException {

        StringBuilder sb = new StringBuilder();
        sb.append(servicePackId);

        sb.append(CommonConstants.VALUE_SEPARATOR);
        sb.append(token);
        sb.append(CommonConstants.VALUE_SEPARATOR);
        sb.append("servicePack");
        WxMpQrCodeTicket wxMpQrCodeTicket = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodeCreateTmpTicket(sb.toString(),259200);
        String qrCodePictureUrl = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
        return RestResponse.ok(qrCodePictureUrl);
    }
    /**
     * 关注公众号的二维码 临时二维码 场景值 是医生id
     *
     * @return 二维码url
     * @throws WxErrorException
     */
    @GetMapping("/doctorSubscribeQrCode")
    public RestResponse doctorSubscribeQrCode(@RequestParam("doctorId") String doctorId,@RequestParam(value = "token",required = false) String token) throws WxErrorException {
        StringBuilder sb = new StringBuilder();

        if (doctorId.indexOf("-") < 0) {
            sb.append(doctorId);

            sb.append(CommonConstants.VALUE_SEPARATOR);
            sb.append(token);
            sb.append(CommonConstants.VALUE_SEPARATOR);
            sb.append("addPatient");
        }else {
            sb.append(doctorId);

            sb.append(CommonConstants.VALUE_SEPARATOR);
            sb.append(token);
            sb.append(CommonConstants.VALUE_SEPARATOR);
            sb.append("addTeam");
        }

        WxMpQrCodeTicket wxMpQrCodeTicket = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodeCreateTmpTicket(sb.toString(),259200);
        String qrCodePictureUrl = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
        return RestResponse.ok(qrCodePictureUrl);
    }

}
