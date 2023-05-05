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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Resource
    private PatientUserService patientUserService;

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


        queryWrapper.orderByDesc("create_time", "withdraw_status");
        IPage<DoctorPoint> doctorPointIPage = service.page(page, queryWrapper);

        List<DoctorPoint> records = doctorPointIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> doctorTeamIds = new ArrayList<>();
            for (DoctorPoint doctorPoint : records) {
                if (doctorPoint.getDoctorTeamId() != null) {
                    doctorTeamIds.add(doctorPoint.getDoctorTeamId());
                }
            }
            if (!CollectionUtils.isEmpty(doctorTeamIds)) {
                doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(doctorTeamIds);
            }
        }
        if (!CollectionUtils.isEmpty(records)) {
            Map<Integer, DoctorTeam> doctorMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(doctorTeams)) {
                doctorMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            }


            //查询患者名字
            List<Integer> patientIds = records.stream().map(DoctorPoint::getPatientId)
                    .collect(Collectors.toList());

            List<User> users = (List<User>) userService.listByIds(patientIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (DoctorPoint doctorPoint : records) {
                DoctorTeam doctorTeam = doctorMap.get(doctorPoint.getDoctorTeamId());
                if (doctorTeam != null) {
                    doctorPoint.setDoctorTeamName(doctorTeam.getName());
                }
                if (userMap.get(doctorPoint.getPatientId()) != null) {
                    doctorPoint.setPatientName(userMap.get(doctorPoint.getPatientId()).getPatientName());
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
        LambdaQueryWrapper<DoctorPoint> eq = new QueryWrapper<DoctorPoint>().lambda()
                .eq(DoctorPoint::getDoctorUserId, SecurityUtils.getUser().getId());

        DoctorPointCountResult result = new DoctorPointCountResult();
        List<DoctorPoint> list = service.list(eq);
        BigDecimal totalPoint = new BigDecimal(0.0);
        BigDecimal pendingWithdraw = new BigDecimal(0.0);
        BigDecimal withdraw = new BigDecimal(0.0);
        if (!CollectionUtils.isEmpty(list)) {
            for (DoctorPoint doctorPoint : list) {
                totalPoint = new BigDecimal(doctorPoint.getPoint()).add(totalPoint);
                if (doctorPoint.getWithdrawStatus().equals(1)) {
                    pendingWithdraw = pendingWithdraw.add(new BigDecimal(doctorPoint.getPoint()));
                } else {
                    withdraw = withdraw.add(new BigDecimal(doctorPoint.getPoint()));
                }
            }
        }
        result.setTotalPoint(totalPoint.setScale(2, RoundingMode.HALF_UP));
        result.setWithdraw(withdraw.setScale(2, RoundingMode.HALF_UP));
        result.setPendingWithdraw(pendingWithdraw.setScale(2, RoundingMode.HALF_UP));
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
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId())
                    .eq(DoctorUserAction::getDoctorUserServiceSetUpId, 1));
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
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId())
                    .eq(DoctorUserAction::getDoctorUserServiceSetUpId, 1)
                    .isNull(DoctorUserAction::getTeamId));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(24);
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
            //查询团队图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(24);

            //处理新的图文咨询 后加入团队的医生也可以看到
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<Integer> doctorIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                ChatUser chatUser = chatUserService.getById(patientOtherOrder.getChatUserId());
                String userIds = chatUser.getUserIds();
                for (Integer doctorId : doctorIds) {
                    if (userIds.indexOf(doctorId + "") < 0) {
                        userIds = userIds + "," + doctorId;
                    }
                }
                chatUser.setUserIds(userIds);
                chatUserService.updateById(chatUser);
            }

        }
        patientOtherOrderService.save(patientOtherOrder);
        //添加待办事项
        Upcoming upcoming = new Upcoming();
        upcoming.setOrderId(patientOtherOrder.getId());
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
                    upcoming1.setOrderId(patientOtherOrder.getId());
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
        //updateChatDesc(chatUserId,patientOtherOrder.getId());

        return restResponse;
    }

    private void updateChatDesc(Integer chatUserId, Integer id) {
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
                                    .set(ChatUser::getPatientOtherOrderStatus, "0")
                                    .set(ChatUser::getPatientOtherOrderNo, id + "")
                                    .set(ChatUser::getChatCount, 9)

                            );
                        }
                    });
                } else {
                    chatUser.setChatDesc("咨询");
                    chatUser.setChatCount(9);
                    chatUser.setPatientOtherOrderStatus("0");
                    chatUser.setPatientOtherOrderNo(id + "");
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
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId())
                    .eq(DoctorUserAction::getDoctorUserServiceSetUpId, 1)
                    .isNull(DoctorUserAction::getTeamId));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(24);
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
            //处理新的图文咨询 后加入团队的医生也可以看到
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<Integer> doctorIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                ChatUser chatUser = chatUserService.getById(patientOtherOrder.getChatUserId());
                String userIds = chatUser.getUserIds();
                for (Integer doctorId : doctorIds) {
                    if (userIds.indexOf(doctorId + "") < 0) {
                        userIds = userIds + "," + doctorId;
                    }
                }
                chatUser.setUserIds(userIds);
                chatUserService.updateById(chatUser);
            }
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
        upcoming.setOrderId(patientOtherOrder.getId());
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
                    upcoming1.setOrderId(patientOtherOrder.getId());
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

        updateChatDesc(patientOtherOrder.getChatUserId(), patientOtherOrder.getId());
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
        String efficientHour = patientOtherOrder.getEfficientHour();
        String replace = efficientHour.replace("-", "");
        patientOtherOrder.setEfficientHour(replace);
        return RestResponse.ok(patientOtherOrder);
    }

    /**
     * 分页查询图文咨询
     *
     * @return
     */
    @GetMapping("/pagePatientOtherOrder")
    public RestResponse pagePatientOtherOrder(@RequestParam("pageNum") Integer pageNum,
                                              @RequestParam(value = "doctorTeamName", required = false) String doctorTeamName,
                                              @RequestParam(value = "doctorName", required = false) String doctorName,
                                              @RequestParam(value = "userName", required = false) String userName,
                                              @RequestParam("pageSize") Integer pageSize) {
        IPage page = new Page(pageNum, pageSize);
        User user = userService.getById(SecurityUtils.getUser().getId());

        LambdaQueryWrapper<PatientOtherOrder> eq = new QueryWrapper<PatientOtherOrder>().lambda().eq(PatientOtherOrder::getDeptId, user.getDeptId());
        if (!StringUtils.isEmpty(userName)) {
            List<PatientUser> patientUsers = patientUserService.list(new QueryWrapper<PatientUser>().lambda().like(PatientUser::getName, userName));
            if (!CollectionUtils.isEmpty(patientUsers)) {
                List<Integer> userIds = patientUsers.stream().map(PatientUser::getUserId)
                        .collect(Collectors.toList());
                eq.in(PatientOtherOrder::getUserId, userIds);

            }
        }
        if (!StringUtils.isEmpty(doctorTeamName)) {
            List<DoctorTeam> doctorTeams = doctorTeamService.list(new QueryWrapper<DoctorTeam>().lambda().like(DoctorTeam::getName, doctorTeamName));
            if (!CollectionUtils.isEmpty(doctorTeams)) {
                List<Integer> teamIds = doctorTeams.stream().map(DoctorTeam::getId)
                        .collect(Collectors.toList());
                eq.in(PatientOtherOrder::getDoctorTeamId, teamIds);

            }
        }
        if (!StringUtils.isEmpty(doctorName)) {
            List<User> users = userService.list(new QueryWrapper<User>().lambda().like(User::getNickname, doctorName));
            if (!CollectionUtils.isEmpty(users)) {
                List<Integer> userIds = users.stream().map(User::getId)
                        .collect(Collectors.toList());
                eq.in(PatientOtherOrder::getDoctorId, userIds);
            }
        }
        IPage page1 = patientOtherOrderService.page(page, eq);
        List<PatientOtherOrder> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> userIds = new ArrayList<>();
            List<Integer> doctorIds = new ArrayList<>();
            List<Integer> teamIds = new ArrayList<>();
            for (PatientOtherOrder patientOtherOrder : records) {
                userIds.add(patientOtherOrder.getUserId());
                if (patientOtherOrder.getDoctorId() != null) {
                    doctorIds.add(patientOtherOrder.getDoctorId());
                }
                if (patientOtherOrder.getDoctorTeamId() != null) {
                    teamIds.add(patientOtherOrder.getDoctorTeamId());
                }
            }
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            Map<Integer, User> doctorMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(doctorIds)) {

                List<User> doctorUsers = (List<User>) userService.listByIds(doctorIds);
                doctorMap = doctorUsers.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
            }
            Map<Integer, DoctorTeam> doctorTeamMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(teamIds)) {

                List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
                doctorTeamMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            }

            for (PatientOtherOrder patientOtherOrder : records) {
                User user1 = userMap.get(patientOtherOrder.getUserId());
                if (user1 != null) {
                    patientOtherOrder.setUser(user1);
                }
                DoctorTeam doctorTeam = doctorTeamMap.get(patientOtherOrder.getDoctorTeamId());
                patientOtherOrder.setDoctorTeam(doctorTeam);
                patientOtherOrder.setDoctor(doctorMap.get(patientOtherOrder.getDoctorId()));
            }
        }
        return RestResponse.ok(page1);
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
        String replace111 = s[0].replace("-", "");
        System.out.println(replace111);

    }

    @Override
    protected Class<DoctorPoint> getEntityClass() {
        return DoctorPoint.class;
    }
}
