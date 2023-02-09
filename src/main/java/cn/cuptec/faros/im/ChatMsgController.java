package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
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
    @Resource
    private PatientOtherOrderService patientOtherOrderService;
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;
    @Resource
    private FollowUpPlanContentService followUpPlanContentService;

    @ApiOperation(value = "查询历史记录")
    @PostMapping("/queryChatMsgHistory")
    public RestResponse queryChatMsgHistory(@RequestBody SocketFrameTextMessage param) {
        log.info("获取聊天记录开始===============================");
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

        if (!CollectionUtils.isEmpty(records)) {
            //查询每个人的头像昵称
            List<Integer> userIds = records.stream().map(ChatMsg::getFromUid)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));


            List<String> otherOrderIds = new ArrayList<>();//获取图文咨询内容
            List<String> followUpPlanNoticeIds = new ArrayList<>();//随访计划
            Map<Integer, PatientOtherOrder> patientOtherOrderMap = new HashMap<>();
            Map<Integer, FollowUpPlanNotice> followUpPlanNoticeMap = new HashMap<>();
            for (ChatMsg chatMsg : records) {
                chatMsg.setUser(userMap.get(chatMsg.getFromUid()));
                if (chatMsg.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {//图文咨询
                    otherOrderIds.add(chatMsg.getStr1());
                }
                if (chatMsg.getMsgType().equals(ChatProto.FOLLOW_UP_PLAN)) {//随访计划
                    followUpPlanNoticeIds.add(chatMsg.getStr1());
                }
            }
            if (!CollectionUtils.isEmpty(otherOrderIds)) {//图文咨询
                List<PatientOtherOrder> patientOtherOrders = (List<PatientOtherOrder>) patientOtherOrderService.listByIds(otherOrderIds);
                if(!CollectionUtils.isEmpty(patientOtherOrders)){
                    for(PatientOtherOrder patientOtherOrder:patientOtherOrders){
                        String imageUrl = patientOtherOrder.getImageUrl();
                        if (!StringUtils.isEmpty(imageUrl)) {
                            String[] split = imageUrl.split(",");
                            List<String> strings = Arrays.asList(split);
                            patientOtherOrder.setImageUrlList(strings);

                        } else {
                            patientOtherOrder.setImageUrlList(new ArrayList<>());
                        }
                    }
                }
                patientOtherOrderMap = patientOtherOrders.stream()
                        .collect(Collectors.toMap(PatientOtherOrder::getId, t -> t));
            }
            if (!CollectionUtils.isEmpty(followUpPlanNoticeIds)) {//随访计划
                List<FollowUpPlanNotice> followUpPlanNoticeList = (List<FollowUpPlanNotice>) followUpPlanNoticeService.listByIds(followUpPlanNoticeIds);
                if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                    //查询推送内容
                    List<Integer> followUpPlanContentIds = followUpPlanNoticeList.stream().map(FollowUpPlanNotice::getFollowUpPlanContentId)
                            .collect(Collectors.toList());
                    List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);
                    if (!CollectionUtils.isEmpty(followUpPlanContents)) {
                        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = followUpPlanContents.stream()
                                .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));
                        for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
                        }
                    }


                }

                followUpPlanNoticeMap = followUpPlanNoticeList.stream()
                        .collect(Collectors.toMap(FollowUpPlanNotice::getId, t -> t));
            }
            for (ChatMsg chatMsg : records) {
                if (chatMsg.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {//图文咨询
                    PatientOtherOrder patientOtherOrder = patientOtherOrderMap.get(Integer.parseInt(chatMsg.getStr1()));

                    chatMsg.setPatientOtherOrder(patientOtherOrder);
                }

                if (chatMsg.getMsgType().equals(ChatProto.FOLLOW_UP_PLAN)) {//随访计划
                    FollowUpPlanNotice followUpPlanNotice = followUpPlanNoticeMap.get(Integer.parseInt(chatMsg.getStr1()));

                    chatMsg.setFollowUpPlanNotice(followUpPlanNotice);
                }
            }

            resultPage.setRecords(records);
        }

        log.info("获取聊天记录结束===============================");
        return RestResponse.ok(resultPage);
    }
}
