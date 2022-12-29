package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
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
     * 关注公众号的二维码 永久二维码
     *
     * @param servicePackId 服务包id
     * @return 二维码url
     * @throws WxErrorException
     */
    @GetMapping("/introduceSubscribeQrCode")
    public RestResponse getProductQrcode(@RequestParam("servicePackId") int servicePackId) throws WxErrorException {
        ServicePack byId = servicePackService.getById(servicePackId);
        if (!StringUtils.isEmpty(byId.getMpQrCode())) {
            return RestResponse.ok(byId.getMpQrCode());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(servicePackId);

        sb.append(CommonConstants.VALUE_SEPARATOR);

        WxMpQrCodeTicket wxMpQrCodeTicket = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodeCreateLastTicket(sb.toString());
        String qrCodePictureUrl = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
        byId.setMpQrCode(qrCodePictureUrl);
        servicePackService.updateById(byId);
        return RestResponse.ok(qrCodePictureUrl);
    }

}
