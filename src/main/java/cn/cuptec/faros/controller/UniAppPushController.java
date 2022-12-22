package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.service.UniAppPushService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 手机app推送消息
 */
@RestController
@RequestMapping("/uniAppPush")
public class UniAppPushController {
    @Resource
    private UniAppPushService uniAppPushService;

    @GetMapping("/send")
    public RestResponse send(@RequestParam("title") String title,
                             @RequestParam("body") String body,
                             @RequestParam("userId") String userId,
                             @RequestParam("url") String url) {
        uniAppPushService.send(title, body, userId, url);
        return RestResponse.ok();
    }
}
