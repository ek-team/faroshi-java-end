package cn.cuptec.faros.controller;

import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.dto.GetMaUrlLinkResult;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 小程序管理
 */
@RestController
@RequestMapping("/wxMa")
public class WxMaController {

    /**
     * 获取小程序urlLink
     */
    @GetMapping("/getMaUrlLink")
    public RestResponse getMaUrlLink(@RequestParam("query") String query) {
        try {
            String accessToken = WxMaConfiguration.getWxMaService().getAccessToken();
            //String accessToken = "63_vcfWInaGRmDLrsVDJtBgMojJqv96yhpfU0YroNWN0mudOYZMGzqu36KmARrXChcxIr_VTGbzmVR0A7Yarax7u8M13PPuZhm02QCAUFz7bnBC7yK7v8Iz0SF4QBAQLChABAMHY";
            String url = "https://api.weixin.qq.com/wxa/generate_urllink?access_token=" + accessToken;
            JSONObject paramMap = new JSONObject();
            //接口调用凭证
            //通过 URL Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
            paramMap.put("path", "pages/goodsDetail/goodsDetail");
            paramMap.put("query", query);
            //生成的 URL Link 类型，到期失效：true，永久有效：false
            paramMap.put("is_expire", false);
            //小程序 URL Link 失效类型，失效时间：0，失效间隔天数：1
            paramMap.put("expire_type", 1);
            //到期失效的URL Link的失效间隔天数。生成的到期失效URL Link在该间隔时间到达前有效。最长间隔天数为365天。expire_type 为 1 必填
            paramMap.put("expire_interval", 1);
            //执行post
            String result = HttpUtil.post(url, paramMap.toJSONString());
            GetMaUrlLinkResult getMaUrlLinkResult = JSONObject.parseObject(result, GetMaUrlLinkResult.class);

            return RestResponse.ok(getMaUrlLinkResult.getUrl_link());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    /**
     * 生成小程序二维码
     */
    @GetMapping("/getMaQrCOde")
    public RestResponse getMaQrCOde(@RequestParam("servicePackId") Integer servicePackId) {
            WxMaQrcodeService qrcodeService = WxMaConfiguration.getWxMaService().getQrcodeService();
        byte[] qrcodeBytes = new byte[0];
        try {
            qrcodeBytes = qrcodeService.createQrcodeBytes("/pages/goodsDetail/goodsDetail?id=" + servicePackId , 430);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return RestResponse.ok(qrcodeBytes);

    }





}
