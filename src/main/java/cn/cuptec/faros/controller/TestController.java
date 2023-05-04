package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.service.HospitalInfoService;
import cn.cuptec.faros.service.PlanService;
import cn.cuptec.faros.service.PlanUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    private final Url url;
    @Resource
    private ClientDetailsService clientDetailsService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @GetMapping("user")
    public RestResponse customUserInfo() {
        String content =
                "<a href=\"https://pharos3.ewj100.com/index.html#/product/homeEdition\">→视频指导</a>\n" +

                "<a href=\"http://mp.weixin.qq.com/mp/homepage?__biz=MzUyNDkxMzMyNw==&hid=5&sn=b5fe982b9c9e10f2d1f9e35e2f4337f5&scene=18#wechat_redirect\">→学术分享</a>\n"
                ;
        wxMpService.sendDoctorUrlTip("oV8W46Jr8-9S-8aDSQ4Mcigwbwms", "您的客户已成功下单，请您尽快处理！", "1", "1",
                content, "https://pharos3.ewj100.com/record.html#/ucenter/recovery/externalLink");


//        List<HospitalInfo> list = hospitalInfoService.list();
//        for (HospitalInfo importDoctor : list) {
//            String hospitalInfoStr = importDoctor.getProvince() + importDoctor.getCity() + importDoctor.getArea() + importDoctor.getName();
//
//            importDoctor.setHospitalInfoStr(hospitalInfoStr);
//        }
//        hospitalInfoService.updateBatchById(list);

//        List<TbTrainUser> list = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
//
//                .isNull(TbTrainUser::getIdCard).or().eq(TbTrainUser::getIdCard, ""));
//        for (TbTrainUser tbTrainUser : list) {
//            tbTrainUser.setIdCard(tbTrainUser.getCaseHistoryNo());
//        }
//        planUserService.updateBatchById(list);
        return RestResponse.ok();
    }


}
