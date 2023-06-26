package cn.cuptec.faros.config.security;

import cn.cuptec.faros.config.security.exception.resolver.ResourceAuthExceptionEntryPoint;
import cn.cuptec.faros.config.security.handler.MobileLoginFailureHandler;
import cn.cuptec.faros.config.security.handler.MobileLoginSuccessHandler;
import cn.cuptec.faros.config.security.service.CustomUserDetailsService;
import cn.cuptec.faros.config.security.social.SocialSecurityConfigurer;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    protected ResourceAuthExceptionEntryPoint resourceAuthExceptionEntryPoint;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientDetailsService clientDetailsService;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Lazy
    @Autowired
    private AuthorizationServerTokenServices defaultAuthorizationServerTokenServices;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/index",
                        "/portal/**",
                        "/ws/**",
                        "/social/token",
                        "/product/**",
                        "/city",
                        "/file/**",
                        "/city/**",
                        "/brand/**",
                        "/liveQrCode/dispatcher/**",
                        "/liveQrCode/dispatcherNaLi/**",
                        "/sms/**",
                        "/protocols/getById/**",
                        "/user/salesmanContactInfo/**",
                        "/sys/getTime",
                        "/mp/qrcode/**",
                        "/user/srBindAdress/**",
                        "/mp/menu/preset",
                        "/retrieveOrder/manage/userConfirmPostMoneySendMessage",
                        "/pay/**",
                        "/im/callback",
                        "/mp/menu/**",
                        "/planUserTrainRecord/trainRecordByPhone",
                        "/trainData/listByRecordId",
                        "/palnUser/getByPhoneAndIdCard",
                        "/plan/listGroupByPhoneAndIdCard",
                        "/plan/updateList",
                        "/test/**",
                        "/plan/excel/**",
                        "/article/getShowContent",
                        "/palnUser/bindNaLiSynInfo",
                        "/palnUser/cleanBindInfo",
                        "/palnUser/checkIdCard",
                        "/deviceScanSignLog/list",
                        "/palnUser/saveBatch",
                        "/productStock/updateVersion",
                        "/palnUser/naLiRecycle",
                        "/retrieveOrder/kuaidicallback",
                        "/file/upload",
                        "/devicelog/add",
                        "/HospitalInfo/list",
                        "/plan/listByxtUserId/**",
                        "/evaluationRecords/getEvaluationRecords",
                        "/evaluationRecords/addEvaluationRecords",
                        "/liveQrCode/getLiveQrCodeUrl",
                        "/evaluationRecords/listGroupByPhoneAndIdCard",
                        "/subPlan/update",
                        "/subPlan/saveOrUpdate",
                        "/subPlan/listByUserId",
                        "/subPlan/listPlanByUserId",
                        "/subPlan/delete",
                        "/subPlan/addHistory",
                        "/plan/listByUid/**",
                        "/subPlan/old",
                        "/subPlan/getOne",
                        "/subPlan/getOneByIdCard",
                        "/subPlan/oldIdCard",
                        "/plan/save",
                        "/purchase/order/test",
                        "/product/queryByCategory",
                        "/product/user/getById",
                        "/subPlan/add",
                        "/purchase/order/naliExport",
                        "/palnUser/backupInfo",
                        "/file/downloadProductStockInfo",
                        "/file/uploadProductStockInfo",
                        "/palnUser/registerPlanUser/**",
                        "/subPlan/batchUpdate",
                        "/subPlan/addDoctorUpdateSubPlanRecord",
                        "/updateSubPlanRecord/save",
                        "/subPlan/addInitSubPlan",
                        "/pneumaticRecord/save",
                        "/pneumaticRecord/getByUserId",
                        "/pneumaticRecord/clearRecordByPlanDayTime",
                        "/pneumaticRecord/getDataByPlanDayTime",
                        "/pneumaticRecord/pneumaticRecordPage/**",
                        "/pneumaticPlan/save",
                        "/pneumaticPlan/getByUserId",
                        "/subPlan/querySubPlanByUserId",
                        "/planUserTrainRecord/getTrainStepCount",
                        "/pneumaticRecord/getByUserIdData",
                        "/planUserTrainRecord/getTrainRecord",
                        "/pneumaticRecord/clearRecordByPlanUserId",
                        "/pneumaticPlan/deleteByTime",
                        "/questionnaire/add",
                        "/questionnaire/getByIdCard",
                        "/questionnaire/getByUserIdGroupDetail",
                        "/xPic/getByIdCard",
                        "/xPic/getByIdCardAndTime",
                        "/palnUser/getUserInfoByUserId",
                        "/palnUser/updateByUserId",
                        "/palnUser/updateById",
                        "/pneumaticEvaluationRecords/addBatch",
                        "/pneumaticEvaluationRecords/getRecordsByUserId",
                        "/pneumaticEvaluationRecords/deleteRecordsByUserId",
                        "/wxpay/notifyOrder",
                        "/purchase/order/deliveryMoBan",
                        "/purchase/order/exportOrder",
                        "/userServicePackageInfo/listByIdCard",
                        "/wxpay/notifyRefunds",
                        "/purchase/order/user/orderDetailByOrderNo",
                        "/followUpPlan/testRedis",
                        "/form/exportFormUserData",
                        "/userGroup/checkGroupHavePatient",
                        "/planUserTrainRecord/save",
                        "/insertTemplate/insert",
                        "/user/register",
                        "/deviceScanSignLog/getByMacAdd",
                        "/palnUser/newSaveBatch",
                        "/deviceScanSignLog/saveOther",
                        "/doctorTeam/getByMacAdd",
                        "/productStock/getByMacAddress",
                        "/palnUser/export",
                        "/deviceScanSignLog/removeByMacAddress",
                        "/productStock/getByMacAddressHospitalInfo",
                        "/productStock/getByMac/**",
                        "/user/checkUserUseMa",
                        "/planUserTrainRecord/getByIdCard",
                        "/xPic/uploadXPian",
                        "/alipay/notifyAliOrder",
                        "/uniAppPush/send",
                        "/alipay/pay",
                        "/alipay/payqrcode",
                        "/productStock/updateDataByMacAdd",
                        "/palnUser/getPlanStatus",
                        "/palnUser/updatePlanStatus",
                        "/subPlan/updatePlanInvalid",
                        "/user/saveLoginLog"

                )
                .permitAll()
                .anyRequest()
                .authenticated()
                .and().csrf().disable()
                .apply(mobileSecurityConfigurer())

        ;
    }

    @Bean
    public SocialSecurityConfigurer mobileSecurityConfigurer() {
        SocialSecurityConfigurer socialSecurityConfigurer = new SocialSecurityConfigurer();
        socialSecurityConfigurer.setMobileLoginSuccessHandler(mobileLoginSuccessHandler());
        socialSecurityConfigurer.setMobileLoginFailureHandler(mobileLoginFailureHandler());
        socialSecurityConfigurer.setUserDetailsService(userDetailsService);
        return socialSecurityConfigurer;
    }

    @Bean
    public AuthenticationSuccessHandler mobileLoginSuccessHandler() {
        return MobileLoginSuccessHandler.builder()
                .objectMapper(objectMapper)
                .clientDetailsService(clientDetailsService)
                .passwordEncoder(passwordEncoder())
                .defaultAuthorizationServerTokenServices(defaultAuthorizationServerTokenServices).build();
    }

    @Bean
    public AuthenticationFailureHandler mobileLoginFailureHandler() {
        return MobileLoginFailureHandler.builder()
                .objectMapper(objectMapper)
                .build();
    }

    /**
     * https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-storage-updated
     * Encoded password does not look like BCrypt
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.authenticationEntryPoint(resourceAuthExceptionEntryPoint);
    }


    public static void main(String[] args) {
        String body = HttpRequest.get("http://vc6y35.natappfree.cc/oauth/token?grant_type=client_credentials")
                .header("Authorization", "Basic dGVzdDp0ZXN0").execute().body();
        System.out.println(body);
    }


}
