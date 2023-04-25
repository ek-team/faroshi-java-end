package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/alipay")
@Api(value = "alipay", tags = "支付宝支付")
public class AliPayController {

    @RequestMapping("/pay")
    public RestResponse pay(HttpServletRequest request) {
        return RestResponse.ok();
    }

}
