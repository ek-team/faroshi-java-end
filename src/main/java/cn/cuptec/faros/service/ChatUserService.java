package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.ChatUserVO;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.mapper.ChatMsgMapper;
import cn.cuptec.faros.mapper.ChatUserMapper;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ChatUserService extends ServiceImpl<ChatUserMapper, ChatUser> {
    @Resource
    private UserService userService;
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;

    public IPage<ChatUserVO> pageChatUsers(SocketFrameTextMessage param) {
        List<ChatUserVO> chatUserVos = new ArrayList<>();


        IPage page = new Page(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery()
                .eq(ChatUser::getUid, param.getMyUserId());
        wrapper.or();
        wrapper.like(ChatUser::getUserIds, param.getMyUserId());
        List<ChatUser> chatUsers = new ArrayList<>();
        IPage result = new Page();
        log.info(param.getSearchName() + "mmmmmmmmmmmmmmmmmmmmmmmmmmmm");
        if (!StringUtils.isEmpty(param.getSearchName())) {//如果是搜索昵称

            LambdaQueryWrapper<User> userWrapper = Wrappers.<User>lambdaQuery()
                    .like(User::getPatientName, param.getSearchName());
            List<User> users = userService.list(userWrapper);

            LambdaQueryWrapper<DoctorTeam> teamWrapper = Wrappers.<DoctorTeam>lambdaQuery()
                    .like(DoctorTeam::getName, param.getSearchName());
            List<DoctorTeam> doctorTeams = doctorTeamService.list(teamWrapper);

            if (CollectionUtils.isEmpty(users) && CollectionUtils.isEmpty(doctorTeams)) {
                return new Page<>();
            }
            //先搜索团队名字
            LambdaQueryWrapper<ChatUser> in = new QueryWrapper<ChatUser>().lambda()
                    .eq(ChatUser::getGroupType, 1)
                    .like(ChatUser::getUserIds, param.getMyUserId());
            if (!CollectionUtils.isEmpty(doctorTeams)) {
                in.eq(ChatUser::getTeamId, doctorTeams.get(0).getId());
                List<ChatUser> list = list(in);
                if (!CollectionUtils.isEmpty(list)) {
                    chatUsers.addAll(list);
                }
            }

            //搜索单聊
            LambdaQueryWrapper<ChatUser> dan = new QueryWrapper<ChatUser>().lambda()
                    .eq(ChatUser::getGroupType, 0)
                    .eq(ChatUser::getUid, param.getMyUserId());
            if (!CollectionUtils.isEmpty(users)) {
                List<Integer> userIds = users.stream().map(User::getId)
                        .collect(Collectors.toList());
                dan.and(wq0 -> wq0.or().in(ChatUser::getTargetUid, userIds));
            }
            List<ChatUser> list1 = list(dan);
            if (!CollectionUtils.isEmpty(list1)) {
                chatUsers.addAll(list1);
            }
            //搜索群聊
            LambdaQueryWrapper<ChatUser> qun = new QueryWrapper<ChatUser>().lambda()
                    .eq(ChatUser::getGroupType, 1)
                    .like(ChatUser::getUserIds, param.getMyUserId());
            if (!CollectionUtils.isEmpty(users)) {
                List<Integer> userIds = users.stream().map(User::getId)
                        .collect(Collectors.toList());
                qun.and(wq0 -> wq0.or().in(ChatUser::getTargetUid, userIds));
            }
            List<ChatUser> list2 = list(qun);
            if (!CollectionUtils.isEmpty(list2)) {
                chatUsers.addAll(list2);
            }
            if (!CollectionUtils.isEmpty(chatUsers)) {
                List<String> Ids = new ArrayList<>();//


                chatUsers = chatUsers.stream().filter(// 过滤去重
                        v -> {
                            boolean flag = !Ids.contains(v.getId() + "");
                            Ids.add(v.getId() + "");
                            return flag;
                        }
                ).collect(Collectors.toList());
            }

            result.setTotal(chatUsers.size());
            result.setRecords(chatUsers);

        } else {
            wrapper.orderByDesc(ChatUser::getLastChatTime);
            result = page(page, wrapper);
            chatUsers = result.getRecords();
        }
        log.info(chatUsers.size() + "ddddddddd");
        if (CollectionUtils.isEmpty(chatUsers)) {
            return result;
        }
        List<Integer> targetUids = new ArrayList<>();
        List<Integer> teamIds = new ArrayList<>();
        List<Integer> patientUserIds = new ArrayList<>();
        for (ChatUser chatUser : chatUsers) {
            if (chatUser.getGroupType() == 0) {
                targetUids.add(chatUser.getTargetUid());
            } else {
                teamIds.add(chatUser.getTeamId());
                patientUserIds.add(chatUser.getTargetUid());
            }

        }
        if (!CollectionUtils.isEmpty(teamIds)) {
            //处理群聊消息
            List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
            //查询所有人员头像
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .in(DoctorTeamPeople::getTeamId, teamIds));
            Map<Integer, User> userMap = new HashMap<>();
            List<Integer> userIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());


                for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                    if (userMap.get(doctorTeamPeople.getUserId()) != null) {
                        doctorTeamPeople.setAvatar(userMap.get(doctorTeamPeople.getUserId()).getAvatar());
                    }

                }
                Map<Integer, List<DoctorTeamPeople>> doctorTeamPeopleMap = doctorTeamPeopleList.stream()
                        .collect(Collectors.groupingBy(DoctorTeamPeople::getTeamId));
                for (DoctorTeam doctorTeam : doctorTeams) {
                    doctorTeam.setDoctorTeamPeopleList(doctorTeamPeopleMap.get(doctorTeam.getId()));
                }
            }
            userIds.addAll(patientUserIds);
            List<User> users = (List<User>) userService.listByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            Map<Integer, DoctorTeam> doctorTeamMap = doctorTeams.stream()
                    .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            for (ChatUser chatUser : chatUsers) {
                if (chatUser.getGroupType().equals(1)) {
                    ChatUserVO chatUserVO = new ChatUserVO();
                    DoctorTeam doctorTeam = doctorTeamMap.get(chatUser.getTeamId());
                    List<DoctorTeamPeople> doctorTeamPeopleList1 = doctorTeam.getDoctorTeamPeopleList();
                    chatUserVO.setDoctorTeamPeopleList(doctorTeamPeopleList1);
                    chatUserVO.setNickname(doctorTeam.getName());
                    User user = userMap.get(chatUser.getTargetUid());
                    if (user != null) {
                        String patientName = user.getPatientName();
                        if (!StringUtils.isEmpty(chatUser.getChatDesc())) {
                            patientName = patientName + "[" + chatUser.getChatDesc() + "]";
                        }
                        chatUserVO.setPatientName(patientName);
                        chatUserVO.setPatientAvatar(user.getAvatar());
                    }
                    chatUserVO.setGroupType(1);

                    chatUserVO.setChatUserId(chatUser.getId());
                    chatUserVO.setServiceEndTime(chatUser.getServiceEndTime());
                    chatUserVO.setServiceStartTime(chatUser.getServiceStartTime());
                    chatUserVO.setTargetUid(chatUser.getTargetUid());
                    // 最后聊天时间和内容

                    chatUserVO.setLastChatTime(chatUser.getLastChatTime());
                    chatUserVO.setLastMsg(chatUser.getLastMsg());
                    chatUserVos.add(chatUserVO);

                    // 是否有新消息
                    int count =
                            chatMsgService.count(
                                    Wrappers.<ChatMsg>lambdaQuery()
                                            .eq(ChatMsg::getChatUserId, chatUser.getId())
                                            .notLike(ChatMsg::getReadUserIds, param.getMyUserId()));
                    chatUserVO.setHasNewMsg(count);
                }
            }
        }

        if (targetUids.size() > 0) {

            List<User> users = (List<User>) userService.listByIds(targetUids);
            List<ChatUser> chatUsersList = new ArrayList<>();
            for (ChatUser chatUser : chatUsers) {
                if (chatUser.getGroupType() == 0) {
                    chatUsersList.add(chatUser);
                }
            }
            // 根据获取的用户信息构造ChatUserVO
            Map<Integer, ChatUser> chatUserMap = chatUsersList.stream()
                    .collect(Collectors.toMap(ChatUser::getTargetUid, t -> t));

            users.forEach(
                    tenantUser -> {
                        ChatUserVO chatUserVO = new ChatUserVO();
                        chatUserVO.setTargetUid(tenantUser.getId());
                        String patientName = tenantUser.getPatientName();
                        if (!StringUtils.isEmpty(chatUserMap.get(tenantUser.getId()).getChatDesc())) {
                            patientName = patientName + "[" + chatUserMap.get(tenantUser.getId()).getChatDesc() + "]";
                        }
                        chatUserVO.setPatientName(patientName);

                        chatUserVO.setPatientName(patientName);
                        chatUserVO.setAvatar(tenantUser.getAvatar());
                        chatUserVO.setPatientAvatar(tenantUser.getAvatar());
                        chatUserVO.setNickname(tenantUser.getNickname());
                        chatUserVO.setServiceEndTime(chatUserMap.get(tenantUser.getId()).getServiceEndTime());
                        chatUserVO.setServiceStartTime(chatUserMap.get(tenantUser.getId()).getServiceStartTime());
                        chatUserVO.setRemark(chatUserMap.get(tenantUser.getId()).getRemark());
                        chatUserVO.setClearTime(chatUserMap.get(tenantUser.getId()).getClearTime());
                        chatUserVO.setChatUserId(chatUserMap.get(tenantUser.getId()).getId());
                        // 最后聊天时间和内容
                        ChatUser user =
                                chatUsersList.stream()
                                        .filter(chatUser -> chatUser.getTargetUid().equals(chatUserVO.getTargetUid()))
                                        .findFirst()
                                        .get();
                        chatUserVO.setIsClosed(user.getIsClosed());
                        chatUserVO.setLastChatTime(user.getLastChatTime());
                        chatUserVO.setLastMsg(user.getLastMsg());
                        chatUserVos.add(chatUserVO);

                        // 是否有新消息
                        int count =
                                chatMsgService.count(
                                        Wrappers.<ChatMsg>lambdaQuery()
                                                .eq(ChatMsg::getFromUid, tenantUser.getId())
                                                .eq(ChatMsg::getToUid, param.getMyUserId())
                                                .eq(ChatMsg::getReadStatus, 0));

                        chatUserVO.setHasNewMsg(count);
                    }
            );

        }
        Collections.sort(chatUserVos);
        result.setRecords(chatUserVos);
        return result;
    }

    /**
     * 添加关系好友
     */
    public void saveOrUpdateChatUser(Integer fromUserId, Integer targetUid, String msg) {
        ChatUser fromUserChat = new ChatUser();
        fromUserChat.setUid(fromUserId);
        fromUserChat.setTargetUid(targetUid);
        fromUserChat.setLastChatTime(new Date());
        fromUserChat.setLastMsg(msg);

        ChatUser toUserChat = new ChatUser();
        toUserChat.setUid(targetUid);
        toUserChat.setTargetUid(fromUserId);
        toUserChat.setLastChatTime(new Date());
        toUserChat.setLastMsg(msg);
        List<ChatUser> chatUsers = new ArrayList<>();
        chatUsers.add(fromUserChat);
        chatUsers.add(toUserChat);

        chatUsers.forEach(chatUser -> {
            ChatUser one = baseMapper.selectOne(Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, chatUser.getTargetUid()).eq(ChatUser::getUid, chatUser.getUid()));
            if (one != null) {
                update(Wrappers.<ChatUser>lambdaUpdate()
                        .eq(ChatUser::getUid, chatUser.getUid())
                        .eq(ChatUser::getTargetUid, chatUser.getTargetUid())
                        .set(ChatUser::getLastChatTime, chatUser.getLastChatTime())
                        .set(ChatUser::getLastMsg, msg));


            } else {

                chatUser.setIsClosed(0);
                chatUser.setLastChatTime(new Date());
                chatUser.setClearTime(LocalDateTime.now().minusDays(1));
                chatUser.setLastMsg(msg);
                save(chatUser);
            }
        });
    }


    /**
     * 添加群聊
     */
    public ChatUser saveGroupChatUser(List<Integer> userIds, Integer doctorTeamId, Integer patientUserId) {
        String chatUserId = "";
        for (Integer userId : userIds) {
            if (StringUtils.isEmpty(chatUserId)) {
                chatUserId = userId + "";
            } else {
                chatUserId = chatUserId + "," + userId;
            }
        }
        LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery()
                .eq(ChatUser::getTeamId, doctorTeamId);
        wrapper.like(ChatUser::getUserIds, patientUserId);
        List<ChatUser> list = list(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }
        ChatUser chatUser = new ChatUser();
        chatUser.setUserIds(chatUserId);
        chatUser.setGroupType(1);
        chatUser.setLastChatTime(new Date());
        chatUser.setTeamId(doctorTeamId);
        chatUser.setTargetUid(patientUserId);
        save(chatUser);
        return chatUser;
    }
}
