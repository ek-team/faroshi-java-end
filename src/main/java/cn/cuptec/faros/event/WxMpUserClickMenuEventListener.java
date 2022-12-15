package cn.cuptec.faros.event;

import cn.cuptec.faros.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户点击公众号菜单异步处理事件
 */
@Slf4j
@AllArgsConstructor
@Component
public class WxMpUserClickMenuEventListener {

    private final UserService userService;

    @Async
    @Order
    @EventListener(WxMpUserMenuClickedEvent.class)
    public void bindOrSaveUser(WxMpUserMenuClickedEvent event) {
        WxMpUser wxMpUser = event.getWxMpUser();
        //click按钮点击事件
        if (event.getMenuType().equals(WxConsts.MenuButtonType.CLICK)){
            //更新或保存用户信息
            int refectCount = userService.saveUserOrUpdateMpOpenIdOnDuplicateUnionId(null, wxMpUser.getUnionId(), null, wxMpUser.getOpenId());
            log.info("用户点击公众号按钮菜单，更新用户mpOpenId结束，受影响条数：{}", refectCount);
        }
    }

}
