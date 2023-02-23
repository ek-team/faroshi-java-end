package cn.cuptec.faros.config.job;

import cn.cuptec.faros.config.properties.RedisConfigProperties;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * redis过期监听
 * 1、自动取消订单
 * 2、自动收货
 */
@Slf4j
@Component
public class RedisKeyExpirationListener implements MessageListener {

    private RedisTemplate<String, String> redisTemplate;
    private RedisConfigProperties redisConfigProperties;
    private UserOrdertService userOrdertService;
    private ChatUserService chatUserService;
    private WxMpService wxMpService;
    private FollowUpPlanNoticeService followUpPlanNoticeService;//随访计划通知模版
    private UserService userService;
    private ChatMsgService chatMsgService;
    private PatientOtherOrderService patientOtherOrderService;
    private HospitalInfoService hospitalInfoService;
    private FollowUpPlanNoticeCountService followUpPlanNoticeCountService;
    private DeptService deptService;
    private DoctorTeamService doctorTeamService;
    public static final String URL = "/pages/orderConfirm/orderConfirm?id=";

    public RedisKeyExpirationListener(RedisTemplate<String, String> redisTemplate,
                                      RedisConfigProperties redisConfigProperties,
                                      UserOrdertService userOrdertService,
                                      ChatUserService chatUserService,
                                      FollowUpPlanNoticeService followUpPlanNoticeService,
                                      WxMpService wxMpService,
                                      UserService userService,
                                      HospitalInfoService hospitalInfoService,
                                      ChatMsgService chatMsgService,
                                      FollowUpPlanNoticeCountService followUpPlanNoticeCountService,
                                      PatientOtherOrderService patientOtherOrderService,
                                      DeptService deptService,
                                      DoctorTeamService doctorTeamService
    ) {
        this.redisTemplate = redisTemplate;
        this.redisConfigProperties = redisConfigProperties;
        this.userOrdertService = userOrdertService;
        this.chatUserService = chatUserService;
        this.followUpPlanNoticeService = followUpPlanNoticeService;
        this.wxMpService = wxMpService;
        this.userService = userService;
        this.hospitalInfoService = hospitalInfoService;
        this.chatMsgService = chatMsgService;
        this.followUpPlanNoticeCountService = followUpPlanNoticeCountService;
        this.patientOtherOrderService = patientOtherOrderService;
        this.deptService = deptService;
        this.doctorTeamService = doctorTeamService;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        RedisSerializer<?> serializer = redisTemplate.getValueSerializer();
        String channel = String.valueOf(serializer.deserialize(message.getChannel()));
        String body = String.valueOf(serializer.deserialize(message.getBody()));
        log.info("key过期监听进入//////////////////////////////////////////////" + body);
        //key过期监听
        if (StrUtil.format("__keyevent@{}__:expired", redisConfigProperties.getDatabase()).equals(channel)) {
            if (body.contains("followUpPlanNotice:")) {
                String[] str = body.split(":");
                String followUpPlanNoticeId = str[1];
                log.info("redis过期监听：：=============" + followUpPlanNoticeId);
                FollowUpPlanNotice followUpPlanNotice = followUpPlanNoticeService.getById(followUpPlanNoticeId);
                if (followUpPlanNotice.getStatus().equals(1)) {
                    return;
                }
                followUpPlanNotice.setStatus(1);
                followUpPlanNoticeService.updateById(followUpPlanNotice);
                User patientUser = userService.getById(followUpPlanNotice.getPatientUserId());
                User doctorUser = userService.getById(followUpPlanNotice.getDoctorId());


                //查询当前医生和患者所在的团队
                List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                        .eq(ChatUser::getTargetUid, followUpPlanNotice.getPatientUserId())
                        .like(ChatUser::getUserIds, followUpPlanNotice.getDoctorId() + "")
                        .eq(ChatUser::getGroupType, 1)
                        .isNotNull(ChatUser::getTeamId));
                List<User> users = new ArrayList<>();
                users.add(doctorUser);
                hospitalInfoService.getHospitalByUser(users);
                doctorUser = users.get(0);
                if (!CollectionUtils.isEmpty(chatUsers)) {
                    ChatUser chatUser = chatUsers.get(0);
                    ChatMsg chatMsg = new ChatMsg();
                    chatMsg.setChatUserId(chatUser.getId());
                    chatMsg.setFromUid(doctorUser.getId());

//                    chatMsg.setToUid(patientUser.getId());
                    chatMsg.setMsg("随访计划提醒");
                    chatMsg.setCreateTime(new Date());
                    chatMsg.setMsgType(ChatProto.FOLLOW_UP_PLAN);
                    chatMsg.setStr1(followUpPlanNoticeId);
                    chatMsg.setReadStatus(0);
                    chatMsg.setReadUserIds(followUpPlanNotice.getDoctorId() + "");
                    chatMsgService.save(chatMsg);
                    chatUser.setLastChatTime(new Date());
                    chatUser.setLastMsg("随访计划");
                    chatUserService.updateById(chatUser);
                    DoctorTeam byId = doctorTeamService.getById(chatUser.getTeamId());
                    wxMpService.sendFollowUpPlanNotice(patientUser.getMpOpenId(), "新的康复计划提醒", byId.getName()+"的随访", doctorUser.getHospitalName(), "/pages/news/news");

                } else {


                    //发送公众号随访计划提醒
                    wxMpService.sendFollowUpPlanNotice(patientUser.getMpOpenId(), "新的康复计划提醒", doctorUser.getNickname(), doctorUser.getHospitalName(), "/pages/news/news");

                    //生成聊天记录
                    List<ChatUser> list = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                            .eq(ChatUser::getUid, patientUser.getId())
                            .eq(ChatUser::getTargetUid, doctorUser.getId()));
                    if (CollectionUtils.isEmpty(list)) {
                        //创建聊天对象
                        chatUserService.saveOrUpdateChatUser(doctorUser.getId(), patientUser.getId(), "随访计划提醒");
                    }
                    ChatMsg chatMsg = new ChatMsg();
                    chatMsg.setFromUid(doctorUser.getId());
                    chatMsg.setToUid(patientUser.getId());
                    chatMsg.setMsg("随访计划提醒");
                    chatMsg.setCreateTime(new Date());
                    chatMsg.setMsgType(ChatProto.FOLLOW_UP_PLAN);
                    chatMsg.setStr1(followUpPlanNoticeId);
                    chatMsg.setReadStatus(0);
                    chatMsgService.save(chatMsg);
                }


                //修改发送次数
                FollowUpPlanNoticeCount one = followUpPlanNoticeCountService.getOne(new QueryWrapper<FollowUpPlanNoticeCount>()
                        .lambda().eq(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanNotice.getFollowUpPlanId())
                        .eq(FollowUpPlanNoticeCount::getPatientUserId, patientUser.getId()));
                one.setPush(one.getPush() + 1);
                followUpPlanNoticeCountService.updateById(one);
            }
            if (body.contains("patientOrder:")) {//图文咨询过期时间
                String[] str = body.split(":");
                String patientOrderId = str[1];
                PatientOtherOrder patientOtherOrder = patientOtherOrderService.getById(patientOrderId);
                if (patientOtherOrder.getAcceptStatus().equals("0")) {
                    //代表医生没有接受或者拒绝
                    if (patientOtherOrder.getAmount() != null) {
                        Dept dept = deptService.getById(patientOtherOrder.getDeptId());
                        String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + patientOtherOrder.getOrderNo() + "&transactionId=" + patientOtherOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue();
                        String result = HttpUtil.get(url);
                    }

                }

            }
        }
    }
}
