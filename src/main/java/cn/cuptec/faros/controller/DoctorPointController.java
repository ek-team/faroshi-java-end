package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.DoctorPointCountResult;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.pay.PayResultData;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.util.collection.CharCollections;
import jdk.nashorn.internal.ir.LiteralNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 医生积分管理
 */
@Slf4j
@RestController
@RequestMapping("/doctorPoint")
public class DoctorPointController extends AbstractBaseController<DoctorPointService, DoctorPoint> {
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    @Resource
    private DoctorUserActionService doctorUserActionService;//医生设置的服务价格
    @Resource
    private UserService userService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private WxPayFarosService wxPayFarosService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private UpcomingService upcomingService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 分页查询医生积分
     *
     * @return
     */
    @GetMapping("/getDoctorPoint")
    public RestResponse getDoctorPoint() {

        //查询当前医生所在的团队
        Page<DoctorPoint> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("doctor_user_id", SecurityUtils.getUser().getId());
        List<DoctorTeam> doctorTeams = new ArrayList<>();
        List<DoctorTeamPeople> doctorTeamPeoples = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(doctorTeamPeoples)) {

            List<Integer> teamIds = doctorTeamPeoples.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
            queryWrapper.or();
            queryWrapper.in("doctor_team_id", teamIds);
        }


