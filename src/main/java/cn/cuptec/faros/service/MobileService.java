package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.common.utils.sms.SmsUtil;
import cn.cuptec.faros.config.properties.SmsConfigProperties;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 手机登录相关业务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileService {

    /**
     * 登录手机验证码缓存的部分key
     */
    private static final String SMS_CACHE_KEY_LOGIN = "login_sms_code";
    private static final String SMS_CACHE_KEY_MOBOLE_CHECK = "mobile_check_sms_code";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsConfigProperties smsConfigProperties;

    /**
     * 发送手机登录校验码
     *
     * @param phoneNo
     */
    public void sendLoginSmsCode(String phoneNo) {
        String smsCode = RandomUtil.randomNumbers(Integer.parseInt(SecurityConstants.CODE_SIZE));
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put(SecurityConstants.CODE, smsCode);
        sendSmsCode(phoneNo, smsCode, SMS_CACHE_KEY_LOGIN, smsConfigProperties.getLoginTemplate(), msgParameterMap);
    }

    /**
     * 校验手机登录验证码
     *
     * @param phoneNo
     * @param smsCode
     * @return
     */
    public boolean checkLoginSmsCode(String phoneNo, String smsCode) {
        return checkSmsCode(phoneNo, SMS_CACHE_KEY_LOGIN, smsCode);
    }


    /**
     * 发送手机正确性校验码
     *
     * @param phoneNo
     */
    public void sendMObileCheckSmsCode(String phoneNo) {
        String smsCode = RandomUtil.randomNumbers(Integer.parseInt(SecurityConstants.CODE_SIZE));
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put(SecurityConstants.CODE, smsCode);
        sendSmsCode(phoneNo, smsCode, SMS_CACHE_KEY_MOBOLE_CHECK, smsConfigProperties.getLoginTemplate(), msgParameterMap);
    }

    /**
     * 校验手机正确性验证码的正确性
     *
     * @param phoneNo
     * @param smsCode
     * @return
     */
    public boolean verifyCheckCode(String phoneNo, String smsCode) {
        return checkSmsCode(phoneNo, SMS_CACHE_KEY_MOBOLE_CHECK, smsCode);
    }

    /**
     * 校验手机验证码
     *
     * @param cacheKey 缓存的key
     * @param smsCode  要验证的校验码
     */
    private boolean checkSmsCode(String phoneNo, String cacheKey, String smsCode) {
        String key = CacheConstants.SMS_CODE + StringPool.COLON + cacheKey + StringPool.COLON + phoneNo;
        Object obj = redisTemplate.opsForValue().get(key);
        String cachedSmsCode = null;
        if (obj != null) {
            cachedSmsCode = obj.toString();
            if (smsCode.equals(cachedSmsCode)) {
                //redisTemplate.delete(key);
                return true;
            }
        }
        return false;
    }

    /**
     * 发送验证码
     *
     * @param phoneNo         电话号码
     * @param smsCode         要发送的验证码
     * @param cacheKey        缓存的key
     * @param smsTemplate     短信模板
     * @param msgParameterMap 参数集合
     */
    private void sendSmsCode(String phoneNo, String smsCode, String cacheKey, String smsTemplate, Map<String, String> msgParameterMap) {
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                smsTemplate,
                msgParameterMap);

        //设置缓存 格式：key => sms_code:0:key:phone  value=> smsCode
        redisTemplate.opsForValue().set(
                CacheConstants.SMS_CODE + StringPool.COLON + cacheKey + StringPool.COLON + phoneNo, smsCode,
                smsConfigProperties.getExpireTime(), TimeUnit.SECONDS
        );
    }

    //业务员确认订单收款，通知用户即将发货
    private static String templateRevMoneyCode = "SMS_219739725";

    public void sendRevMoneyNotice(String phoneNo, String productName) {
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put("productName", productName);
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                templateRevMoneyCode,
                msgParameterMap);
    }

    //回收单业务员确认打款给客户了
    private static String templateRetrievedMoneyCode = "SMS_220355026";

    public void sendRetrievedMoneySms(String phoneNo) {
        Map<String, String> msgParameterMap = null;
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                templateRetrievedMoneyCode,
                msgParameterMap);
    }
//    //纳里订单通知给业务员
//    private static String templateNaLiCode = "SMS_237057060";
//
//
//    public void sendNaLiSms(String phoneNo,String orderId,String serviceId) {
//        Map<String, String> msgParameterMap = new HashMap<>();
//        msgParameterMap.put("orderId", orderId);
//        msgParameterMap.put("servicePackId", "服务包");
//        SmsUtil.sendSms(
//                smsConfigProperties.getAccessKeyId(),
//                smsConfigProperties.getAccessKeySecret(),
//                smsConfigProperties.getSignName(),
//                phoneNo,
//                templateNaLiCode,
//                msgParameterMap);
//    }
    public static void main(String[] args) {
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put("orderId", "abfdcb08a0f5472a9b0735b210fe5c77");
        msgParameterMap.put("deliveryNumber", "SF77844455");
        SmsUtil.sendSms(
                "LTAIvHPdRBZFbUHk",
                "LwjfN1jCj1r7lp1RohspJyOm97wmmE",
                "易网健",
                "13621745179",
                templateNaLiOrderCode,
                msgParameterMap);
    }
    private static String templateNaLiOrderCode = "SMS_257732986";
    public void sendNaLiOrderSms(String phoneNo,String orderId,String deliveryNumber) {
        log.info("发送短信:{}",deliveryNumber+"=="+phoneNo+"=="+orderId);
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put("orderId", orderId);
        msgParameterMap.put("deliveryNumber", deliveryNumber);
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                templateNaLiOrderCode,
                msgParameterMap);
    }
    //回收单用户确认收款
    private static String templateUserRetrievedMoneyCode = "SMS_221481236";

    public void sendUserRetrievedMoneySms(String phoneNo, String orderId) {
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put("orderId", orderId);
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                templateUserRetrievedMoneyCode,
                msgParameterMap);
    }

    //设备激活码短信
    private static String activeCode = "SMS_222275017";

    public void activeCode(String phoneNo, String code) {
        Map<String, String> msgParameterMap = new HashMap<>();
        msgParameterMap.put("code", code);
        SmsUtil.sendSms(
                smsConfigProperties.getAccessKeyId(),
                smsConfigProperties.getAccessKeySecret(),
                smsConfigProperties.getSignName(),
                phoneNo,
                activeCode,
                msgParameterMap);
    }
}
