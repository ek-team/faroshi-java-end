package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.entity.OrderRefundInfo;
import cn.cuptec.faros.entity.RetrieveOrder;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserOrder;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
@Service
public class AliPayService {
    @Resource
    private OrderRefundInfoService orderRefundInfoService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private UserService userService;
    @Resource
    private WxMpService wxMpService;

    public void aliRefundOrder(String orderNo, BigDecimal amount, String userOrderNo) {

        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnvbm/slPh1TKRx3u/GpNbwCObJACqnSfsj72hEHlB8mDMivA6GvYpxbct1gGqQKknBZntbq4pgqY48ugDVFfVqbV14xKA74KGoMK0k87mHTSkSnXURsUXmgAnM7h44hhsPWAFlxUhJT+m6gt51+ltA4txigMXcvJtTSapJHU7nyzNFZ7fXqSzLQ1ZIiVgx8zuVGAnDbWimwd4O8+yVrxYuic2ZOO+2Qt5s91TwtTMOasfcXv/S4TB82v8t6g9gWm9riY9RvJ6gEucZkMkQvV4etU8s4CEIzud9ybqUOfhZguocSXMtTxHU3PdM1HhzktMj68cgP50vgDAdGBt9CTQIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", orderNo);//支付宝的流水号
        bizContent.put("refund_amount", amount);
        bizContent.put("out_request_no", userOrderNo);//回收单号


        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
            log.info("支付宝退款返回结果" + response.toString());
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println("调用成功");
            String outTradeNo = response.getOutTradeNo();//回收单号
            String refundFee = response.getRefundFee();//实际退款金额
            log.info("支付宝退款返回结果===" + outTradeNo + "==" + refundFee);

            RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                    .eq(RetrieveOrder::getOrderNo, userOrderNo));

            BigDecimal divide = amount;
            if (retrieveOrder != null) {
                //修改订单的实际回收价
                retrieveOrder.setRetrieveAmount(divide);
                retrieveOrder.setRetrieveEndTime(LocalDateTime.now());
                retrieveOrder.setStatus(5);
                retrieveOrderService.updateById(retrieveOrder);

                UserOrder byId = userOrdertService.getById(Integer.parseInt(retrieveOrder.getOrderId()));

                UserOrder userOrder = new UserOrder();
                userOrder.setId(Integer.parseInt(retrieveOrder.getOrderId()));
                userOrder.setActualRetrieveAmount(divide);
                userOrder.setSettlementAmount(byId.getPayment().subtract(divide));
                userOrdertService.updateById(userOrder);
                User userById = userService.getById(retrieveOrder.getUserId());
                //发送公众号通知
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                wxMpService.refundNotice(userById.getMpOpenId(), "您的订单已退款", divide + "元", df.format(LocalDateTime.now()), df.format(LocalDateTime.now()),
                        "点击查看详情", "pages/myRetrieveOrder/myRetrieveOrder");
            }


        } else {
            System.out.println("调用失败");
        }

    }


    public static void main(String[] args) {
        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnvbm/slPh1TKRx3u/GpNbwCObJACqnSfsj72hEHlB8mDMivA6GvYpxbct1gGqQKknBZntbq4pgqY48ugDVFfVqbV14xKA74KGoMK0k87mHTSkSnXURsUXmgAnM7h44hhsPWAFlxUhJT+m6gt51+ltA4txigMXcvJtTSapJHU7nyzNFZ7fXqSzLQ1ZIiVgx8zuVGAnDbWimwd4O8+yVrxYuic2ZOO+2Qt5s91TwtTMOasfcXv/S4TB82v8t6g9gWm9riY9RvJ6gEucZkMkQvV4etU8s4CEIzud9ybqUOfhZguocSXMtTxHU3PdM1HhzktMj68cgP50vgDAdGBt9CTQIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", "2023060122001451801431201069");//支付宝的流水号
        bizContent.put("refund_amount", new BigDecimal("0.01"));
        bizContent.put("out_request_no", 1);//回收单号


        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println("调用成功");
            log.info(response.getOutTradeNo());
            log.info(response.getRefundFee());


        } else {
            System.out.println("调用失败");
        }

    }
}
