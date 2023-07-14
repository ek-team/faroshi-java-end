package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.mapper.SysTemNoticMapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class SysTemNoticService extends ServiceImpl<SysTemNoticMapper, SysTemNotic> {
    @Resource
    private PlanUserService planUserService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private UserService userService;
    @Resource
    private UniAppPushService uniAppPushService;

    public void sendNotic(String userId, String content) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));

        if (infoByUXtUserId.getPlanCheckStatus()==null || infoByUXtUserId.getPlanCheckStatus().equals(2)) {
            SysTemNotic sysTemNotic = new SysTemNotic();
            if (infoByUXtUserId != null) {
                sysTemNotic.setTeamId(infoByUXtUserId.getDoctorTeamId());
            }
            sysTemNotic.setCreateTime(LocalDateTime.now());
            sysTemNotic.setContent(content);
            sysTemNotic.setTitle(content);
            sysTemNotic.setReadStatus(1);
            sysTemNotic.setType(2);
            sysTemNotic.setPatientUserId(infoByUXtUserId.getXtUserId() + "");
            sysTemNotic.setStockUserId(userId);
            save(sysTemNotic);
        }


        List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                .eq(ChatUser::getTargetUid, userId)
                .eq(ChatUser::getTeamId, infoByUXtUserId.getDoctorTeamId()));
        for (ChatUser chatUser : chatUsers) {

            if (chatUser.getGroupType().equals(1)) {
                //群聊
                String data = chatUser.getUserIds();
                List<String> allUserIds = Arrays.asList(data.split(","));
                sendNotic1(content, infoByUXtUserId.getXtUserId(), chatUser.getPatientId(), allUserIds, chatUser.getId());
            }

        }

    }

    private void sendNotic1(String msg, Integer fromUserId,
                            String patientId, List<String> allUserIds, Integer chatUserId) {

        String name = "";
        if (!StringUtils.isEmpty(patientId)) {
            PatientUser patientUser = patientUserService.getById(patientId);
            name = patientUser.getName();
        }
        for (String userId : allUserIds) {
            String replace = userId.replace("[", "");
            userId = replace.replace("]", "");
            userId = userId.trim();
            if (!userId.equals(fromUserId + "")) {

                Channel targetUserChannel = UserChannelManager.getUserChannel(Integer.parseInt(userId));
                //2.向目标用户发送新消息提醒
                SocketFrameTextMessage targetUserMessage
                        = SocketFrameTextMessage.newMessageTip(fromUserId, "", "", new Date(), ChatProto.SYSTEM_NOTIC, "");
                User user = userService.getById(userId);

                if (StringUtils.isEmpty(name)) {
                    if (StringUtils.isEmpty(user.getPatientName())) {
                        name = user.getNickname();
                    } else {
                        name = user.getPatientName();
                    }

                }
                if (targetUserChannel != null) {
                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                    uniAppPushService.send("法罗适", name + ": " + msg, userId, "","1");

                } else {

                    uniAppPushService.send("法罗适", name + ": " + msg, userId, "","1");

                }
            }

        }

    }
}