        queryWrapper.orderByDesc("create_time", "withdraw_status");
        IPage<DoctorPoint> doctorPointIPage = service.page(page, queryWrapper);
        List<DoctorPoint> records = doctorPointIPage.getRecords();
        if (!CollectionUtils.isEmpty(records) && !CollectionUtils.isEmpty(doctorTeams)) {
            Map<Integer, DoctorTeam> doctorMap = doctorTeams.stream()
                    .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            for (DoctorPoint doctorPoint : records) {
                DoctorTeam doctorTeam = doctorMap.get(doctorPoint.getDoctorTeamId());
                if (doctorTeam != null) {
                    doctorPoint.setDoctorTeamName(doctorTeam.getName());
                }
            }
        }
        return RestResponse.ok(doctorPointIPage);
    }

    /**
     * 查询医生总积分
     *
     * @return
     */
    @GetMapping("/getDoctorTotalPoint")
    public RestResponse getDoctorTotalPoint() {
        List<DoctorTeamPeople> doctorTeamPeoples = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        LambdaQueryWrapper<DoctorPoint> eq = new QueryWrapper<DoctorPoint>().lambda()
                .eq(DoctorPoint::getDoctorUserId, SecurityUtils.getUser().getId());

        if (!CollectionUtils.isEmpty(doctorTeamPeoples)) {
            List<Integer> teamIds = doctorTeamPeoples.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            eq.or();
            eq.in(DoctorPoint::getDoctorTeamId, teamIds);
        }


        DoctorPointCountResult result = new DoctorPointCountResult();
        List<DoctorPoint> list = service.list(eq);
        Double totalPoint = 0.0;
        Double pendingWithdraw = 0.0;
        Double withdraw = 0.0;
        if (!CollectionUtils.isEmpty(list)) {
            for (DoctorPoint doctorPoint : list) {
                totalPoint = doctorPoint.getPoint() + totalPoint;
                if (doctorPoint.getWithdrawStatus().equals(1)) {
                    pendingWithdraw = pendingWithdraw + doctorPoint.getPoint();
                } else {
                    withdraw = withdraw + doctorPoint.getPoint();
                }
            }
        }
        result.setTotalPoint(totalPoint);
        result.setWithdraw(withdraw);
        result.setPendingWithdraw(pendingWithdraw);
        return RestResponse.ok(result);
    }

    /**
     * 判断医生和团队是否开通图文咨询申请
     */
    @PostMapping("/checkOpen")
    public RestResponse checkOpen(@RequestBody PatientOtherOrder patientOtherOrder) {

        if (patientOtherOrder.getDoctorId() != null) {
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            if (one != null) {
                return RestResponse.ok(one);
            }
        } else {
            //查询团队图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (one != null) {
                return RestResponse.ok(one);
            }
        }
        return RestResponse.ok();
    }

    /**
     * 患者图文咨询申请
     *
     * @return
     */
    @PostMapping("/addPatientOtherOrder")
    public RestResponse addPatientOtherOrder(@RequestBody PatientOtherOrder patientOtherOrder) {
        patientOtherOrder.setUserId(SecurityUtils.getUser().getId());
        patientOtherOrder.setCreateTime(LocalDateTime.now());
        patientOtherOrder.setStatus(1);
        patientOtherOrder.setType(1);
        patientOtherOrder.setAcceptStatus("0");
        patientOtherOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        if (patientOtherOrder.getDoctorId() != null) {
            User doctorUser = userService.getById(patientOtherOrder.getDoctorId());
            patientOtherOrder.setDeptId(doctorUser.getDeptId());
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
            //查询团队图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        }
        patientOtherOrderService.save(patientOtherOrder);
        //添加待办事项
        Upcoming upcoming = new Upcoming();
        upcoming.setTeamId(patientOtherOrder.getDoctorTeamId());
        upcoming.setChatUserId(patientOtherOrder.getChatUserId());
        upcoming.setContent("图文咨询申请");
        upcoming.setTitle("图文咨询申请");
        upcoming.setUserId(SecurityUtils.getUser().getId());
        Integer doctorId = patientOtherOrder.getDoctorId();


        upcoming.setCreateTime(LocalDateTime.now());
        upcoming.setType("2");
        List<Upcoming> upcomingList = new ArrayList<>();
        if (doctorId != null) {
            upcoming.setDoctorId(doctorId);
            upcomingList.add(upcoming);
        } else {

            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                    Upcoming upcoming1 = new Upcoming();
                    upcoming1.setTeamId(patientOtherOrder.getDoctorTeamId());
                    upcoming1.setChatUserId(patientOtherOrder.getChatUserId());
                    upcoming1.setContent("图文咨询申请");
                    upcoming1.setTitle("图文咨询申请");
                    upcoming1.setUserId(SecurityUtils.getUser().getId());
                    upcoming1.setCreateTime(LocalDateTime.now());
                    upcoming1.setType("2");
                    upcoming1.setDoctorId(doctorTeamPeople.getUserId());
                    upcomingList.add(upcoming1);
                }


            }
        }
        upcomingService.saveBatch(upcomingList);
        RestResponse restResponse = wxPayFarosService.unifiedOtherOrder(patientOtherOrder.getOrderNo());

        //添加到redis 超过24小时如果医生没有接受则退款 返回使用次数
        String keyRedis = String.valueOf(StrUtil.format("{}{}", "patientOrder:", patientOtherOrder.getId()));
        redisTemplate.opsForValue().set(keyRedis, patientOtherOrder.getId(), 24, TimeUnit.HOURS);//设置过期时间
        //修改聊天状态为咨询
        Integer chatUserId = patientOtherOrder.getChatUserId();
        updateChatDesc(chatUserId);

        return restResponse;
    }

    private void updateChatDesc(Integer chatUserId) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ChatUser chatUser = chatUserService.getById(chatUserId);
                if (chatUser.getGroupType().equals(0)) {
                    ChatUser fromUserChat = new ChatUser();
                    fromUserChat.setUid(chatUser.getUid());
                    fromUserChat.setTargetUid(chatUser.getTargetUid());


                    ChatUser toUserChat = new ChatUser();
                    toUserChat.setUid(chatUser.getTargetUid());
                    toUserChat.setTargetUid(chatUser.getUid());

                    List<ChatUser> chatUsers = new ArrayList<>();
                    chatUsers.add(fromUserChat);
                    chatUsers.add(toUserChat);

                    chatUsers.forEach(c -> {
                        ChatUser one = chatUserService.getOne(Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, c.getTargetUid()).eq(ChatUser::getUid, c.getUid()));
                        if (one != null) {

                            chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                                    .eq(ChatUser::getUid, c.getUid())
                                    .eq(ChatUser::getTargetUid, c.getTargetUid())
                                    .set(ChatUser::getChatDesc, "咨询")

                            );
                        }
                    });
                } else {
                    chatUser.setChatDesc("咨询");
                    chatUserService.updateById(chatUser);
                }

            }
        });
    }

    @PostMapping("/addPatientOtherOrder1")
    public RestResponse addPatientOtherOrder1(@RequestBody PatientOtherOrder patientOtherOrder) {
        patientOtherOrder.setUserId(SecurityUtils.getUser().getId());
        patientOtherOrder.setCreateTime(LocalDateTime.now());
        patientOtherOrder.setStatus(1);
        patientOtherOrder.setType(1);
        patientOtherOrder.setAcceptStatus("0");
        patientOtherOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        if (patientOtherOrder.getDoctorId() != null) {
            User doctorUser = userService.getById(patientOtherOrder.getDoctorId());
            patientOtherOrder.setDeptId(doctorUser.getDeptId());
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
//            //查询团队图文咨询申请价格
//            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
//                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
//            patientOtherOrder.setAmount(one.getPrice());
//            patientOtherOrder.setHour(one.getHour());
        }
        patientOtherOrderService.save(patientOtherOrder);
        PayResultData data = new PayResultData();
        data.setOrderId(patientOtherOrder.getId());
        //添加待办事项
        Upcoming upcoming = new Upcoming();
        upcoming.setTeamId(patientOtherOrder.getDoctorTeamId());
        upcoming.setChatUserId(patientOtherOrder.getChatUserId());
        upcoming.setContent("图文咨询申请");
        upcoming.setTitle("图文咨询申请");
        upcoming.setUserId(SecurityUtils.getUser().getId());
        Integer doctorId = patientOtherOrder.getDoctorId();


        upcoming.setCreateTime(LocalDateTime.now());
        upcoming.setType("2");
        List<Upcoming> upcomingList = new ArrayList<>();
        if (doctorId != null) {
            upcoming.setDoctorId(doctorId);
            upcomingList.add(upcoming);
        } else {

            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                    Upcoming upcoming1 = new Upcoming();
                    upcoming1.setTeamId(patientOtherOrder.getDoctorTeamId());
                    upcoming1.setChatUserId(patientOtherOrder.getChatUserId());
                    upcoming1.setContent("图文咨询申请");
                    upcoming1.setTitle("图文咨询申请");
                    upcoming1.setUserId(SecurityUtils.getUser().getId());
                    upcoming1.setCreateTime(LocalDateTime.now());
                    upcoming1.setType("2");
                    upcoming1.setDoctorId(doctorTeamPeople.getUserId());
                    upcomingList.add(upcoming1);
                }


            }
        }
        upcomingService.saveBatch(upcomingList);
        //添加到redis 超过24小时如果医生没有接受则退款 返回使用次数
        String keyRedis = String.valueOf(StrUtil.format("{}{}", "patientOrder:", patientOtherOrder.getId()));
        redisTemplate.opsForValue().set(keyRedis, patientOtherOrder.getId(), 24, TimeUnit.HOURS);//设置过期时间

        updateChatDesc(patientOtherOrder.getChatUserId());
        return RestResponse.ok(data);
    }

    /**
     * 查询图文咨询详情
     *
     * @return
     */
    @GetMapping("/getByPatientOtherOrderId")
    public RestResponse getByPatientOtherOrderId(@RequestParam("id") Integer id) {
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getById(id);
        String imageUrl = patientOtherOrder.getImageUrl();
        if (!StringUtils.isEmpty(imageUrl)) {
            String[] split = imageUrl.split(",");
            List<String> strings = Arrays.asList(split);
            patientOtherOrder.setImageUrlList(strings);

        } else {
            patientOtherOrder.setImageUrlList(new ArrayList<>());
        }
        //查询患者信息
        User user = userService.getUserINfo(patientOtherOrder.getUserId());
        patientOtherOrder.setUser(user);
        //计算有效时长
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime createTime = patientOtherOrder.getCreateTime().plusHours(24);
        if (now.isBefore(createTime)) {
            Duration duration = java.time.Duration.between(createTime, now);
            String s1 = new String(duration.toString()).substring(3);// 转换成字符串类型，然后截取
            String replace = s1.replace("H", "时");
            String replace1 = replace.replace("M", "分");
            String[] s = replace1.split("分");
            patientOtherOrder.setEfficientHour(s[0] + "分");

        } else {
            patientOtherOrder.setEfficientHour("0时");
        }
        return RestResponse.ok(patientOtherOrder);
    }

    public static void main(String[] args) {
        LocalDateTime d1 = LocalDateTime.now();
        System.out.println(d1);


        LocalDateTime d2 = LocalDateTime.now();
        d2 = d2.plusHours(24);
        d2 = d2.plusMinutes(5);
        d2 = d2.plusSeconds(20);
        System.out.println(d2);

        Duration sjc = Duration.between(d2, d1);// 计算时间差
        System.out.println(sjc.toString());
        String s1 = new String(sjc.toString()).substring(3);// 转换成字符串类型，然后截取
        String replace = s1.replace("H", "时");
        String replace1 = replace.replace("M", "分");
        String[] s = replace1.split("分");
        System.out.println(s[0]);

    }

    @Override
    protected Class<DoctorPoint> getEntityClass() {
        return DoctorPoint.class;
    }
}
