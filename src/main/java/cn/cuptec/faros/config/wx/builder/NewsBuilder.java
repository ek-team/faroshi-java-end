package cn.cuptec.faros.config.wx.builder;

import com.alibaba.fastjson.JSON;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;

public class NewsBuilder {

    public WxMpXmlOutMessage build(WxMpXmlMessage wxMessage, WxMpService service, WxMpXmlOutNewsMessage.Item...items) {
        WxMpXmlOutNewsMessage m = WxMpXmlOutMessage.NEWS()
                .addArticle(items)
                .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
                .build();

        return m;

    }

    public static void main(String[] args) {
        WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
//        item.setUrl("http://");
        item.setTitle("Hi,人见人爱的小海螺在此");
        item.setPicUrl("http://ewj100.oss-cn-hangzhou.aliyuncs.com/imgs/product/935e7256ff804edea99e1a979860f173%23%E4%B8%BB%E5%9B%BE.jpg?x-oss-process=style/imgsWithAbsoluteSizeAndWaterFlow");
        item.setDescription("\n" +
                "\n" +
                "<a href=\"http://ctm.ewj100.com/postsale/index.html#afterSales\">→我要维修</a>\n" +
                "\n" +
                "<a href=\"http://ctm.ewj100.com/postsale/index.html#feedback\">→问题与建议</a>\n" +
                "\n" +
                "如需以上服务可直接戳蓝色字体，法罗适随时陪伴在您左右");
        System.out.println(JSON.toJSONString(item));

    }

}
