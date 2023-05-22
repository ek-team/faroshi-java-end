package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 就诊人和医生的绑定
 */
@RestController
@RequestMapping("/patientUserBindDoctor")
public class PatientUserBindDoctorController extends AbstractBaseController<PatientUserBindDoctorService, PatientUserBindDoctor> {
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private UserService userService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;

    @PostMapping("/add")
    public RestResponse add(@RequestBody PatientUserBindDoctor patientUserBindDoctor) {
        patientUserBindDoctor.setUserId(SecurityUtils.getUser().getId());
        PatientUser patientUser = patientUserService.getById(patientUserBindDoctor.getPatientUserId());

        service.save(patientUserBindDoctor);
        if (patientUserBindDoctor.getDoctorTeamId() != null) {
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, patientUserBindDoctor.getDoctorTeamId()));

            List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                    .collect(Collectors.toList());
            userIds.add(patientUserBindDoctor.getUserId());
            String chatUserId = "";
            for (Integer userId : userIds) {
                if (StringUtils.isEmpty(chatUserId)) {
                    chatUserId = userId + "";
                } else {
                    chatUserId = chatUserId + "," + userId;
                }
            }
            LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery()
                    .eq(ChatUser::getTeamId, patientUserBindDoctor.getDoctorTeamId())
                    .eq(ChatUser::getPatientId, patientUser.getId());
            wrapper.like(ChatUser::getUserIds, patientUserBindDoctor.getUserId());
            List<ChatUser> list = chatUserService.list(wrapper);
            if (CollectionUtils.isEmpty(list)) {
                ChatUser chatUser1 = new ChatUser();
                chatUser1.setUserIds(chatUserId);
                chatUser1.setGroupType(1);
                chatUser1.setLastChatTime(new Date());
                chatUser1.setTeamId(patientUserBindDoctor.getDoctorTeamId());
                chatUser1.setTargetUid(patientUserBindDoctor.getUserId());
                chatUser1.setPatientId(patientUser.getId());
                chatUser1.setPatientName(patientUser.getName());
                chatUserService.save(chatUser1);
            }

        } else {
            ChatUser fromUserChat = new ChatUser();
            fromUserChat.setUid(patientUserBindDoctor.getDoctorId());
            fromUserChat.setTargetUid(patientUserBindDoctor.getUserId());
            fromUserChat.setLastChatTime(new Date());
            fromUserChat.setLastMsg("");

            ChatUser toUserChat = new ChatUser();
            toUserChat.setUid(patientUserBindDoctor.getUserId());
            toUserChat.setTargetUid(patientUserBindDoctor.getDoctorId());
            toUserChat.setLastChatTime(new Date());
            toUserChat.setLastMsg("");
            List<ChatUser> chatUsers = new ArrayList<>();
            chatUsers.add(fromUserChat);
            chatUsers.add(toUserChat);

            chatUsers.forEach(chatUser -> {
                ChatUser one = chatUserService.getOne(Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, chatUser.getTargetUid()).eq(ChatUser::getUid, chatUser.getUid())
                        .eq(ChatUser::getPatientId, patientUser.getId()));
                if (one != null) {
                    chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                            .eq(ChatUser::getUid, chatUser.getUid())
                            .eq(ChatUser::getPatientId, patientUser.getId())
                            .eq(ChatUser::getTargetUid, chatUser.getTargetUid())
                            .set(ChatUser::getLastChatTime, chatUser.getLastChatTime())
                            .set(ChatUser::getLastMsg, "")
                            .set(ChatUser::getPatientId, patientUser.getId())
                            .set(ChatUser::getPatientName, patientUser.getName()));


                } else {

                    chatUser.setIsClosed(0);
                    chatUser.setLastChatTime(new Date());
                    chatUser.setClearTime(LocalDateTime.now().minusDays(1));
                    chatUser.setLastMsg("");
                    chatUser.setPatientName(patientUser.getName());
                    chatUser.setPatientId(patientUser.getId());
                    chatUserService.save(chatUser);
                }
            });
        }

        return RestResponse.ok();
    }

    @GetMapping("/list")
    public RestResponse list(@RequestParam("patientId") Integer patientId) {
        List<PatientUserBindDoctor> list = service.list(new QueryWrapper<PatientUserBindDoctor>().lambda().eq(PatientUserBindDoctor::getPatientUserId, patientId));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> doctorIds = new ArrayList<>();
            List<Integer> doctorTeamIds = new ArrayList<>();
            for (PatientUserBindDoctor patientUserBindDoctor : list) {
                if (patientUserBindDoctor.getDoctorId() != null) {
                    doctorIds.add(patientUserBindDoctor.getDoctorId());
                }
                if (patientUserBindDoctor.getDoctorTeamId() != null) {
                    doctorTeamIds.add(patientUserBindDoctor.getDoctorTeamId());
                }
            }
            Map<Integer, User> userMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(doctorIds)) {
                List<User> users = (List<User>) userService.listByIds(doctorIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
            }
            Map<Integer, DoctorTeam> doctorTeamMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(doctorTeamIds)) {
                List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(doctorTeamIds);
                doctorTeamMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            }
            for (PatientUserBindDoctor patientUserBindDoctor : list) {
                if (patientUserBindDoctor.getDoctorId() != null) {
                    patientUserBindDoctor.setDoctor(userMap.get(patientUserBindDoctor.getDoctorId()));
                }
                if (patientUserBindDoctor.getDoctorTeamId() != null) {
                    patientUserBindDoctor.setDoctorTeam(doctorTeamMap.get(patientUserBindDoctor.getDoctorTeamId()));
                }
            }
        }
        return RestResponse.ok(list);
    }

    @Override
    protected Class<PatientUserBindDoctor> getEntityClass() {
        return PatientUserBindDoctor.class;
    }
}
