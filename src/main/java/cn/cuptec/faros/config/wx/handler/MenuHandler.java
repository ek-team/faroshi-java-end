package cn.cuptec.faros.config.wx.handler;

import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.event.WxMpUserMenuClickedEvent;
import cn.cuptec.faros.service.UserOrdertService;
import cn.cuptec.faros.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.MenuButtonType;

@Slf4j
@Component
@AllArgsConstructor
public class MenuHandler extends AbstractHandler {

    private final ApplicationEventPublisher publisher;
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService weixinService, WxSessionManager sessionManager) {

        String msg = String.format("type:%s, event:%s, key:%s",
                wxMessage.getMsgType(), wxMessage.getEvent(),
                wxMessage.getEventKey());
        if (MenuButtonType.VIEW.equals(wxMessage.getEvent())) {
            return null;
        } else if (MenuButtonType.CLICK.toUpperCase().equals(wxMessage.getEvent())) {
            String fromUser = wxMessage.getFromUser();
            log.info("按钮点击事件--------------" + wxMessage.getEventKey() + "========" + fromUser);
            User user = userService.getBaseMapper().getMpOpenIdIsExist(fromUser);
            if (user != null) {
                log.info("回复用户消息");
                String content =
                        "全国售后服务电话" +
                                "\n" +
                                "4009001022" +
                                "\n" +
                                "上海售后服务电话" +
                                "\n" +
                                "13816158143";

                return WxMpXmlOutMessage.TEXT().content(content)
                        .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
                        .build();

            }

            try {
                WxMpUser wxMpUser = WxMpConfiguration.getWxMpService().getUserService().userInfo(fromUser);
                //发布按钮点击事件
                WxMpUserMenuClickedEvent wxMpUserMenuClickedEvent = new WxMpUserMenuClickedEvent(wxMpUser, MenuButtonType.CLICK);
                publisher.publishEvent(wxMpUserMenuClickedEvent);


            } catch (WxErrorException e) {
                log.error("Menu click handler get user info error: {}", e.getError().getErrorMsg(), e);
            }
        }

//		return WxMpXmlOutMessage.TEXT().content(msg)
//				.fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
//				.build();
        return null;
    }

}
