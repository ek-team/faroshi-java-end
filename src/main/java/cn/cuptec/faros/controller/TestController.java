package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
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
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private ReviewRefundOrderController reviewRefundOrderController;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserService userService;
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @GetMapping("user")
    public RestResponse customUserInfo() {
        List<UserRole> userRoles = userRoleService.list(new QueryWrapper<UserRole>().lambda().eq(UserRole::getRoleId, 20));
        List<Integer> userIds = userRoles.stream().map(UserRole::getUserId)
                .collect(Collectors.toList());
        List<User> users = (List<User>) userService.listByIds(userIds);
        for(User user:users){
            if(!StringUtils.isEmpty(user.getPhone()) && user.getPhone().length()>=10){
                String substring = user.getPhone().substring(5, 11);
                if (!ENCODER.matches(substring,user.getPassword())){
                    System.out.println(user.getPhone());
                }
            }

        }
        return RestResponse.ok();
    }

    public static void main(String[] args) {
        boolean matches = ENCODER.matches("094731","$2a$10$LxMOH83G2JIbObfh8ytjO.HkZDbjQpr3FUaYowarf76G2gV4Gvxby");
        System.out.println(matches);
    }
}
