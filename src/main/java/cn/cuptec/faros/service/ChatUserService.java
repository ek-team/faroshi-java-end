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
                    chatUserVO.setPatientId(chatUser.getPatientId());
                    DoctorTeam doctorTeam = doctorTeamMap.get(chatUser.getTeamId());
                    if(doctorTeam!=null){
                        List<DoctorTeamPeople> doctorTeamPeopleList1 = doctorTeam.getDoctorTeamPeopleList();
                        chatUserVO.setDoctorTeamPeopleList(doctorTeamPeopleList1);
                        chatUserVO.setNickname(doctorTeam.getName());
                    }


                    User user = userMap.get(chatUser.getTargetUid());
                    if (user != null) {
                        String patientName = user.getPatientName();
                        if (!StringUtils.isEmpty(chatUser.getChatDesc())) {
                            patientName = patientName + "[" + chatUser.getChatDesc() + "]";
                        }
                        chatUserVO.setPatientName(patientName);
                        chatUserVO.setPatientAvatar(user.getAvatar());
                    }
                    if (!StringUtils.isEmpty(chatUser.getPatientName())) {
                        chatUserVO.setPatientName(chatUser.getPatientName());
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
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));

            chatUsers.forEach(
                    chatUser -> {
                        if (chatUser.getTeamId() == null) {
                            ChatUserVO chatUserVO = new ChatUserVO();
                            chatUserVO.setTargetUid(chatUser.getTargetUid());
                            chatUserVO.setPatientId(chatUser.getPatientId());
                            User tenantUser = userMap.get(chatUser.getTargetUid());
                            String patientName="";
                            if(tenantUser!=null){
                                 patientName = tenantUser.getPatientName();
                                chatUserVO.setAvatar(tenantUser.getAvatar());
                                chatUserVO.setPatientAvatar(tenantUser.getAvatar());
                                chatUserVO.setNickname(tenantUser.getNickname());
                            }

                            if (!StringUtils.isEmpty(chatUser.getPatientName())) {
                                patientName = chatUser.getPatientName();
                            }

                            if (!StringUtils.isEmpty(chatUser.getChatDesc())) {
                                patientName = patientName + "[" + chatUser.getChatDesc() + "]";
                            }
                            chatUserVO.setPatientName(patientName);

                            chatUserVO.setPatientName(patientName);

                            chatUserVO.setServiceEndTime(chatUser.getServiceEndTime());
                            chatUserVO.setServiceStartTime(chatUser.getServiceStartTime());
                            chatUserVO.setRemark(chatUser.getRemark());
                            chatUserVO.setClearTime(chatUser.getClearTime());
                            chatUserVO.setChatUserId(chatUser.getId());
                            // 最后聊天时间和内容
                            chatUserVO.setIsClosed(chatUser.getIsClosed());
                            chatUserVO.setLastChatTime(chatUser.getLastChatTime());
                            chatUserVO.setLastMsg(chatUser.getLastMsg());
                            chatUserVos.add(chatUserVO);

                            // 是否有新消息
                            int count;
                            if (!StringUtils.isEmpty(chatUser.getPatientId())) {
                                LambdaQueryWrapper<ChatMsg> eq = Wrappers.<ChatMsg>lambdaQuery()
                                        .eq(ChatMsg::getFromUid, chatUser.getTargetUid())
                                        .eq(ChatMsg::getToUid, param.getMyUserId())
                                        .eq(ChatMsg::getPatientId, chatUser.getPatientId())
                                        .eq(ChatMsg::getReadStatus, 0);
                                count =
                                        chatMsgService.count(eq
                                        );

                            } else {
                                LambdaQueryWrapper<ChatMsg> eq = Wrappers.<ChatMsg>lambdaQuery()
                                        .eq(ChatMsg::getFromUid, chatUser.getTargetUid())
                                        .eq(ChatMsg::getToUid, param.getMyUserId())
                                        .eq(ChatMsg::getReadStatus, 0);
                                count =
                                        chatMsgService.count(eq
                                        );
                            }

                            chatUserVO.setHasNewMsg(count);
                        }

                    }
            );

        }
        Collections.sort(chatUserVos);
        result.setRecords(chatUserVos);
        return result;
    }

    public int pageWaitChatUsersCount(SocketFrameTextMessage param) {

        int count = count(new QueryWrapper<ChatUser>().lambda()
                .eq(ChatUser::getReceiverId, param.getMyUserId())
                .eq(ChatUser::getReceiverStatus, 0));

        LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery();

        wrapper.and(wq0 -> wq0.eq(ChatUser::getUid, param.getMyUserId())
                .or().like(ChatUser::getUserIds, param.getMyUserId()));
        wrapper.eq(ChatUser::getPatientOtherOrderStatus, 0);
        int count1 = count(wrapper);

        return count + count1;
    }

    public List<ChatUserVO> pageWaitChatUsers(SocketFrameTextMessage param) {
        List<ChatUserVO> chatUserVos = new ArrayList<>();

        List<ChatUser> chatUsers = list(new QueryWrapper<ChatUser>().lambda()
                .eq(ChatUser::getReceiverId, param.getMyUserId())
                .eq(ChatUser::getReceiverStatus, 0));
        if (CollectionUtils.isEmpty(chatUsers)) {
            chatUsers = new ArrayList<>();
        }
        LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery();

        wrapper.and(wq0 -> wq0.eq(ChatUser::getUid, param.getMyUserId())
                .or().like(ChatUser::getUserIds, param.getMyUserId()));
        wrapper.eq(ChatUser::getPatientOtherOrderStatus, 0);
        List<ChatUser> list = list(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            chatUsers.addAll(list);
        }

        if (CollectionUtils.isEmpty(chatUsers)) {
            return chatUserVos;
        }
        List<String> Ids = new ArrayList<>();//

        chatUsers = chatUsers.stream().filter(// 过滤去重
                v -> {
                    boolean flag = !Ids.contains(v.getId() + "");
                    Ids.add(v.getId() + "");
                    return flag;
                }
        ).collect(Collectors.toList());
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
                    chatUserVO.setPatientId(chatUser.getPatientId());
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
                    if (!StringUtils.isEmpty(chatUser.getPatientName())) {
                        chatUserVO.setPatientName(chatUser.getPatientName());
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
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));

            chatUsers.forEach(
                    chatUser -> {
                        if (chatUser.getTeamId() == null) {
                            ChatUserVO chatUserVO = new ChatUserVO();
                            chatUserVO.setTargetUid(chatUser.getTargetUid());
                            chatUserVO.setPatientId(chatUser.getPatientId());
                            User tenantUser = userMap.get(chatUser.getTargetUid());
                            String patientName="";
                            if(tenantUser!=null){
                                 patientName = tenantUser.getPatientName();
                                chatUserVO.setAvatar(tenantUser.getAvatar());
                                chatUserVO.setPatientAvatar(tenantUser.getAvatar());
                                chatUserVO.setNickname(tenantUser.getNickname());

                            }
                            if (!StringUtils.isEmpty(chatUser.getPatientName())) {
                                patientName = chatUser.getPatientName();
                            }

                            if (!StringUtils.isEmpty(chatUser.getChatDesc())) {
                                patientName = patientName + "[" + chatUser.getChatDesc() + "]";
                            }
                            chatUserVO.setPatientName(patientName);

                            chatUserVO.setPatientName(patientName);
                              chatUserVO.setServiceEndTime(chatUser.getServiceEndTime());
                            chatUserVO.setServiceStartTime(chatUser.getServiceStartTime());
                            chatUserVO.setRemark(chatUser.getRemark());
                            chatUserVO.setClearTime(chatUser.getClearTime());
                            chatUserVO.setChatUserId(chatUser.getId());
                            // 最后聊天时间和内容
                            chatUserVO.setIsClosed(chatUser.getIsClosed());
                            chatUserVO.setLastChatTime(chatUser.getLastChatTime());
                            chatUserVO.setLastMsg(chatUser.getLastMsg());
                            chatUserVos.add(chatUserVO);

                            // 是否有新消息
                            int count;
                            if (!StringUtils.isEmpty(chatUser.getPatientId())) {
                                LambdaQueryWrapper<ChatMsg> eq = Wrappers.<ChatMsg>lambdaQuery()
                                        .eq(ChatMsg::getFromUid, chatUser.getTargetUid())
                                        .eq(ChatMsg::getToUid, param.getMyUserId())
                                        .eq(ChatMsg::getPatientId, chatUser.getPatientId())
                                        .eq(ChatMsg::getReadStatus, 0);
                                count =
                                        chatMsgService.count(eq
                                        );

                            } else {
                                LambdaQueryWrapper<ChatMsg> eq = Wrappers.<ChatMsg>lambdaQuery()
                                        .eq(ChatMsg::getFromUid, chatUser.getTargetUid())
                                        .eq(ChatMsg::getToUid, param.getMyUserId())
                                        .eq(ChatMsg::getReadStatus, 0);
                                count =
                                        chatMsgService.count(eq
                                        );
                            }

                            chatUserVO.setHasNewMsg(count);
                        }

                    }
            );

        }
        Collections.sort(chatUserVos);
        return chatUserVos;
    }

    /**
     * 添加关系好友
     */
    public ChatUser saveOrUpdateChatUser(Integer fromUserId, Integer targetUid, String msg) {
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
        return fromUserChat;
    }


    /**
     * 添加群聊
     */
    public ChatUser saveGroupChatUser(List<Integer> userIds, Integer doctorTeamId, Integer patientUserId,Integer patientId,String patientName) {
        String chatUserId = "";
        for (Integer userId : userIds) {
            if (StringUtils.isEmpty(chatUserId)) {
                chatUserId = userId + "";
            } else {
                chatUserId = chatUserId + "," + userId;
            }
        }
        LambdaQueryWrapper<ChatUser> wrapper = Wrappers.<ChatUser>lambdaQuery()
                .eq(ChatUser::getTeamId, doctorTeamId)
                .eq(ChatUser::getPatientId, patientId);
        wrapper.like(ChatUser::getUserIds, patientUserId);
        List<ChatUser> list = list(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            ChatUser chatUser = list.get(0);
            //处理新的图文咨询 后加入团队的医生也可以看到
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, doctorTeamId));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<Integer> doctorIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                String userIdStr = chatUser.getUserIds();
                for (Integer doctorId : doctorIds) {
                    if (userIdStr.indexOf(doctorId + "") < 0) {
                        userIdStr = userIds + "," + doctorId;
                    }
                }
                chatUser.setUserIds(userIdStr);
                chatUser.setPatientName(patientName);
                chatUser.setPatientId(patientId+"");
                updateById(chatUser);
                return chatUser;
            }
        }
        ChatUser chatUser1 = new ChatUser();
        chatUser1.setPatientId(patientId+"");
        chatUser1.setPatientName(patientName);
        chatUser1.setUserIds(chatUserId);
        chatUser1.setGroupType(1);
        chatUser1.setLastChatTime(new Date());
        chatUser1.setTeamId(doctorTeamId);
        chatUser1.setTargetUid(patientUserId);
        save(chatUser1);
        return chatUser1;
    }

}
