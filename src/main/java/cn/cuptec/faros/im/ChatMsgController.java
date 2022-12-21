package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.service.ChatMsgService;
import cn.cuptec.faros.service.ChatUserService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/chatMsg")
public class ChatMsgController {
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private UserService userService;

    @ApiOperation(value = "查询历史记录")
    @PostMapping("/queryChatMsgHistory")
    public RestResponse queryChatMsgHistory(@RequestBody SocketFrameTextMessage param) {

        Integer pageNum = param.getPageNum();
        Integer pageSize = param.getPageSize();
        param.setMyUserId(SecurityUtils.getUser().getId());
        if (param.getClearTime() == null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse("2017-09-28 01:07:05", df);
            param.setClearTime(ldt);

        }
        //查询清空历史记录
        ChatUser one = chatUserService.getOne(new QueryWrapper<ChatUser>().lambda().eq(ChatUser::getUid, param.getMyUserId()).eq(ChatUser::getTargetUid, param.getTargetUid()));
        if (one != null) {
            if (one.getClearTime() != null) {
                param.setClearTime(one.getClearTime());
            }
        }
        IPage page = new Page(pageNum, pageSize);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        IPage resultPage;
        if (param.getChatUserId() != null) {
            //查询群聊
            resultPage = chatMsgService.page(page, Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getChatUserId, param.getChatUserId()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .orderByDesc(ChatMsg::getCreateTime));
        } else {
            resultPage = chatMsgService.page(page, Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getToUid, param.getMyUserId()).eq(ChatMsg::getFromUid, param.getTargetUid()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .or(query -> query.eq(ChatMsg::getToUid, param.getTargetUid()).eq(ChatMsg::getFromUid, param.getMyUserId()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .orderByDesc(ChatMsg::getCreateTime)
            );
        }


        List<ChatMsg> records = resultPage.getRecords();
        //查询每个人的头像昵称
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> userIds = records.stream().map(ChatMsg::getFromUid)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (ChatMsg chatMsg : records) {
                chatMsg.setUser(userMap.get(chatMsg.getFromUid()));
            }
            resultPage.setRecords(records);
        }


        return RestResponse.ok(resultPage);
    }
}
