package cn.cuptec.faros.config.wx.handler;

import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class UnsubscribeHandler extends AbstractHandler {

    @Resource
    private UserService userService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
        WxMpUser userWxInfo = wxMpService.getUserService()
                .userInfo(wxMessage.getFromUser(), null);
        if (userWxInfo != null) {
            User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getMpOpenId, wxMessage.getFromUser()));
            if (user != null){
                user.setIsSubscribe(false);
                userService.updateById(user);
            }
        }
        return null;
    }

}
