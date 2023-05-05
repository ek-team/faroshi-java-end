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
