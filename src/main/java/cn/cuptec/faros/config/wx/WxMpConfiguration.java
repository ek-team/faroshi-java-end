package cn.cuptec.faros.config.wx;

import cn.cuptec.faros.config.wx.handler.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static me.chanjar.weixin.common.api.WxConsts.*;

/**
 * 微信公众号基础配置，初始化配置
 */
@Configuration
@AllArgsConstructor
public class WxMpConfiguration {

    //region handler
	private final LogHandler logHandler;
	private final NullHandler nullHandler;
	private final KfSessionHandler kfSessionHandler;
	private final StoreCheckNotifyHandler storeCheckNotifyHandler;
//	private final LocationHandler locationHandler;
	private final MenuHandler menuHandler;
	private final MsgHandler msgHandler;
	private final UnsubscribeHandler unsubscribeHandler;
	private final SubscribeHandler subscribeHandler;
	private final ScanHandler scanHandler;
    //endregion

    private final WxMpProperties wxMpProperties;

    @Getter
	private static WxMpMessageRouter router;

	@Getter
	private static WxMpService wxMpService;


	@PostConstruct
	public void initService() {
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(wxMpProperties.getAppId());
        configStorage.setSecret(wxMpProperties.getSecret());
        configStorage.setToken(wxMpProperties.getToken());
        configStorage.setAesKey(wxMpProperties.getAesKey());

        wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(configStorage);
        router = this.newRouter(wxMpService);
	}

	private WxMpMessageRouter newRouter(WxMpService wxMpService) {
		final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

		// 记录所有事件的日志 （异步执行）
		newRouter.rule().handler(this.logHandler).next();

		// 接收客服会话管理事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION)
				.handler(this.kfSessionHandler).end();
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION)
				.handler(this.kfSessionHandler)
				.end();
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION)
				.handler(this.kfSessionHandler).end();

		// 门店审核事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.POI_CHECK_NOTIFY)
				.handler(this.storeCheckNotifyHandler).end();

		// 自定义菜单事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(MenuButtonType.CLICK).handler(this.menuHandler).end();

		// 点击菜单连接事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(MenuButtonType.VIEW).handler(this.nullHandler).end();

		// 关注事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(EventType.SUBSCRIBE).handler(this.subscribeHandler)
				.end();

		// 取消关注事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(EventType.UNSUBSCRIBE)
				.handler(this.unsubscribeHandler).end();

		// 上报地理位置事件
//		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
//				.event(EventType.LOCATION).handler(this.locationHandler)
//				.end();

		// 接收地理位置消息
//		newRouter.rule().async(false).msgType(XmlMsgType.LOCATION)
//				.handler(this.locationHandler).end();

		// 扫码事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(EventType.SCAN).handler(this.scanHandler).end();

		// 默认
		newRouter.rule().async(false).handler(this.msgHandler).end();

		return newRouter;
	}

}
