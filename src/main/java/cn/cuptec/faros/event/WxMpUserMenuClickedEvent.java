package cn.cuptec.faros.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Component;

/**
 * 公众号用户点击事件
 */
@Getter
@AllArgsConstructor
public class WxMpUserMenuClickedEvent {

    /**
     * 微信公众号用户
     */
    private final WxMpUser wxMpUser;

    //用户点击的按钮类型
    private final String menuType;

}
