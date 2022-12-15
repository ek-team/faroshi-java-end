package cn.cuptec.faros.config.pay;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;

import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/16 14:59
 */
@Configuration
public class PayConfig {

    /**
     *  获取WxMpService
     * @param wxPayConfig wxPayConfig.appId、wxPayConfig.subAppId
     * @return
     */
    public static WxPayService getPayService(WxPayConfig wxPayConfig) {
        WxPayService wxPayService = new WxPayServiceImpl();

            wxPayConfig.setAppId("wx0ed9d77369636eb3");
            wxPayConfig.setMchId("1634816847");
            wxPayConfig.setMchKey("H2kjnmlpaX0bcdhdcbD2abhmz2ubJKKJ");
            wxPayConfig.setKeyPath("/ctm/front_faros/apiclient_cert.p12");
            wxPayConfig.setPrivateKeyPath("/ctm/front_faros/hxd/apiclient_key.pem");
            wxPayConfig.setPrivateCertPath("/ctm/front_faros/apiclient_cert.pem");
            wxPayConfig.setCertSerialNo("797654DEC7272D9ECCC53FDEDC185FABF57C834D");
            wxPayConfig.setApiV3Key("HsbjhbXjbcbDnjjhbJhhKhbgkbgvgJ09");
            // 可以指定是否使用沙箱环境
            wxPayConfig.setUseSandboxEnv(false);
            wxPayService.setConfig(wxPayConfig);

        return wxPayService;
    }
}
