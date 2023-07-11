package cn.cuptec.faros.service;

import cn.cuptec.faros.dto.XiaDanParam;
import cn.cuptec.faros.entity.*;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.HttpClientUtil;
import com.alipay.api.internal.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ShunFengService {
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeliveryInfoService deliveryInfoService;

    public static void main(String[] args) {
        //  SFMiandan("SF7444467872275");

    }

    /**
     * 顺丰打印面单
     */
    public SFMsgDataResult SFMiandan(String waybillNo) {
        //String url = "https://sfapi-sbox.sf-express.com/std/service";//沙箱环境地址
        String checkWord = "ixCTn8uoUOiUyEKkb25hrT0k3dGvjsxL";//校验码
        String url = "https://bspgw.sf-express.com/std/service";//正式环境地址


        Map<String, String> params = new HashMap<String, String>();

        params.put("partnerID", "JH7X5WF1Z");  // 顾客编码 ，对应丰桥上获取的clientCode

        params.put("requestID", UUID.randomUUID().toString().replace("-", ""));

        params.put("serviceCode", "COM_RECE_CLOUD_PRINT_WAYBILLS");// 接口服务码
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("timestamp", timestamp);
        SFMsgDataMianDan sfMsgDataMianDan = new SFMsgDataMianDan();
        sfMsgDataMianDan.setTemplateCode("fm_150_standard_JH7X5WF1Z");
        List<Sfdocuments> documents = new ArrayList<>();
        Sfdocuments sfdocuments = new Sfdocuments();
        sfdocuments.setMasterWaybillNo(waybillNo);
        documents.add(sfdocuments);
        sfMsgDataMianDan.setDocuments(documents);

        String msgData = JSONObject.toJSONString(sfMsgDataMianDan);
        params.put("msgData", msgData);

        params.put("msgDigest", msgDigest(timestamp, msgData, checkWord));//数据签名
        String post = post(params, url);
        System.out.println(post);
        post = post.replace("\\", "");
        String[] apiResultData = post.split("apiResultData");
        String apiResultDatum = apiResultData[1];
        String substring = apiResultDatum.substring(3, apiResultDatum.length() - 2);
        if (StringUtils.isEmpty(substring)) {
            substring = "{}";
        }
        String post1 = apiResultData[0] + "apiResultData\":" + substring + "}";

        SFMsgDataResult sfMsgDataResult = JSONObject.parseObject(post1, SFMsgDataResult.class);

        return sfMsgDataResult;
    }

    /**
     * 顺丰下单
     */
    public void autoXiaDanSF(UserOrder userOrder, LocalDate deliveryDate, ServicePack servicePack, DeliverySetting deliverySetting) {
        //String url = "https://sfapi-sbox.sf-express.com/std/service";//沙箱环境地址
        String checkWord = "ixCTn8uoUOiUyEKkb25hrT0k3dGvjsxL";//校验码
        String url = "https://bspgw.sf-express.com/std/service";//正式环境地址


        Map<String, String> params = new HashMap<String, String>();

        params.put("partnerID", "JH7X5WF1Z");  // 顾客编码 ，对应丰桥上获取的clientCode

        params.put("requestID", UUID.randomUUID().toString().replace("-", ""));

        params.put("serviceCode", "EXP_RECE_CREATE_ORDER");// 接口服务码
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("timestamp", timestamp);
        SFMsgData sfMsgData = new SFMsgData();
        LocalDateTime localDateTime = deliveryDate.atTime(9, 00);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        sfMsgData.setSendStartTm(date);
        sfMsgData.setCargoDesc("医疗设备");
        sfMsgData.setOrderId(userOrder.getOrderNo());
        List<CargoDetail> cargoDetailList = new ArrayList<>();
        CargoDetail cargoDetail = new CargoDetail();
        cargoDetail.setName(servicePack.getName());
        cargoDetail.setUnit("台");
        cargoDetail.setWeight(servicePack.getWeight());
        cargoDetailList.add(cargoDetail);
        sfMsgData.setCargoDetails(cargoDetailList);
        List<ContactInfo> contactInfoList = new ArrayList<>();
        ContactInfo fromContactInfo = new ContactInfo();
        fromContactInfo.setContact(deliverySetting.getName());
        fromContactInfo.setContactType(1);
        fromContactInfo.setCompany("杭州易网健医疗有限公司");
        fromContactInfo.setAddress(deliverySetting.getAddress());
        fromContactInfo.setMobile(deliverySetting.getPhone());
        ContactInfo toContactInfo = new ContactInfo();
        toContactInfo.setContact(userOrder.getReceiverName());
        toContactInfo.setContactType(2);
        toContactInfo.setAddress(userOrder.getReceiverDetailAddress());
        toContactInfo.setMobile(userOrder.getReceiverPhone());

        contactInfoList.add(fromContactInfo);
        contactInfoList.add(toContactInfo);
        sfMsgData.setContactInfoList(contactInfoList);
        String msgData = JSONObject.toJSONString(sfMsgData);
        params.put("msgData", msgData);

        params.put("msgDigest", msgDigest(timestamp, msgData, checkWord));//数据签名
        String post = post(params, url);
        System.out.println(post);
        post = post.replace("\\", "");
        String[] apiResultData = post.split("apiResultData");
        String apiResultDatum = apiResultData[1];
        String substring = apiResultDatum.substring(3, apiResultDatum.length() - 2);

        String post1 = apiResultData[0] + "apiResultData\":" + substring + "}";

        SFMsgDataResult sfMsgDataResult = JSONObject.parseObject(post1, SFMsgDataResult.class);
        ApiResultData apiResultData1 = sfMsgDataResult.getApiResultData();
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        if (apiResultData1 != null) {
            MsgData msgData1 = apiResultData1.getMsgData();
            if (msgData1 != null) {
                userOrder.setDeliverySn(msgData1.getWaybillNoInfoList().get(0).getWaybillNo());
                userOrder.setDeliveryCompanyCode("shunfeng");
                deliveryInfo.setDeliverySn(msgData1.getWaybillNoInfoList().get(0).getWaybillNo());
            }
        }

        deliveryInfo.setMessage(sfMsgDataResult.getApiErrorMsg() + sfMsgDataResult.getApiResultCode());
        deliveryInfo.setUserOrderNo(userOrder.getOrderNo());
        deliveryInfo.setDeliveryName("SF");
        deliveryInfo.setDeptId(userOrder.getDeptId());
        deliveryInfoService.save(deliveryInfo);
        userOrdertService.updateById(userOrder);
        System.out.println(post1);

    }

    public static String post(Map<String, String> params, String urlData) {
        StringBuilder response = new StringBuilder("");
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry param : params.entrySet()) {
                if (builder.length() > 0) {
                    builder.append('&');
                }
                builder.append(URLEncoder.encode(param.getKey() + "", "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] bytes = builder.toString().getBytes("UTF-8");
            URL url = new URL(urlData);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(bytes);
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }

    public String msgDigest(String timestamp, String msgData, String checkWord) {
        //客户校验码    使用顺丰分配的客户校验码


        //将业务报文+时间戳+校验码组合成需加密的字符串(注意顺序)
        String toVerifyText = msgData + timestamp + checkWord;

        //因业务报文中可能包含加号、空格等特殊字符，需要urlEnCode处理
        try {
            toVerifyText = URLEncoder.encode(toVerifyText, "UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(toVerifyText.getBytes("UTF-8"));
            byte[] md = md5.digest();

            String msgDigest = new String(new BASE64Encoder().encode(md));
            return msgDigest;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}
