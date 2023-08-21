package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.QrCodeUtils;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/alipay")
@Api(value = "alipay", tags = "支付宝支付")
public class AliPayController {
    @Autowired
    public RedisTemplate redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeptService deptService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private OrderRefundInfoService orderRefundInfoService;
    @Resource
    private WxPayFarosService wxPayFarosService;
    @Resource
    private UserFollowDoctorService userFollowDoctorService;
    @Resource
    private UserDoctorRelationService userDoctorRelationService;
    @Resource
    private MacAddOrderCountService macAddOrderCountService;//患者其它订单
    @Resource
    private ProductStockService productStockService;//医生积分
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private DiseasesService diseasesService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private PatientRelationTeamService patientRelationTeamService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private UserQrCodeService userQrCodeService;
    @Resource
    private UserOrderNotifyService userOrderNotifyService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private SaleSpecGroupService saleSpecGroupService;
    @Resource
    private SaleSpecDescService saleSpecDescService;
    @Resource
    private UpdateOrderRecordService updateOrderRecordService;
    @Resource
    private SaleSpecService saleSpecService;
    private final Url urlData;


    @RequestMapping("/payqrcode")
    public RestResponse payqrcode(@RequestParam("orderNo") String orderNo) {
        String[] split = orderNo.split("KF");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));
        log.info(userOrder.getStatus() + "订单号");
        if (!userOrder.getStatus().equals(1)) {
            log.info("返回结果");
            return RestResponse.ok("已支付");
        }
        log.info(userOrder.getStatus() + "wahahah");
        String url = (String) redisTemplate.opsForValue().get(orderNo);

        if (!StringUtils.isEmpty(url)) {
            try {
                BufferedImage bufferedImage = QrCodeUtils.generatorQrCode(url, "/ctm");
                ImageIO.write(bufferedImage, "png", ServletUtils.getResponse().getOutputStream());
                return RestResponse.ok();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnvbm/slPh1TKRx3u/GpNbwCObJACqnSfsj72hEHlB8mDMivA6GvYpxbct1gGqQKknBZntbq4pgqY48ugDVFfVqbV14xKA74KGoMK0k87mHTSkSnXURsUXmgAnM7h44hhsPWAFlxUhJT+m6gt51+ltA4txigMXcvJtTSapJHU7nyzNFZ7fXqSzLQ1ZIiVgx8zuVGAnDbWimwd4O8+yVrxYuic2ZOO+2Qt5s91TwtTMOasfcXv/S4TB82v8t6g9gWm9riY9RvJ6gEucZkMkQvV4etU8s4CEIzud9ybqUOfhZguocSXMtTxHU3PdM1HhzktMj68cgP50vgDAdGBt9CTQIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(urlData.getUrl() + "alipay/notifyAliOrder");
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        bizContent.put("total_amount", userOrder.getPayment());
        bizContent.put("subject", "faros");


        request.setBizContent(bizContent.toString());
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

        try {


            url = response.getQrCode();
            BufferedImage bufferedImage = QrCodeUtils.generatorQrCode(url, "/ctm");
            redisTemplate.opsForValue().set(orderNo, url, 2, TimeUnit.HOURS);//设置过期时间
            ImageIO.write(bufferedImage, "png", ServletUtils.getResponse().getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    @RequestMapping("/pay")
    public RestResponse pay(@RequestParam("orderNo") String orderNo) {
        String[] split = orderNo.split("KF");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));

        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtLUzH7+Zpld6nyxCOcZ+tQVxVIpGfg3pmV34Ky1Bz6v/fVGbt7ZVFPc004vmPwToPd4FURzXI9+udtzE+QNLGdA7wqRES65S49F6CyVNcfELbGAWpG6F5OkI6ZPPj0TYL3BYUm0CwuoxR8mSmlUtwh5FGe9EWvOJdw+rgLtXl6l2gd+YSzy3tD9fPfKVdRLNxZLUE96lQRlf+R0GraxRu1mvg9hbxVBT8HD3GSW8Rfdy94cUkLKDazLzucw74K60u5OGBXkpc5fA4GI2x6rqUt4nbxuJI2yJBFF7CnWEpdsYhXuClhvAqlMn02prepCck0RJZrT1T/oL6W9k+xJjRwIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
//异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(urlData.getUrl() + "alipay/notifyAliOrder");
//同步跳转地址，仅支持http/https
        request.setReturnUrl("");
/******必传参数******/
        JSONObject bizContent = new JSONObject();
//商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderNo);
//支付金额，最小值0.01元
        bizContent.put("total_amount", userOrder.getPayment());
//订单标题，不可使用特殊符号
        bizContent.put("subject", "faros");

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

        return RestResponse.ok(response.getBody());
    }

    public static void main(String[] args) {
        UserOrder userOrder = new UserOrder();
        userOrder.setStatus(2);
        if (!userOrder.getStatus().equals(1)) {
            System.out.println(1);
        }

//        String appId = "2021003190689317";
//        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
//        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtLUzH7+Zpld6nyxCOcZ+tQVxVIpGfg3pmV34Ky1Bz6v/fVGbt7ZVFPc004vmPwToPd4FURzXI9+udtzE+QNLGdA7wqRES65S49F6CyVNcfELbGAWpG6F5OkI6ZPPj0TYL3BYUm0CwuoxR8mSmlUtwh5FGe9EWvOJdw+rgLtXl6l2gd+YSzy3tD9fPfKVdRLNxZLUE96lQRlf+R0GraxRu1mvg9hbxVBT8HD3GSW8Rfdy94cUkLKDazLzucw74K60u5OGBXkpc5fA4GI2x6rqUt4nbxuJI2yJBFF7CnWEpdsYhXuClhvAqlMn02prepCck0RJZrT1T/oL6W9k+xJjRwIDAQAB";
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
//        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
////异步接收地址，仅支持http/https，公网可访问
//        request.setNotifyUrl("");
////同步跳转地址，仅支持http/https
//        request.setReturnUrl("");
///******必传参数******/
//        JSONObject bizContent = new JSONObject();
////商户订单号，商家自定义，保持唯一性
//        bizContent.put("out_trade_no", "20210817010101004");
////支付金额，最小值0.01元
//        bizContent.put("total_amount", 0.01);
////订单标题，不可使用特殊符号
//        bizContent.put("subject", "测试商品");
//
///******可选参数******/
////手机网站支付默认传值FAST_INSTANT_TRADE_PAY
//        bizContent.put("product_code", "QUICK_WAP_WAY");
//
//
//        request.setBizContent(bizContent.toString());
//        AlipayTradeWapPayResponse response = null;
//        try {
//            response = alipayClient.pageExecute(request,"get");
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        if (response.isSuccess()) {
//            System.out.println("调用成功");
//            System.out.println(response);
//            System.out.println(response.getBody());
//        } else {
//            System.out.println("调用失败");
//        }
    }


    @RequestMapping("/notifyAliOrder")
    public RestResponse notifyAliOrder(@RequestBody String data) {
        log.info("支付宝回调" + data);

        Map<String, String> reconstructedUtilMap = Arrays.stream(data.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        String out_trade_no = reconstructedUtilMap.get("out_trade_no");//订单号
        log.info("支付宝回调参数" + out_trade_no);
        String trade_status = reconstructedUtilMap.get("trade_status");//状态
        log.info("支付宝回调参数" + trade_status);
        String trade_no = reconstructedUtilMap.get("trade_no");//流水号
        log.info("支付宝回调参数" + trade_no);
        if (trade_status.equals("TRADE_SUCCESS")) {

            UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, out_trade_no));
            if (userOrder != null) {
                UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
                updateOrderRecord.setOrderId(userOrder.getId());
                updateOrderRecord.setCreateUserId(userOrder.getUserId());
                updateOrderRecord.setCreateTime(LocalDateTime.now());
                updateOrderRecord.setDescStr("支付成功");
                updateOrderRecordService.save(updateOrderRecord);
                if (!userOrder.getStatus().equals(1)) {
                    return RestResponse.ok();
                }
                String doctorTeamName = "";

                Integer patientUserId = userOrder.getPatientUserId();
                PatientUser byId = patientUserService.getById(patientUserId);
                User userById = userService.getById(userOrder.getUserId());
                //发送公众号通知
                wxMpService.paySuccessNotice(userById.getMpOpenId(), "您的订单已支付成功!请耐心等待发货", userOrder.getOrderNo(), userOrder.getPayment().toString(),
                        "点击查看详情", "pages/myOrder/myOrder");

                //发送公众号通知业务员
                LambdaQueryWrapper<UserQrCode> eq = new QueryWrapper<UserQrCode>().lambda()
                        .eq(UserQrCode::getQrCodeId, userOrder.getServicePackId());
                List<UserQrCode> userQrCodeList = userQrCodeService.list(
                        eq);
                List<String> userIdList = new ArrayList<>();
                List<UserOrderNotify> userOrderNotifyList = userOrderNotifyService.list();
                if (!CollectionUtils.isEmpty(userOrderNotifyList)) {
                    userIdList = userOrderNotifyList.stream().map(UserOrderNotify::getUserId)
                            .collect(Collectors.toList());
                }

                if (!CollectionUtils.isEmpty(userQrCodeList)) {
                    List<String> userIdLIst = userQrCodeList.stream().map(UserQrCode::getUserId)
                            .collect(Collectors.toList());
                    userIdList.addAll(userIdLIst);
                }
                String patientName = "";
                if (byId != null) {
                    patientName = byId.getName();
                }
                if (!CollectionUtils.isEmpty(userIdList)) {
                    ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());
                    String keyword1 = "";
                    if (servicePack != null) {
                        keyword1 = servicePack.getName() + "患者：" + patientName + "订单号:" + out_trade_no;
                    } else {
                        keyword1 = "患者：" + patientName + "订单号:" + out_trade_no;
                    }
                    List<User> clerkUser = (List<User>) userService.listByIds(userIdList);
                    //查询医院信息
                    Integer hospitalId = servicePack.getHospitalId();
                    if (hospitalId != null) {
                        HospitalInfo hospitalInfo = hospitalInfoService.getById(hospitalId);
                        if (hospitalInfo != null) {
                            keyword1 = keyword1 + "医院:" + hospitalInfo.getName();
                        }
                    }
                    //查询医生团队
                    Integer doctorTeamId = userOrder.getDoctorTeamId();

                    if (doctorTeamId != null) {
                        DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
                        if (doctorTeam != null) {
                            doctorTeamName = doctorTeam.getName();
                            keyword1 = keyword1 + "医生团队:" + doctorTeam.getName();
                        }
                    }
                    for (User user : clerkUser) {
                        if (clerkUser != null) {
                            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                                wxMpService.paySuccessNoticeSalesman(user.getMpOpenId(), "您的客户已成功下单，请您尽快处理！", keyword1, userOrder.getPayment().toString(),
                                        "点击查看详情", urlData.getUrl() + "index.html#/salesman/orderDetailster?id=" + out_trade_no);
                            }

                        }
                    }

                }


                if (byId != null) {
                    userById.setPatientName(byId.getName());
                    userById.setPatientId(byId.getId());
                    userById.setIdCard(byId.getIdCard());
                    userById.setDeptId(userOrder.getDeptId());
                    userService.updateById(userById);
                }
                userOrder.setConfirmPayTime(new Date());
                userOrder.setTransactionId(trade_no);
                userOrder.setStatus(2);//已支付 待发货
                userOrder.setPayType(2);
                userOrder.setPayTime(LocalDateTime.now());
                //为用户创建群聊
                Integer doctorTeamId = userOrder.getDoctorTeamId();
                List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                        .eq(DoctorTeamPeople::getTeamId, doctorTeamId));

                List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());

                List<UserFollowDoctor> one = userFollowDoctorService.list(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getUserId, userOrder.getUserId())
                        .in(UserFollowDoctor::getTeamId, doctorTeamId));
                if (CollectionUtils.isEmpty(one)) {
                    //添加医生团队的好友关系
                    UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                    userFollowDoctor.setTeamId(doctorTeamId);
                    userFollowDoctor.setUserId(userOrder.getUserId());
                    userFollowDoctorService.save(userFollowDoctor);
                }
                //患者和团队的关系
                patientRelationTeamService.remove(new QueryWrapper<PatientRelationTeam>().lambda()
                        .eq(PatientRelationTeam::getPatientId, userOrder.getUserId())
                        .eq(PatientRelationTeam::getTeamId, doctorTeamId));
                PatientRelationTeam patientRelationTeam = new PatientRelationTeam();

                patientRelationTeam.setPatientId(userOrder.getUserId());
                patientRelationTeam.setTeamId(doctorTeamId);
                patientRelationTeamService.save(patientRelationTeam);
                //添加医生和患者的关系
                List<UserDoctorRelation> userDoctorRelationList = new ArrayList<>();
                userDoctorRelationService.remove(new QueryWrapper<UserDoctorRelation>().lambda()
                        .eq(UserDoctorRelation::getUserId, userOrder.getUserId())
                        .in(UserDoctorRelation::getDoctorId, userIds));


                for (Integer doctorId : userIds) {
                    UserDoctorRelation userDoctorRelation = new UserDoctorRelation();
                    userDoctorRelation.setDoctorId(doctorId);
                    userDoctorRelation.setUserId(userOrder.getUserId());
                    userDoctorRelationList.add(userDoctorRelation);
                }
                userDoctorRelationService.saveBatch(userDoctorRelationList);

                userIds.add(userOrder.getUserId());
                ChatUser chatUser = chatUserService.saveGroupChatUser(userIds, doctorTeamId, userOrder.getUserId(), patientUserId, byId.getName());

                //修改用户的病种
                Integer diseasesId = userOrder.getDiseasesId();
                if (diseasesId != null) {
                    Diseases diseases = diseasesService.getById(diseasesId);
                    if (diseases != null) {
                        User user = new User();
                        user.setId(userOrder.getUserId());
                        user.setDiseasesName(diseases.getName());
                        userService.updateById(user);
                    }
                }
                userOrder.setChatUserId(chatUser.getId());

                //添加用户自己的服务
                Integer servicePackId = userOrder.getServicePackId();

                List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>().lambda()
                        .eq(ServicePackageInfo::getServicePackageId, servicePackId));

                List<SaleSpecGroup> saleSpecGroupList = saleSpecGroupService.list(new QueryWrapper<SaleSpecGroup>().lambda().eq(SaleSpecGroup::getQuerySaleSpecIds, userOrder.getQuerySaleSpecIds())
                        .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));
                Integer serviceCount = 3;
                Integer sendUrl = 0;
                if (!CollectionUtils.isEmpty(saleSpecGroupList)) {
                    SaleSpecGroup saleSpecGroup = saleSpecGroupList.get(0);
                    if (saleSpecGroup.getServiceCount() != null) {
                        serviceCount = saleSpecGroup.getServiceCount();
                    }
                    sendUrl = saleSpecGroup.getSendUrl();
                }

                if (!CollectionUtils.isEmpty(servicePackageInfos)) {
                    List<UserServicePackageInfo> userServicePackageInfos = new ArrayList<>();
                    for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                        UserServicePackageInfo userServicePackageInfo = new UserServicePackageInfo();
                        userServicePackageInfo.setUserId(userOrder.getUserId());
                        userServicePackageInfo.setOrderId(userOrder.getId());

                        userServicePackageInfo.setTotalCount(serviceCount);

                        userServicePackageInfo.setChatUserId(chatUser.getId());
                        userServicePackageInfo.setServicePackageInfoId(servicePackageInfo.getId());
                        userServicePackageInfo.setCreateTime(LocalDateTime.now());
                        if (servicePackageInfo.getExpiredDay() != null) {
                            userServicePackageInfo.setExpiredTime(LocalDateTime.now().plusDays(servicePackageInfo.getExpiredDay()));
                        }
                        userServicePackageInfos.add(userServicePackageInfo);
                    }
                    userServicePackageInfoService.saveBatch(userServicePackageInfos);
                    userOrder.setUserServicePackageInfoId(userServicePackageInfos.get(0).getId());
                }

                userOrdertService.updateById(userOrder);

                //判断用户是否选择了发送支架链接规格
                if (sendUrl.equals(1)) {
                    //发送支架提醒
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String time = df.format(now);
                    wxMpService.sendDoctorUrlTip(userById.getMpOpenId(), "购买支架链接", patientName, time,
                            "购买支架链接", urlData.getUrl() + "record.html#/ucenter/recovery/externalLink");

                }

                userOrdertService.autoXiaDanCheck(userOrder.getOrderNo(), urlData.getUrl());
                pushOrderCount(userOrder.getServicePackId());
            }
        }
        return RestResponse.ok();
    }

    private void pushOrderCount(Integer servicePackId) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                        .eq(ProductStock::getServicePackId, servicePackId)
                        .eq(ProductStock::getDel, 1));
                if (!CollectionUtils.isEmpty(productStocks)) {
                    for (ProductStock productStock : productStocks) {
                        String macAddress = productStock.getMacAddress();
                        MacAddOrderCount macAddOrderCount = macAddOrderCountService.getOne(new QueryWrapper<MacAddOrderCount>().lambda()
                                .eq(MacAddOrderCount::getMacAdd, macAddress));
                        if (macAddOrderCount == null) {
                            macAddOrderCount = new MacAddOrderCount();
                            macAddOrderCount.setCount(1);
                        } else {
                            macAddOrderCount.setCount(macAddOrderCount.getCount() + 1);
                        }
                        macAddOrderCount.setMacAdd(macAddress);
                        macAddOrderCountService.saveOrUpdate(macAddOrderCount);
                        Channel targetUserChannel = UserChannelManager.getUserChannelByMacAdd(macAddress);
                        //2.向目标用户发送新消息提醒
                        if (targetUserChannel != null) {

                            SocketFrameTextMessage targetUserMessage
                                    = SocketFrameTextMessage.addOrderCount(macAddOrderCount.getCount(), macAddress);

                            targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));

                        }
                    }
                }
            }
        });
    }

    /**
     * 订单退款
     *
     * @param orderNo
     * @return
     */
    @RequestMapping("/aliRefundOrder")
    public RestResponse aliRefundOrder(@RequestParam("orderNo") String orderNo) throws AlipayApiException {

        String appId = "2021003190689317";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0tTMfv5mmV3qfLEI5xn61BXFUikZ+DemZXfgrLUHPq/99UZu3tlUU9zTTi+Y/BOg93gVRHNcj36523MT5A0sZ0DvCpERLrlLj0XoLJU1x8QtsYBakboXk6Qjpk8+PRNgvcFhSbQLC6jFHyZKaVS3CHkUZ70Ra84l3D6uAu1eXqXaB35hLPLe0P1898pV1Es3FktQT3qVBGV/5HQatrFG7Wa+D2FvFUFPwcPcZJbxF93L3hxSQsoNrMvO5zDvgrrS7k4YFeSlzl8DgYjbHqupS3idvG4kjbIkEUXsKdYSl2xiFe4KWG8CqUyfTamt6kJyTRElmtPVP+gvpb2T7EmNHAgMBAAECggEAZeGHCk5GvU6yto0IZXRwuXRxGb2/0o/bdPlS0lz4rrIFIE1jYqcsvt5E7UQBsuP8X+0NyFZfQT16Kk97yfy+WbZaCvn7+0M0Pnc6vI/yYtwImbhu65PYb1+nA7GvItIopE5NrWMCXIwW7qdJvTNq0feo898/BZwqk3LFOZXl433XwxmgCBrci1xa5SXzn3fCbGpcut9XX4jdgTU8e2XFXxEUBWUlIKh/nRqeIZOqj71SPG+YwbV4ouhhR/hp8zS0qw8z4RuqjUPPsn5/6Usu/Xgcf0Mjctt0ZObnk4crR5Txr45RvoCrFCDDvyWMTsY/cNvRqQTO8LCoq0PUSEfqkQKBgQDereFPmv2r3lKw4faOkdE6R+4cewwRisw9Vr3lpQDn2uqKV7SnEfPzZGdii5MfYnoa5X9URJ6Rp4PD7n4AtUTG6DYKN4CBHlWQSDU2TzmfpxbFRKIXj5uQSXTJUSpb5bgHEgm1vKwblD2biYm+hkdWAaHiHyVwoeGRdlUknKTrnwKBgQDPv4flcmrX8JtVCLSReZgnsIVJ49RlwaYDb1MOC6Pgg5UneCoXHSzN7C6jl+eKgvL7kC2WEOg+gGGNSNStuj8ebjQX5pyyxlQig3/3qnOalFbRHab+UL/Xuic/iF+ZeLW4oaaETzzubrUR55vvY5vo9ut8/pY0XJ6eL+uwNuDnWQKBgGa6XMk2vXQ+enNzqyUWjCmQ6X5mHakyGQrrK2v39TUBP1ZXI9Y3aA2O8kr6DQNbkO07lsQva9/SIe2P5r044uPIWLXZ6QSoE90eEr5dSj4m/VBAW273J1MnMCN3uEzw6zcH0UbwJY4Lk2hfyRYGKH66/g2tRL5zT/alWp4rTcINAoGAY6MwwlMF+1tipH3wXHU9DIwU4UNr8wHVZYBXDT13844oUy3Gwh80Be9ozv1kB4KWlyCnPHoPaSqZnvF3T3ssGqQwR+ZK8VM9tu/qyBXwLAtJODJIjWCdIhIeENKPR0Qlo8+j1YFLb++Y2GWE3GOhuzHx75kK4UIqsSO6nmEzrMECgYEA2HEY22eKmLp1Fqaca8lKXIIL9ocQZbXh21J/zEFmaJzl2C2IL+gOksOyS4KroMYvWPX09tw3VsGxcch/qa9KW/f6HLImwo14XuRxXUrP4AJiDzLxqCjN0DLKSb0g/MEyfVlg9RegtGM/Kcq/0qtgwRUOQ2PcSglo4h423H8khSQ=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnvbm/slPh1TKRx3u/GpNbwCObJACqnSfsj72hEHlB8mDMivA6GvYpxbct1gGqQKknBZntbq4pgqY48ugDVFfVqbV14xKA74KGoMK0k87mHTSkSnXURsUXmgAnM7h44hhsPWAFlxUhJT+m6gt51+ltA4txigMXcvJtTSapJHU7nyzNFZ7fXqSzLQ1ZIiVgx8zuVGAnDbWimwd4O8+yVrxYuic2ZOO+2Qt5s91TwtTMOasfcXv/S4TB82v8t6g9gWm9riY9RvJ6gEucZkMkQvV4etU8s4CEIzud9ybqUOfhZguocSXMtTxHU3PdM1HhzktMj68cgP50vgDAdGBt9CTQIDAQAB";
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, appPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", "2021081722001419121412730660");
        bizContent.put("refund_amount", 0.01);
        bizContent.put("out_request_no", "HZ01RF001");


        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return RestResponse.ok();
    }

}
