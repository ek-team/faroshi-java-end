package cn.cuptec.faros.common.utils.sms;


import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.exception.OuterException;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Creater: Miao
 * CreateTime: 2018/11/27 17:24
 * Description:
 */
@Slf4j
public class SmsUtil {

    static final String product = "Dysmsapi";
    static final String domain = "dysmsapi.aliyuncs.com";
//    static final String accessKeyId = "LTAIvHPdRBZFbUHk";
//    static final String accessKeySecret = "LwjfN1jCj1r7lp1RohspJyOm97wmmE";

    public static void sendSms(String accessKeyId, String accessKeySecret, String signName, String phoneNo, String templateCode, Map<String, String> msgMap) {
        try{
            //初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            //组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            request.setMethod(MethodType.POST);
            //必填:待发送手机号
            request.setPhoneNumbers(phoneNo);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为 {"code":"123456"}
            String msg = null;
            if(msgMap != null){
                 msg = JSON.toJSONString(msgMap);
            }
            request.setTemplateParam(msg);

            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            log.info("发送短信:{}",sendSmsResponse);
            if(!sendSmsResponse.getCode().toLowerCase().equals(CommonConstants.OK)){
                log.info("发送短信失败1:{}",sendSmsResponse.getCode());
            }
        }catch (Exception e){

           log.info("发送短信失败2:{}",e.toString());
        }
    }

    public static QuerySendDetailsResponse querySendDetails(String accessKeyId, String accessKeySecret, String bizId, String phoneNo) throws ClientException {

        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象
        QuerySendDetailsRequest request = new QuerySendDetailsRequest();
        //必填-号码
        request.setPhoneNumber(phoneNo);
        //可选-流水号
        request.setBizId(bizId);
        //必填-发送日期 支持30天内记录查询，格式yyyyMMdd
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
        request.setSendDate(ft.format(new Date()));
        //必填-页大小
        request.setPageSize(10L);
        //必填-当前页码从1开始计数
        request.setCurrentPage(1L);

        //hint 此处可能会抛出异常，注意catch
        QuerySendDetailsResponse querySendDetailsResponse = acsClient.getAcsResponse(request);

        return querySendDetailsResponse;
    }

}

