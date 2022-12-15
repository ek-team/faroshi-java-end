package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.im.bean.ChatUserVO;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.mapper.ChatMsgMapper;
import cn.cuptec.faros.mapper.ChatUserMapper;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ChatUserService extends ServiceImpl<ChatUserMapper, ChatUser> {
    @Resource
    private UserService userService;
    @Resource
    private ChatMsgService chatMsgService;
    public IPage<ChatUserVO> pageChatUsers(SocketFrameTextMessage param) {
        List<ChatUserVO> chatUserVos = new ArrayList<>();


        IPage page = new Page(param.getPageNum(), param.getPageSize());


        IPage result = page(page, Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getUid, param.getMyUserId()).orderByDesc(ChatUser::getLastChatTime));
        List<ChatUser> chatUsers = result.getRecords();
        if (CollectionUtils.isEmpty(chatUsers)) {
            return result;
        }
        List<Integer> targetUids = chatUsers.stream().map(ChatUser::getTargetUid).collect(Collectors.toList());
        //1
        if (targetUids.size() > 0) {

            List<User> users = (List<User>) userService.listByIds(targetUids);

            // 根据获取的用户信息构造ChatUserVO
            Map<Integer, ChatUser> chatUserMap = chatUsers.stream()
                    .collect(Collectors.toMap(ChatUser::getTargetUid, t -> t));
            users.forEach(
                    tenantUser -> {
                        ChatUserVO chatUserVO = new ChatUserVO();
                        chatUserVO.setTargetUid(tenantUser.getId());
                        chatUserVO.setAvatar(tenantUser.getAvatar());
                        chatUserVO.setNickname(tenantUser.getNickname());

                        chatUserVO.setRemark(chatUserMap.get(tenantUser.getId()).getRemark());
                        chatUserVO.setClearTime(chatUserMap.get(tenantUser.getId()).getClearTime());

                        // 最后聊天时间和内容
                        ChatUser user =
                                chatUsers.stream()
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

            Collections.sort(chatUserVos);
            result.setRecords(chatUserVos);
        }
        return result;
    }
}
