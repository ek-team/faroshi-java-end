package cn.cuptec.faros.config.wx.handler;

import cn.cuptec.faros.config.wx.builder.NewsBuilder;
import cn.cuptec.faros.config.wx.builder.TextBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Slf4j
@Component
public class MsgHandler extends AbstractHandler {

    @Override
    @SneakyThrows
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {

        if (!wxMessage.getMsgType().equals(XmlMsgType.EVENT)) {
            //TODO 可以选择将消息保存到本地
        }

        //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
        if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服")
                && weixinService.getKefuService().kfOnlineList()
                .getKfOnlineList().size() > 0) {
            return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE()
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser()).build();
        }
        //TODO 组装回复消息
//		String content = "\n" +
//				"\n" +
//				"<a href=\"http://ctm.ewj100.com/postsale/index.html#afterSales\">→我要维修</a>\n" +
//				"\n" +
//				"<a href=\"http://ctm.ewj100.com/postsale/index.html#feedback\">→问题与建议</a>\n" +
//				"\n" +
//				"如需以上服务可直接戳蓝色字体，法罗适随时陪伴在您左右";
//		log.info("消息内容"+wxMessage.getToUser()+"[[[[["+wxMessage.getFromUser());
//		return new TextBuilder().build(content, wxMessage, weixinService);
        WxMpXmlOutMessage responseResult = null;
        return responseResult;
    }

    public static void main(String[] args) {
        WxMpXmlMessage wxMessage = new WxMpXmlMessage();
        wxMessage.setToUser("obp8I69Ms2LPENT7MtvDGufyGqd4");
        wxMessage.setFromUser("obp8I69Ms2LPENT7MtvDGufyGqd4");
        WxMpService weixinService = new WxMpServiceImpl();
        String content = "\n" +
                "\n" +
                "<a href=\"http://ctm.ewj100.com/postsale/index.html#afterSales\">→我要维修</a>\n" +
                "\n" +
                "<a href=\"http://ctm.ewj100.com/postsale/index.html#feedback\">→问题与建议</a>\n" +
                "\n" +
                "如需以上服务可直接戳蓝色字体，法罗适随时陪伴在您左右";
        WxMpXmlOutMessage build = new TextBuilder().build(content, wxMessage, weixinService);
    }

}
