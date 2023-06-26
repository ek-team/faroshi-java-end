package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/systemNotic")
public class SysTemNoticController extends AbstractBaseController<SysTemNoticService, SysTemNotic> {
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

    @GetMapping("/page")
    public RestResponse page() {
        Page<SysTemNotic> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        queryWrapper.eq("doctor_id",SecurityUtils.getUser().getId());
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> teamIds = list.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            queryWrapper.or();
            queryWrapper.in("team_id",teamIds);

        }
        queryWrapper.orderByDesc("create_time","read_status");


        IPage page1 = service.page(page, queryWrapper);
        return RestResponse.ok(page1);

    }

    //设备端主动发送通知
    @GetMapping("/sendNotic")
    public RestResponse sendNotic(@RequestParam("userId")String userId,@RequestParam("content")String content) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));


        SysTemNotic sysTemNotic = new SysTemNotic();
        if(infoByUXtUserId!=null){
            sysTemNotic.setTeamId(infoByUXtUserId.getDoctorTeamId());
        }
        sysTemNotic.setCreateTime(LocalDateTime.now());
        sysTemNotic.setContent(content);
        sysTemNotic.setTitle(content);
        sysTemNotic.setReadStatus(1);
        sysTemNotic.setType(2);
        sysTemNotic.setPatientUserId(infoByUXtUserId.getXtUserId()+"");
        service.save(sysTemNotic);

        List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                .eq(ChatUser::getTargetUid, userId)
                .eq(ChatUser::getTeamId,infoByUXtUserId.getDoctorTeamId()));
        for (ChatUser chatUser : chatUsers) {

            if (chatUser.getGroupType().equals(1)) {
                //群聊
                String data = chatUser.getUserIds();
                List<String> allUserIds = Arrays.asList(data.split(","));
                sendNotic1(content,infoByUXtUserId.getXtUserId(), chatUser.getPatientId(), allUserIds, chatUser.getId());
            }

        }
        return RestResponse.ok();

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

                if (targetUserChannel != null) {
                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                } else {
                    User user = userService.getById(userId);

                    if (StringUtils.isEmpty(name)) {
                        if (StringUtils.isEmpty(user.getPatientName())) {
                            name = user.getNickname();
                        } else {
                            name = user.getPatientName();
                        }

                    }
                    uniAppPushService.send("法罗适", name + ": " + msg, userId, "");

                }
            }

        }

    }
    @Override
    protected Class<SysTemNotic> getEntityClass() {
        return SysTemNotic.class;
    }
}
