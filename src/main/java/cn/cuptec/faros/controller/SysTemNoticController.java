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
import java.util.*;
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
    public RestResponse page(@RequestParam(value = "type", required = false) Integer type) {
        Page<SysTemNotic> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        queryWrapper.eq("doctor_id", SecurityUtils.getUser().getId());
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> teamIds = list.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            queryWrapper.or();
            queryWrapper.in("team_id", teamIds);

        }
        queryWrapper.eq("check_status", 1);
        if (type != null) {
            if (type.equals(1)) {
                queryWrapper.eq("type", 1);
            }
            if (type.equals(2)) {
                queryWrapper.eq("type", 2);
            }
        }

        queryWrapper.orderByDesc("create_time", "read_status");


        IPage page1 = service.page(page, queryWrapper);
        List<SysTemNotic> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<String> stockUserIds = new ArrayList<>();
            for (SysTemNotic sysTemNotic : records) {

                if (!StringUtils.isEmpty(sysTemNotic.getStockUserId())) {
                    stockUserIds.add(sysTemNotic.getStockUserId());
                }
            }
            Map<String, TbTrainUser> tbTrainUserMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(stockUserIds)) {
                List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getUserId, stockUserIds));
                tbTrainUserMap = tbTrainUsers.stream()
                        .collect(Collectors.toMap(TbTrainUser::getUserId, t -> t));
            }
            for (SysTemNotic sysTemNotic : records) {
                sysTemNotic.setTbTrainUser(tbTrainUserMap.get(sysTemNotic.getStockUserId()));

            }
        }
        return RestResponse.ok(page1);

    }

    //设备端主动发送通知
    @GetMapping("/sendNotic")
    public RestResponse sendNotic(@RequestParam("userId") String userId, @RequestParam("content") String content) {
        service.sendNotic(userId, content);
        return RestResponse.ok();

    }

    @Override
    protected Class<SysTemNotic> getEntityClass() {
        return SysTemNotic.class;
    }
}
