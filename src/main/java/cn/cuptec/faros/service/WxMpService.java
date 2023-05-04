package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;

import cn.hutool.core.collection.CollUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WxMpService {


    public void sendTemplateMsg(String openId, String templateId, String url, List<WxMpTemplateData> dataList) {

        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)//要推送的用户openid
                .templateId(templateId)//模板id
                .miniProgram(new WxMpTemplateMessage.MiniProgram("wxad59cd874b45bb96",url,false))
                .build();

        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(templateMessage::addData);
        }

        try {
            WxMpConfiguration.getWxMpService().getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }
    public void sendUrlTemplateMsg(String openId, String templateId, String url, List<WxMpTemplateData> dataList) {

        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)//要推送的用户openid
                .templateId(templateId)//模板id
                .url(url)
                .build();

        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(templateMessage::addData);
        }

        try {
            WxMpConfiguration.getWxMpService().getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }
    public void sendTopic(String openId, String first, String keyword1,
                          String keyword2, String remark, String url) {
        String templateId = "s4SMlZMpXSeml9lMfaHG8mm1kv4TW5ga77giTfTj2Q8";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("remark");
        wxMpTemplateData3.setValue(remark);
        dataList.add(wxMpTemplateData3);
        sendTemplateMsg(openId, templateId, url, dataList);
    }
    public void sendLiveQrCodeNotice(String openId, String first, String keyword1,
                          String keyword2, String remark, String url) {
        String templateId = "UsLUYgFGFqvv2FEl4UeHa-aGi_kvUF5Rl1zrkRGerMc";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("remark");
        wxMpTemplateData3.setValue(remark);
        dataList.add(wxMpTemplateData3);
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    public void sendImMsgUnreadNotice(String openId, String first, String keyword1,
                          String keyword2,String keyword3, String remark, String url) {
        String templateId = "Ks7_eGLPFvbbeTQObUqnxEpZwfHonZ5qhUKuzOEA_lc";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);
        WxMpTemplateData wxMpTemplateData4 = new WxMpTemplateData();
        wxMpTemplateData4.setName("remark");
        wxMpTemplateData4.setValue(remark);
        dataList.add(wxMpTemplateData4);
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    public void sendSubNotice(String openId, String first, String keyword1,
                                      String keyword2, String remark, String url) {
        String templateId = "A5D2HXLY7MnU72NaeDqcQBwCYR3Ldg-8plClkQL8OGE";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }
    //支付成功
    public void paySuccessNotice(String openId, String first, String keyword1,
                              String keyword2, String remark, String url) {
        String templateId = "mzXslzSjZiMhCQJ6MQi_bcDJOVbDLl6sfLn6u_0gocI";
        if(StringUtils.isEmpty(remark)){
            remark="点击查看详情";
        }
        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        dataList.add(new WxMpTemplateData("remark", remark, "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }
    //发货通知
    public void shipNotice(String openId, String first, String keyword1,
                                 String keyword2,String keyword3,String keyword4, String remark, String url) {
        String templateId = "4bNAEvxGtvR3itx2X6SzUOBiU8wtmby9tdEOhGU5a3I";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);

        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);

        WxMpTemplateData wxMpTemplateData4 = new WxMpTemplateData();
        wxMpTemplateData4.setName("keyword4");
        wxMpTemplateData4.setValue(keyword4);
        dataList.add(wxMpTemplateData4);

        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }
    //退款通知
    public void refundNotice(String openId, String first, String keyword1,
                             String keyword2,String keyword3, String remark, String url) {
        String templateId = "XAJp_620LIdi6otX14W1dLSrYSpuJ_txhuusVoNIv5k";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);

        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    //发票通知
    public void faPiaoNotice(String openId, String first, String keyword1,
                           String keyword2,String keyword3, String remark, String url) {
        String templateId = "cYvxMNdH8IOSrLlDCaQ9g-Z8MlCpikBMLi0mvR-9yk0";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);

        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    //随访计划
    public void sendFollowUpPlanNotice(String openId, String first, String keyword1,
                              String keyword2, String url) {
        String templateId = "hqiXa8OXHi3w4BbJF2RaXiVLWbtgTRPi_CQNhFAwa60";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }
    public void sendDoctorUrlTip(String openId, String first, String keyword1,
                     String keyword2, String keyword3, String url) {
        String templateId = "JS1GcbXs_Tm28-HnzuStvnDTbLxtL71wxdWaZ-wpnd0";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendUrlTemplateMsg(openId, templateId, url, dataList);
    }

    //医嘱消息提醒
    public void sendDoctorTip(String openId, String first, String keyword1,
                              String keyword2, String keyword3, String url) {
        String templateId = "JS1GcbXs_Tm28-HnzuStvnDTbLxtL71wxdWaZ-wpnd0";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("keyword3");
        wxMpTemplateData3.setValue(keyword3);
        dataList.add(wxMpTemplateData3);
        dataList.add(new WxMpTemplateData("remark", "点击查看详情", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    //注册设备用户模版消息
    public void sendRegisterPlanUser(String openId, String first, String keyword1,
                                     String keyword2, String keyword3,  String remark, String url) {
        String templateId = "9NIDBqir8gpDR1PHCZfGjgvMx-avwwZ4QMfcr_B2PGw";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        WxMpTemplateData wxMpTemplateData3 = new WxMpTemplateData();
        wxMpTemplateData3.setName("remark");
        wxMpTemplateData3.setValue(remark);
        dataList.add(wxMpTemplateData3);

        WxMpTemplateData wxMpTemplateData4 = new WxMpTemplateData();
        wxMpTemplateData4.setName("keyword3");
        wxMpTemplateData4.setValue(keyword3);
        dataList.add(wxMpTemplateData4);
        sendTemplateMsg(openId, templateId, url, dataList);
    }

    /**
     * 患者添加医生
     */
    public void patientAddDoctor(String openId, String first, String keyword1,
                              String keyword2, String remark, String url) {
        String templateId = "Y3MxxxKiLCCqf6D2qn1nJwYOlKU2pf2_zFy0cz_YEcs";

        List<WxMpTemplateData> dataList = new ArrayList<>();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData();
        wxMpTemplateData.setName("first");
        wxMpTemplateData.setValue(first);
        dataList.add(wxMpTemplateData);
        WxMpTemplateData wxMpTemplateData1 = new WxMpTemplateData();
        wxMpTemplateData1.setName("keyword1");
        wxMpTemplateData1.setValue(keyword1);
        dataList.add(wxMpTemplateData1);
        WxMpTemplateData wxMpTemplateData2 = new WxMpTemplateData();
        wxMpTemplateData2.setName("keyword2");
        wxMpTemplateData2.setValue(keyword2);
        dataList.add(wxMpTemplateData2);
        dataList.add(new WxMpTemplateData("remark", "点击完善信息", "#FF0000"));
        sendTemplateMsg(openId, templateId, url, dataList);
    }
}