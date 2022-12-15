package cn.cuptec.faros.config.wx.handler;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.WxUtil;
import cn.cuptec.faros.config.wx.builder.TextBuilder;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.ProductService;
import cn.cuptec.faros.service.UserService;
import cn.cuptec.faros.service.WxScanService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SubscribeHandler extends AbstractHandler {

    private static final String URL_PREFIX = "http";
    private static final String JSON_ARRAY_PREFIX = "[{";

    @Resource
    private WxScanService wxScanService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService weixinService, WxSessionManager sessionManager) throws WxErrorException {
        return wxScanService.handle(wxMessage, context, weixinService, sessionManager);
    }



}
