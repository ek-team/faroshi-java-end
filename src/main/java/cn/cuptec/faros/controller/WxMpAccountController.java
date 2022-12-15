package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpDataCubeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeArticleResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeInterfaceResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeMsgResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserCumulate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mp/account")
public class WxMpAccountController {


    /**
     * 获取公众号统计数据
     * @param interval 时间间隔
     * @return
     */
    @GetMapping("/statistics")
    public RestResponse statistic(String interval){
        String[] split = interval.split(StrUtil.DASHED);
        Date start = new Date(Long.parseLong(split[0]));
        Date end = new Date(Long.parseLong(split[1]));

        WxMpService wxMpService = WxMpConfiguration.getWxMpService();
        WxMpDataCubeService cubeService = wxMpService.getDataCubeService();

        List<List<Object>> result = new ArrayList<>();
        try {
            // 获取累计用户数据
            List<Object> cumulateList = cubeService.getUserCumulate(start, end).stream()
                    .map(WxDataCubeUserCumulate::getCumulateUser).collect(Collectors.toList());
            result.add(cumulateList);

            // 获取用户分享数据
            List<Object> shareList = cubeService.getUserShare(start, end).stream()
                    .map(WxDataCubeArticleResult::getShareCount).collect(Collectors.toList());
            result.add(shareList);

            // 获取消息发送概况数据
            List<Object> upstreamList = cubeService.getUpstreamMsg(start, end).stream()
                    .map(WxDataCubeMsgResult::getMsgCount).collect(Collectors.toList());
            result.add(upstreamList);

            // 获取接口调用概况数据
            List<WxDataCubeInterfaceResult> interfaceSummaryList = cubeService.getInterfaceSummary(start, end);
            List<Object> interfaceList = interfaceSummaryList.stream().map(WxDataCubeInterfaceResult::getCallbackCount)
                    .collect(Collectors.toList());
            result.add(interfaceList);

            // 接口日期保存
            List<Object> dateList = interfaceSummaryList.stream().map(WxDataCubeInterfaceResult::getRefDate)
                    .collect(Collectors.toList());
            result.add(dateList);
        }
        catch (WxErrorException e) {
            log.error(" 获取公众号统计数据报错", e);
            return RestResponse.failed("获取公众号数据失败:" + e.getError().getErrorMsg());
        }

        return RestResponse.ok();
    }

}
