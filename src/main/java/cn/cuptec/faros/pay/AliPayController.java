package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.UserOrdertService;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/alipay")
@Api(value = "alipay", tags = "支付宝支付")
public class AliPayController {
    private final Url url;
    @Resource
    private UserOrdertService userOrdertService;

    @RequestMapping("/pay")
    public RestResponse pay(@RequestParam("orderNo") String orderNo) {
        String[] split = orderNo.split("-");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));

        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtLUzH7+Zpld6nyxCOcZ+tQVxVIpGfg3pmV34Ky1Bz6v/fVGbt7ZVFPc004vmPwToPd4FURzXI9+udtzE+QNLGdA7wqRES65S49F6CyVNcfELbGAWpG6F5OkI6ZPPj0TYL3BYUm0CwuoxR8mSmlUtwh5FGe9EWvOJdw+rgLtXl6l2gd+YSzy3tD9fPfKVdRLNxZLUE96lQRlf+R0GraxRu1mvg9hbxVBT8HD3GSW8Rfdy94cUkLKDazLzucw74K60u5OGBXkpc5fA4GI2x6rqUt4nbxuJI2yJBFF7CnWEpdsYhXuClhvAqlMn02prepCck0RJZrT1T/oL6W9k+xJjRwIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "GBK", alipayPublicKey, "RSA2");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
//异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(url.getUrl() + "alipay/notifyAliOrder");
//同步跳转地址，仅支持http/https
        request.setReturnUrl("");
/******必传参数******/
        JSONObject bizContent = new JSONObject();
//商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderNo);
//支付金额，最小值0.01元
        bizContent.put("total_amount", userOrder.getPayment());
//订单标题，不可使用特殊符号
        bizContent.put("subject", "测试商品");

/******可选参数******/
//手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");


        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println(response.getBody());
        } else {
            System.out.println("调用失败");
        }
        return RestResponse.ok();
    }

    public static void main(String[] args) {
        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtLUzH7+Zpld6nyxCOcZ+tQVxVIpGfg3pmV34Ky1Bz6v/fVGbt7ZVFPc004vmPwToPd4FURzXI9+udtzE+QNLGdA7wqRES65S49F6CyVNcfELbGAWpG6F5OkI6ZPPj0TYL3BYUm0CwuoxR8mSmlUtwh5FGe9EWvOJdw+rgLtXl6l2gd+YSzy3tD9fPfKVdRLNxZLUE96lQRlf+R0GraxRu1mvg9hbxVBT8HD3GSW8Rfdy94cUkLKDazLzucw74K60u5OGBXkpc5fA4GI2x6rqUt4nbxuJI2yJBFF7CnWEpdsYhXuClhvAqlMn02prepCck0RJZrT1T/oL6W9k+xJjRwIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "GBK", alipayPublicKey, "RSA2");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
//异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl("");
//同步跳转地址，仅支持http/https
        request.setReturnUrl("");
/******必传参数******/
        JSONObject bizContent = new JSONObject();
//商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", "20210817010101004");
//支付金额，最小值0.01元
        bizContent.put("total_amount", 0.01);
//订单标题，不可使用特殊符号
        bizContent.put("subject", "测试商品");

/******可选参数******/
//手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");


        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println(response.getBody());
        } else {
            System.out.println("调用失败");
        }
    }


    @RequestMapping("/notifyAliOrder")
    public RestResponse notifyAliOrder(@RequestBody String data) {
        log.info("支付宝回调" + data);
        return RestResponse.ok();
    }

}
