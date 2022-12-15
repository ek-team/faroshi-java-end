package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.config.wx.WxMpContextHolder;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.web.bind.annotation.*;

@Api(tags = "微信接入入口", hidden = true)
@Slf4j
@RestController
@RequestMapping("/portal/mp")
public class WxMpPortalController {

	/**
	 * @param signature 微信签名
	 * @param timestamp 时间戳
	 * @param nonce     随机数
	 * @param echostr   随机字符串
	 * @return
	 */
	@ApiOperation(value = "微信接入校验处理")
	@GetMapping(produces = "text/plain;charset=utf-8")
	public String authGet(@RequestParam(name = "signature", required = false) String signature,
						  @RequestParam(name = "timestamp", required = false) String timestamp,
						  @RequestParam(name = "nonce", required = false) String nonce,
						  @RequestParam(name = "echostr", required = false) String echostr) {

		log.info("接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature, timestamp, nonce, echostr);

		if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
			throw new IllegalArgumentException("请求参数非法，请核实!");
		}

		final WxMpService wxService = WxMpConfiguration.getWxMpService();

		if (wxService == null) {
			throw new IllegalArgumentException(String.format("未找到微信的配置，请核实！"));
		}

		if (wxService.checkSignature(timestamp, nonce, signature)) {
			return echostr;
		}

		return "非法请求";
	}

	/**
	 *
	 *
	 * @param requestBody  请求报文体
	 * @param signature    微信签名
	 * @param encType      加签方式
	 * @param msgSignature 微信签名
	 * @param timestamp    时间戳
	 * @param nonce        随机数
	 * @return
	 */
	@ApiOperation(value = "微信消息处理")
	@PostMapping(produces = "application/xml; charset=UTF-8")
	public String post(@RequestBody String requestBody,
					   @RequestParam("signature") String signature,
					   @RequestParam("timestamp") String timestamp,
					   @RequestParam("nonce") String nonce,
					   @RequestParam("openid") String openid,
					   @RequestParam(name = "encrypt_type", required = false) String encType,
					   @RequestParam(name = "msg_signature", required = false) String msgSignature) {

		final WxMpService wxService = WxMpConfiguration.getWxMpService();

		log.info("接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
						+ " timestamp=[{}], nonce=[{}], requestBody=[{}] ",
				openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

		if (!wxService.checkSignature(timestamp, nonce, signature)) {
			throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
		}

		String out = null;
		if (requestBody.startsWith("[")){
			requestBody = StringUtils.trimstart(requestBody, "[");
		}
		if (requestBody.endsWith("]")){
			requestBody = StringUtils.trimend(requestBody, "]");
		}
		// 明文模式
		if (StrUtil.isBlank(encType)) {
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
			WxMpXmlOutMessage outMessage = WxMpConfiguration.getRouter().route(inMessage);
			if (outMessage != null) {
				out = outMessage.toXml();
			}
		}

		// aes加密模式
		if (SecurityConstants.AES.equalsIgnoreCase(encType)) {
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
					timestamp, nonce, msgSignature);

			log.debug("消息解密后内容为：{} ", inMessage.toString());

			WxMpXmlOutMessage outMessage = WxMpConfiguration.getRouter().route(inMessage);
			if (outMessage != null) {
				out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
			}
		}

		log.debug("组装回复信息：{}", out);
		WxMpContextHolder.clear();
		return out;
	}

}
