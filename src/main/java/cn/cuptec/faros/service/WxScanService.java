package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.WxUtil;
import cn.cuptec.faros.config.wx.builder.NewsBuilder;
import cn.cuptec.faros.config.wx.builder.NewsItemBuilder;
import cn.cuptec.faros.config.wx.builder.TextBuilder;
import cn.cuptec.faros.controller.WxMpMenuController;
import cn.cuptec.faros.entity.*;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WxScanService {

    @Resource
    private WxMpTagService wxMpTagService;
    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private ProductStockService productStockService;

    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService weixinService, WxSessionManager sessionManager) {
        String wxMessageEventKey = WxUtil.getWxMessageEventKey(wxMessage.getEventKey());
        String event = wxMessage.getEvent();
        log.info("扫码消息:" + event);
        log.info("扫码消息====:" + wxMessageEventKey);
        // TODO 此处简单处理场景
        String[] split = wxMessageEventKey.split(";");
        ServicePack byId = null;
        String token = "";
        if (split.length >= 3) {
            if (split[2].equals("servicePack")) {
                Integer servicePackId = Integer.parseInt(split[0]);
                token = split[1];
                log.info("场景值：" + wxMessageEventKey + "=====" + event);
                log.info("服务包id：" + servicePackId + "=====" + event);

                byId = servicePackService.getById(servicePackId);
            }
        }

        Integer doctorId = null;
        User doctor = null;
        if (split.length >= 3) {
            if (split[2].equals("addPatient")) {
                token = split[1];
                doctorId = Integer.parseInt(split[0]);
                doctor = userService.getById(doctorId);

            }
        }
        Integer teamId = null;
        DoctorTeam doctorTeam = null;
        if (split.length >= 3) {
            if (split[2].equals("addTeam")) {
                token = split[1];
                teamId = Integer.parseInt(split[0]);
                doctorTeam = doctorTeamService.getById(teamId);

            }
        }
        // 获取微信用户基本信息
        WxMpUser userWxInfo = null;
        try {
            userWxInfo = weixinService.getUserService()
                    .userInfo(wxMessage.getFromUser(), null);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        if (userWxInfo != null) {
            log.info("扫码关注公众号====" + userWxInfo.getUnionId());
            //添加关注用户到本地数据库
            User user = userService.getBaseMapper().getUnionIdIsExist(userWxInfo.getUnionId());

            if (user == null) {
                log.info("用户不存在，将新增");
                User u = new User();
                u.setPhone(userWxInfo.getOpenId());
                u.setMpOpenId(userWxInfo.getOpenId());
                u.setUnionId(userWxInfo.getUnionId());
                u.setNickname(userWxInfo.getNickname());
                u.setAvatar(userWxInfo.getHeadImgUrl());
                u.setGender(userWxInfo.getSexDesc());
                u.setProvince(userWxInfo.getProvince());
                u.setCity(userWxInfo.getCity());
                u.setLanguage(userWxInfo.getLanguage());
                u.setIsSubscribe(true);
                u.setDelFlag(CommonConstants.STATUS_NORMAL);
                if (user == null) userService.save(u);
                else {
                    u.setDelFlag(CommonConstants.STATUS_NORMAL);
                    u.setLockFlag(CommonConstants.STATUS_NORMAL);
                    userRoleService.deleteByUserId(user.getId());
                    u.setId(user.getId());
                    userService.getBaseMapper().updateUserById(u);
                }

            } else {

                user.setIsSubscribe(true);
                userService.updateById(user);
                //查询该用户的权限
                List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                        .in(UserRole::getUserId, user.getId()));

                wxMpTagService.batchTaggings(userRoles, user.getId());
            }
            if (byId != null) {
                wxMpService.sendSubNotice(user.getMpOpenId(), "扫码成功", byId.getName(), "法罗适",
                        "点击查看详情", "/pages/goodsDetail/goodsDetail?id=" + byId.getId() + "&token=" + token);

            }
            if (doctor != null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = df.format(now);
                wxMpService.patientAddDoctor(user.getMpOpenId(), "您添加医生成功", doctor.getNickname(), time,
                        "点击查看详情", "/pages/savePersonInfo/savePersonInfo?token=" + token + "&doctorId=" + doctor.getId());
            }
            if (doctorTeam != null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = df.format(now);
                wxMpService.patientAddDoctor(user.getMpOpenId(), "您添加医生成功", doctorTeam.getName(), time,
                        "点击查看详情", "/pages/savePersonInfo/savePersonInfo?token=" + token + "&doctorTeamId=" + doctorTeam.getId());
            }
        }

        if ("subscribe".equals(event)) {//关注消息
//            User user = userService.getBaseMapper().getMpOpenIdIsExist(userWxInfo.getOpenId());
//            if (user != null) {
//                String macAdd = user.getMacAdd();
//                ProductStock byMac = productStockService.getByMac(macAdd);
//                if (byMac.getServicePackId() != null) {
//                    ServicePack servicePack = servicePackService.getById(byMac.getServicePackId());
//                    wxMpService.sendSubNotice(user.getMpOpenId(), "扫码成功", servicePack.getName(), "法罗适",
//                            "点击查看详情", "/pages/goodsDetail/goodsDetail?id=" + servicePack.getId());
//                }
//
//            }
            log.info("关注消息：" + wxMessageEventKey + "=====" + event);
            // wxMpService.sendSubNotice(userWxInfo.getOpenId(), "扫码成功", byId.getName(), "法罗适", "点击查看详情", "/pages/orderConfirm/orderConfirm?id=1");
            String content = "\n" +
                    "<a href=\"https://pharos3.ewj100.com/index.html#/product/homeEdition\">→视频指导</a>\n" +
                    "\n" +
                    "<a href=\"http://mp.weixin.qq.com/mp/homepage?__biz=MzUyNDkxMzMyNw==&hid=5&sn=b5fe982b9c9e10f2d1f9e35e2f4337f5&scene=18#wechat_redirect\">→学术分享</a>\n" +
                    "\n" +
                    "法罗适医康随时陪伴在您左右";
            return new TextBuilder().build(content, wxMessage, weixinService);


        }
        WxMpXmlOutMessage responseResult = null;
        return responseResult;
    }

    public static boolean isNumericZidai(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {

        System.out.println(isNumericZidai(""));
    }

}
