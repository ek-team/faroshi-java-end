package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.ChatUserVO;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chatUser")
public class ChatUserController {
    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    public RedisTemplate redisTemplate;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;

    /**
     * 查询会话信息 聊天是否有效
     */
    @ApiOperation(value = "查询会话信息 聊天是否有效")
    @GetMapping("/queryChatTime")
    public RestResponse queryChatTime(@RequestParam("chatUserId") int chatUserId) {

        ChatUser chatUser = chatUserService.getById(chatUserId);
        if (chatUser.getServiceEndTime() == null) {

            chatUser.setStatus(2);
            return RestResponse.ok(chatUser);
        }
        if (chatUser.getServiceEndTime().isAfter(LocalDateTime.now())) {
            chatUser.setStatus(1);
        } else {
            chatUser.setStatus(2);
        }
        return RestResponse.ok(chatUser);
    }

    /**
     * 医生主动结束会话
     */
    @ApiOperation(value = "查询会话信息 聊天是否有效")
    @GetMapping("/endChat")
    public RestResponse endChat(@RequestParam("chatUserId") int chatUserId) {

        ChatUser chatUser = chatUserService.getById(chatUserId);
        chatUser.setServiceEndTime(LocalDateTime.now().minusDays(2));
        return RestResponse.ok(chatUser);
    }

    /**
     * 用户进入聊天 开始计时 使用服务
     *
     * @return
     */
    @ApiOperation(value = "使用服务")
    @GetMapping("/useService")
    public RestResponse useService(@RequestParam("userServiceId") int userServiceId) {
        UserServicePackageInfo userServicePackageInfo = userServicePackageInfoService.getById(userServiceId);
        ChatUser chatUser = chatUserService.getById(userServicePackageInfo.getChatUserId());
        if (chatUser.getServiceStartTime() != null && chatUser.getServiceEndTime().isAfter(LocalDateTime.now())) {
            return RestResponse.ok();
        }
        if (userServicePackageInfo.getTotalCount().equals(userServicePackageInfo.getUseCount())) {
            return RestResponse.ok();
        }
        userServicePackageInfo.setUseCount(userServicePackageInfo.getUseCount() + 1);
        userServicePackageInfoService.updateById(userServicePackageInfo);
        //修改聊天框使用时间
        chatUser.setId(userServicePackageInfo.getChatUserId());
        chatUser.setServiceStartTime(LocalDateTime.now());
        chatUser.setServiceEndTime(LocalDateTime.now().plusHours(24));
        chatUserService.updateById(chatUser);
        return RestResponse.ok("1");
    }


    @ApiOperation(value = "分页查询聊天列表")
    @PostMapping("/pageChatUsers")
    public RestResponse pageChatUsers(@RequestBody SocketFrameTextMessage param) {
        param.setMyUserId(SecurityUtils.getUser().getId());
        //获取聊天用户列表
        IPage<ChatUserVO> iPage = chatUserService.pageChatUsers(param);
        List<ChatUserVO> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            records = new ArrayList<>();
            iPage.setRecords(records);
            return RestResponse.ok(iPage);
        }

        iPage.setRecords(records);
        //返回请求结果
        return RestResponse.ok(iPage);
    }

    /**
     * 获取用户未读数量
     */
    @GetMapping("/getUnReadCount")
    public RestResponse getUnReadCount() {

        int count =
                chatMsgService.count(
                        Wrappers.<ChatMsg>lambdaQuery()
                                .eq(ChatMsg::getToUid, SecurityUtils.getUser().getId())
                                .eq(ChatMsg::getReadStatus, 0));
        // 是否有新消息
        int count1 =
                chatMsgService.count(
                        Wrappers.<ChatMsg>lambdaQuery()
                                .notLike(ChatMsg::getReadUserIds, SecurityUtils.getUser().getId()));
        return RestResponse.ok(count + count1);
    }

    /**
     * 设置全部已读
     */
    @GetMapping("/readAll")
    public RestResponse readAll() {
        //设置单聊
        chatMsgService.update(Wrappers.<ChatMsg>lambdaUpdate()
                .set(ChatMsg::getReadStatus, 1)
                .eq(ChatMsg::getToUid, SecurityUtils.getUser().getId())
        );
        //设置群聊
        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            return RestResponse.ok();


        }
        List<Integer> teamIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getTeamId)
                .collect(Collectors.toList());
        List<ChatUser> chatUserList = chatUserService.list(new QueryWrapper<ChatUser>().lambda().in(ChatUser::getTeamId, teamIds));
        if (CollectionUtils.isEmpty(chatUserList)) {
            return RestResponse.ok();

        }
        List<Integer> chatIds = chatUserList.stream().map(ChatUser::getId)
                .collect(Collectors.toList());
        List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().lambda().in(ChatMsg::getChatUserId, chatIds));
        if (!CollectionUtils.isEmpty(chatMsgs)) {
            List<ChatMsg> updateChatMsg = new ArrayList<>();
            for (ChatMsg chatMsg : chatMsgs) {
                String readUserIds = chatMsg.getReadUserIds();
                if (StringUtils.isEmpty(readUserIds)) {
                    readUserIds = SecurityUtils.getUser().getId() + "";
                    chatMsg.setReadUserIds(readUserIds);
                    updateChatMsg.add(chatMsg);
                } else {
                    if (readUserIds.indexOf(SecurityUtils.getUser().getId() + "") < 0) {
                        readUserIds = readUserIds + "," + SecurityUtils.getUser().getId();
                        chatMsg.setReadUserIds(readUserIds);
                        updateChatMsg.add(chatMsg);
                    }
                }

            }
            if (!org.springframework.util.CollectionUtils.isEmpty(updateChatMsg)) {
                chatMsgService.updateBatchById(updateChatMsg);
            }
        }
        return RestResponse.ok();
    }
}
