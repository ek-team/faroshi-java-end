package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.im.bean.ChatUserVO;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.service.ChatMsgService;
import cn.cuptec.faros.service.ChatUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chatUser")
public class ChatUserController {
    @Resource
    private ChatUserService chatUserService;

    @Resource
    private ChatMsgService chatMsgService;

    @ApiOperation(value = "分页查询聊天列表")
    @PostMapping("/pageChatUsers")
    public RestResponse pageChatUsers(@RequestBody SocketFrameTextMessage param) {

        //获取聊天用户列表
        IPage<ChatUserVO> iPage = chatUserService.pageChatUsers(param);
        List<ChatUserVO> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            records = new ArrayList<>();
            iPage.setRecords(records);
            return RestResponse.ok(iPage);
        }
        //设置聊天用户在线状态
        records.forEach(chatUserVO -> {
            UserChannelManager.userChannelMap.forEach((ch, socketUser) -> {
                if (socketUser.isAuth() && socketUser.getUserInfo().getId().equals(chatUserVO.getTargetUid())) {
                    chatUserVO.setOnline(1);

                }
            });
        });
        iPage.setRecords(records);
        //返回请求结果
        return RestResponse.ok(iPage);
    }

}
