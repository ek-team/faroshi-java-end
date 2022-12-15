package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private ClientDetailsService clientDetailsService;


    @GetMapping("user")
    public RestResponse<CustomUser> customUserInfo(){
        try {
            ServletUtils.getResponse().sendRedirect("https://wxaurl.cn/zZQDIaslJfe");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RestResponse.ok();
    }


}
