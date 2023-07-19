package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    private final Url url;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;
    @Resource
    private DeptService deptService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private ExpressService expressService;

    @GetMapping("user")
    public RestResponse customUserInfo() {
        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
                .isNull(TbTrainUser::getDeptId)
        .isNotNull(TbTrainUser::getMacAdd));
        List<String> macAdds = tbTrainUsers.stream().map(TbTrainUser::getMacAdd)
                .collect(Collectors.toList());
        List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().lambda().in(ProductStock::getMacAddress, macAdds)
                .eq(ProductStock::getDel, 1));
        Map<String, List<ProductStock>> map = productStocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getMacAddress));
        for (TbTrainUser tbTrainUser : tbTrainUsers) {
            List<ProductStock> productStocks1 = map.get(tbTrainUser.getMacAdd());
            if (!CollectionUtils.isEmpty(productStocks1)) {
                tbTrainUser.setDeptId(productStocks1.get(0).getDeptId());
            }
        }
        planUserService.updateBatchById(tbTrainUsers);
        return RestResponse.ok();
    }


}
