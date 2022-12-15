package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.service.WxMpService;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


//微信公众号JS api相关接口
@RestController
@RequestMapping("/mp/jsapi")
public class WxMpJsApiController {

    @Resource
    private WxMpService wxMpService;

    //获取jsapi ticket
    @SneakyThrows
    @GetMapping("ticket")
    public ResponseEntity<String> getJsApiTicket(){
        return ResponseEntity.ok(WxMpConfiguration.getWxMpService().getJsapiTicket());
    }

    @SneakyThrows
    @GetMapping("signature")
    public ResponseEntity<WxJsapiSignature> createJsapiSignature(@RequestParam String url){
        return ResponseEntity.ok(WxMpConfiguration.getWxMpService().createJsapiSignature(url));
    }
    @SneakyThrows
    @GetMapping("signatureNotUrl")
    public ResponseEntity<WxJsapiSignature> createJsapiSignature(){
        return ResponseEntity.ok(WxMpConfiguration.getWxMpService().createJsapiSignature(""));
    }

}
