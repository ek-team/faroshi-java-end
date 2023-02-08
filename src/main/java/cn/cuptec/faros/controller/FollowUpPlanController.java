package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.properties.RedisConfigProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 随访计划管理
 */
@AllArgsConstructor
@RestController
@RequestMapping("/followUpPlan")
public class FollowUpPlanController extends AbstractBaseController<FollowUpPlanService, FollowUpPlan> {
    private final RedisConfigProperties redisConfigProperties;
    @Resource
    private FollowUpPlanContentService followUpPlanContentService;
    @Resource
    private FollowUpPlanPatientUserService followUpPlanPatientUserService;
    @Resource
    private UserService userService;
    @Resource
    private FormService formService;
    @Resource
    private UserFollowDoctorService userFollowDoctorService;//医生和患者的好友表
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;//随访计划通知模版
    @Autowired
    public RedisTemplate redisTemplate;
    @Resource
    private FollowUpPlanNoticeCountService followUpPlanNoticeCountService;//随访计划推送次数记录
    @Resource
    private ElectronicCaseService electronicCaseService;
    @Resource
    private InquiryService inquiryService;
    @Resource
    private WxMpService wxMpService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private HospitalInfoService hospitalInfoService;

    /**
     * 添加随访计划
     *
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody FollowUpPlan followUpPlan) {
        followUpPlan.setCreateUserId(SecurityUtils.getUser().getId());
        followUpPlan.setCreateTime(LocalDateTime.now());
        User byId = userService.getById(SecurityUtils.getUser().getId());
        followUpPlan.setDeptId(byId.getDeptId());
        service.save(followUpPlan);
        List<FollowUpPlanContent> followUpPlanContentList = followUpPlan.getFollowUpPlanContentList();
        if (!CollectionUtils.isEmpty(followUpPlanContentList)) {
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                followUpPlanContent.setFollowUpPlanId(followUpPlan.getId());
            }
            followUpPlanContentService.saveBatch(followUpPlanContentList);
        }
        List<Integer> userIds = followUpPlan.getFollowUpPlanPatientUsers();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<FollowUpPlanPatientUser> followUpPlanPatientUsers = new ArrayList<>();
            for (Integer userId : userIds) {
                FollowUpPlanPatientUser followUpPlanPatientUser = new FollowUpPlanPatientUser();
                followUpPlanPatientUser.setFollowUpPlanId(followUpPlan.getId());
                followUpPlanPatientUser.setUserId(userId);
                followUpPlanPatientUsers.add(followUpPlanPatientUser);


            }
            followUpPlanPatientUserService.saveBatch(followUpPlanPatientUsers);
            followUpPlan.setPatientUserCount(followUpPlanPatientUsers.size());

        }
        service.updateById(followUpPlan);
        //添加随访计划通知记录
        List<FollowUpPlanNotice> followUpPlanNoticeList = new ArrayList<>();
        List<FollowUpPlanNoticeCount> followUpPlanNoticeCountList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(followUpPlanContentList) && !CollectionUtils.isEmpty(userIds)) {

            List<Integer> countUserIds = new ArrayList<>();
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {

                for (Integer userId : userIds) {
                    //随访计划记录
                    LocalDateTime pushDay = followUpPlanContent.getDay();

                    FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();
                    followUpPlanNotice.setFollowUpPlanId(followUpPlan.getId());
                    followUpPlanNotice.setPatientUserId(userId);
                    followUpPlanNotice.setNoticeTime(pushDay);

                    followUpPlanNotice.setDoctorId(followUpPlan.getCreateUserId());
                    followUpPlanNotice.setFollowUpPlanContentId(followUpPlanContent.getId());


                    followUpPlanNoticeList.add(followUpPlanNotice);

                    //通知次数记录
                    if (!countUserIds.contains(userId)) {
                        countUserIds.add(userId);
                        FollowUpPlanNoticeCount count = new FollowUpPlanNoticeCount();
                        count.setTotalPush(followUpPlanContentList.size());
                        count.setDoctorId(followUpPlan.getCreateUserId());
                        count.setPush(0);
                        count.setFollowUpPlanId(followUpPlan.getId());
                        count.setPatientUserId(userId);
                        followUpPlanNoticeCountList.add(count);
                    }

                }

            }
            followUpPlanNoticeCountService.saveBatch(followUpPlanNoticeCountList);
            if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                followUpPlanNoticeService.saveBatch(followUpPlanNoticeList);
                for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                    LocalDateTime noticeTime = followUpPlanNotice.getNoticeTime();
                    LocalDateTime thisNow = LocalDateTime.now();
                    java.time.Duration duration = java.time.Duration.between(thisNow, noticeTime);
                    long hours = duration.toHours();//分钟
                    String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                    redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.HOURS);//设置过期时间

                }

            }


        }

        //判断是否是首次加入推送
        if (followUpPlan.getPushType() != null && followUpPlan.getPushType() == 1) {
            pushFollowUpPlan(followUpPlan.getCreateUserId());
        }
        return RestResponse.ok(followUpPlan);
    }

    private void pushFollowUpPlan(Integer doctorUserId) {

        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                User doctorUser = userService.getById(doctorUserId);
                List<User> users = new ArrayList<>();
                users.add(doctorUser);
                hospitalInfoService.getHospitalByUser(users);
                doctorUser = users.get(0);
                LocalDate now = LocalDate.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String format = df.format(now);
                String startTime = format + " 00:00:00";
                String endTime = format + " 24:00:00";
                List<FollowUpPlanNotice> followUpPlanNoticeList = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                        .ge(FollowUpPlanNotice::getNoticeTime, startTime)
                        .le(FollowUpPlanNotice::getNoticeTime, endTime)
                        .le(FollowUpPlanNotice::getDoctorId, doctorUserId));
                //推送计划给患者
                //发送公众号随访计划提醒

                if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                    List<Integer> patientUserIds = followUpPlanNoticeList.stream().map(FollowUpPlanNotice::getPatientUserId)
                            .collect(Collectors.toList());
                    List<User> users1 = (List<User>) userService.listByIds(patientUserIds);
                    Map<Integer, User> userMap = users1.stream()
                            .collect(Collectors.toMap(User::getId, t -> t));
                    for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                        wxMpService.sendFollowUpPlanNotice(userMap.get(followUpPlanNotice.getPatientUserId()).getMpOpenId(), "新的康复计划提醒", doctorUser.getNickname(), doctorUser.getHospitalName(), "/pages/news/news");
                        //生成聊天记录
                        List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                                .eq(ChatUser::getUid, followUpPlanNotice.getPatientUserId())
                                .eq(ChatUser::getTargetUid, followUpPlanNotice.getDoctorId()));
                        if (CollectionUtils.isEmpty(chatUsers)) {
                            //创建聊天对象
                            chatUserService.saveOrUpdateChatUser(followUpPlanNotice.getDoctorId(), followUpPlanNotice.getPatientUserId(), "随访计划提醒");
                        }
                        ChatMsg chatMsg = new ChatMsg();
                        chatMsg.setFromUid(followUpPlanNotice.getDoctorId());
                        chatMsg.setToUid(followUpPlanNotice.getPatientUserId());
                        chatMsg.setMsg("随访计划提醒");
                        chatMsg.setCreateTime(new Date());
                        chatMsg.setMsgType(ChatProto.FOLLOW_UP_PLAN);
                        chatMsg.setStr1(followUpPlanNotice.getId() + "");
                        chatMsg.setReadStatus(0);
                        chatMsgService.save(chatMsg);

                        //修改发送次数
                        FollowUpPlanNoticeCount one = followUpPlanNoticeCountService.getOne(new QueryWrapper<FollowUpPlanNoticeCount>()
                                .lambda().eq(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanNotice.getFollowUpPlanId())
                                .eq(FollowUpPlanNoticeCount::getPatientUserId, followUpPlanNotice.getPatientUserId()));
                        one.setPush(one.getPush() + 1);
                        followUpPlanNoticeCountService.updateById(one);
                    }

                }

            }
        });


    }

    /**
     * 测试redis通知
     *
     * @return
     */
    @GetMapping("/testRedis")
    public RestResponse testRedis(@RequestParam("minutes") Integer minutes, @RequestParam("followUpPlanNoticeId") Integer followUpPlanNoticeId) {

        String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNoticeId));
        redisTemplate.opsForValue().set(keyRedis, followUpPlanNoticeId, minutes, TimeUnit.MINUTES);//设置过期时间

        return RestResponse.ok(redisConfigProperties.getPassword());
    }

    /**
     * 编辑随访计划
     *
     * @return
     */
    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody FollowUpPlan followUpPlan) {
        List<FollowUpPlanContent> oldFollowUpPlanContentList = followUpPlanContentService.list(new QueryWrapper<FollowUpPlanContent>().lambda()
                .eq(FollowUpPlanContent::getFollowUpPlanId, followUpPlan.getId()));


        List<FollowUpPlanContent> followUpPlanContentList = followUpPlan.getFollowUpPlanContentList();

        if (!CollectionUtils.isEmpty(followUpPlanContentList)) {
            List<FollowUpPlanContent> saveFollowUpPlanContentList = new ArrayList<>();
            List<FollowUpPlanContent> updateFollowUpPlanContentList = new ArrayList<>();
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                followUpPlanContent.setFollowUpPlanId(followUpPlan.getId());
                if (followUpPlanContent.getId() == null) {
                    saveFollowUpPlanContentList.add(followUpPlanContent);
                } else {
                    updateFollowUpPlanContentList.add(followUpPlanContent);
                }
                followUpPlanContent.setFollowUpPlanId(followUpPlan.getId());
            }
            if (!CollectionUtils.isEmpty(updateFollowUpPlanContentList)) {
                followUpPlanContentService.updateBatchById(updateFollowUpPlanContentList);
            }
            if (!CollectionUtils.isEmpty(saveFollowUpPlanContentList)) {
                followUpPlanContentService.saveBatch(saveFollowUpPlanContentList);
            }
        }
        followUpPlanPatientUserService.remove(new QueryWrapper<FollowUpPlanPatientUser>().lambda()
                .eq(FollowUpPlanPatientUser::getFollowUpPlanId, followUpPlan.getId()));
        List<Integer> userIds = followUpPlan.getFollowUpPlanPatientUsers();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<FollowUpPlanPatientUser> followUpPlanPatientUsers = new ArrayList<>();
            for (Integer userId : userIds) {
                FollowUpPlanPatientUser followUpPlanPatientUser = new FollowUpPlanPatientUser();
                followUpPlanPatientUser.setFollowUpPlanId(followUpPlan.getId());
                followUpPlanPatientUser.setUserId(userId);
                followUpPlanPatientUsers.add(followUpPlanPatientUser);
            }
            followUpPlanPatientUserService.saveBatch(followUpPlanPatientUsers);
            followUpPlan.setPatientUserCount(followUpPlanPatientUsers.size());

        }
        service.updateById(followUpPlan);
        //修改随访计划通知记录
        List<FollowUpPlanNotice> followUpPlanNoticeList = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getFollowUpPlanId, followUpPlan.getId()));

        if (!CollectionUtils.isEmpty(followUpPlanContentList) && !CollectionUtils.isEmpty(userIds)) {
            Collections.sort(followUpPlanContentList, (o1, o2) -> {
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(o1.getDay(), o2.getDay());
            });
            List<FollowUpPlanNotice> newFollowUpPlanNoticeList = new ArrayList<>();
            List<FollowUpPlanNoticeCount> followUpPlanNoticeCountList = new ArrayList<>();
            List<Integer> countUserIds = new ArrayList<>();

            if (CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                //如果记录是空就全部添加
                for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                    for (Integer userId : userIds) {
                        //随访计划记录
                        LocalDateTime noticeTime = followUpPlanContent.getDay();
                        FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();
                        followUpPlanNotice.setFollowUpPlanId(followUpPlan.getId());
                        followUpPlanNotice.setPatientUserId(userId);
                        followUpPlanNotice.setNoticeTime(noticeTime);
                        followUpPlanNotice.setDoctorId(followUpPlan.getCreateUserId());
                        followUpPlanNotice.setFollowUpPlanContentId(followUpPlanContent.getId());
                        followUpPlanNoticeList.add(followUpPlanNotice);
                        LocalDateTime thisNow = LocalDateTime.now();
                        java.time.Duration duration = java.time.Duration.between(thisNow, noticeTime);
                        long hours = duration.toHours();//分钟
                        String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                        redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.HOURS);//设置过期时间


                        //通知次数记录
                        if (!countUserIds.contains(userId)) {
                            countUserIds.add(userId);
                            FollowUpPlanNoticeCount count = new FollowUpPlanNoticeCount();
                            count.setTotalPush(followUpPlanContentList.size());
                            count.setDoctorId(followUpPlan.getCreateUserId());
                            count.setPush(0);
                            count.setFollowUpPlanId(followUpPlan.getId());
                            count.setPatientUserId(userId);
                            followUpPlanNoticeCountList.add(count);
                        }
                    }

                }

                followUpPlanNoticeService.saveBatch(newFollowUpPlanNoticeList);

            } else {

                List<Integer> patientUserIds = followUpPlanNoticeList.stream().map(FollowUpPlanNotice::getPatientUserId)
                        .collect(Collectors.toList());

                List<LocalDateTime> followUpPlanContentDays = oldFollowUpPlanContentList.stream().map(FollowUpPlanContent::getDay)
                        .collect(Collectors.toList());

                for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                    for (Integer userId : userIds) {
                        if (!patientUserIds.contains(userId)) {//处理有新增患者 给新的患者添加通知记录
                            //随访计划记录
                            LocalDateTime noticeTime = followUpPlanContent.getDay();

                            FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();
                            followUpPlanNotice.setFollowUpPlanId(followUpPlan.getId());
                            followUpPlanNotice.setFollowUpPlanContentId(followUpPlanContent.getId());
                            followUpPlanNotice.setPatientUserId(userId);
                            followUpPlanNotice.setNoticeTime(noticeTime);

                            followUpPlanNotice.setDoctorId(followUpPlan.getCreateUserId());
                            LocalDateTime thisNow = LocalDateTime.now();
                            java.time.Duration duration = java.time.Duration.between(thisNow, noticeTime);
                            long hours = duration.toHours();//分钟
                            String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                            redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.HOURS);//设置过期时间

                            newFollowUpPlanNoticeList.add(followUpPlanNotice);

                            //通知次数记录
                            if (!countUserIds.contains(userId)) {
                                countUserIds.add(userId);
                                FollowUpPlanNoticeCount count = new FollowUpPlanNoticeCount();
                                count.setTotalPush(followUpPlanContentList.size());
                                count.setDoctorId(followUpPlan.getCreateUserId());
                                count.setPush(0);
                                count.setFollowUpPlanId(followUpPlan.getId());
                                count.setPatientUserId(userId);
                                followUpPlanNoticeCountList.add(count);
                            }
                        }
                        //处理新增计划给老的患者添加记录
                        if (!followUpPlanContentDays.contains(followUpPlanContent.getDay())) {
                            if (patientUserIds.contains(userId)) {
                                LocalDateTime noticeTime = followUpPlanContent.getDay();

                                FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();
                                followUpPlanNotice.setFollowUpPlanId(followUpPlan.getId());
                                followUpPlanNotice.setFollowUpPlanContentId(followUpPlanContent.getId());
                                followUpPlanNotice.setPatientUserId(userId);
                                followUpPlanNotice.setNoticeTime(noticeTime);

                                followUpPlanNotice.setDoctorId(followUpPlan.getCreateUserId());
                                LocalDateTime thisNow = LocalDateTime.now();
                                java.time.Duration duration = java.time.Duration.between(thisNow, noticeTime);
                                long hours = duration.toHours();//分钟
                                String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                                redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.HOURS);//设置过期时间

                                newFollowUpPlanNoticeList.add(followUpPlanNotice);


                                FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountService.getOne(new QueryWrapper<FollowUpPlanNoticeCount>().lambda()
                                        .eq(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlan.getId())
                                        .eq(FollowUpPlanNoticeCount::getPatientUserId, userId)
                                        .eq(FollowUpPlanNoticeCount::getDoctorId, followUpPlan.getCreateUserId()));
                                if (followUpPlanNoticeCount != null) {
                                    followUpPlanNoticeCount.setTotalPush(followUpPlanNoticeCount.getTotalPush() + followUpPlanContentList.size());
                                    followUpPlanNoticeCountService.updateById(followUpPlanNoticeCount);
                                }

                            }


                        }
                    }
                }

                if (!CollectionUtils.isEmpty(newFollowUpPlanNoticeList)) {
                    followUpPlanNoticeService.saveBatch(newFollowUpPlanNoticeList);
                    for (FollowUpPlanNotice followUpPlanNotice : newFollowUpPlanNoticeList) {
                        LocalDateTime noticeTime = followUpPlanNotice.getNoticeTime();
                        LocalDateTime thisNow = LocalDateTime.now();
                        java.time.Duration duration = java.time.Duration.between(thisNow, noticeTime);
                        long hours = duration.toHours();//分钟
                        String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                        redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.HOURS);//设置过期时间

                    }

                }
                if (!CollectionUtils.isEmpty(followUpPlanNoticeCountList)) {
                    followUpPlanNoticeCountService.saveBatch(followUpPlanNoticeCountList);
                }

            }


        }


        return RestResponse.ok(followUpPlan);
    }

    /**
     * 列表查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageScoped() {
        Page<FollowUpPlan> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("create_user_id", SecurityUtils.getUser().getId());
        IPage<FollowUpPlan> followUpPlanIPage = service.page(page, queryWrapper);
        List<FollowUpPlan> records = followUpPlanIPage.getRecords();
        return RestResponse.ok(followUpPlanIPage);
    }

    /**
     * 查询随访计划详情
     *
     * @return
     */
    @GetMapping("/getDetailById")
    public RestResponse getDetailById(@RequestParam("id") Integer id) {
        FollowUpPlan followUpPlan = service.getById(id);
        List<FollowUpPlanPatientUser> followUpPlanPatientUsers = followUpPlanPatientUserService.list(new QueryWrapper<FollowUpPlanPatientUser>().lambda()
                .eq(FollowUpPlanPatientUser::getFollowUpPlanId, id));
        if (!CollectionUtils.isEmpty(followUpPlanPatientUsers)) {
            List<Integer> userIds = followUpPlanPatientUsers.stream().map(FollowUpPlanPatientUser::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);

            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (FollowUpPlanPatientUser followUpPlanPatientUser : followUpPlanPatientUsers) {
                followUpPlanPatientUser.setUser(userMap.get(followUpPlanPatientUser.getUserId()));
            }
        }

        List<FollowUpPlanContent> followUpPlanContentList = followUpPlanContentService.list(new QueryWrapper<FollowUpPlanContent>().lambda()
                .eq(FollowUpPlanContent::getFollowUpPlanId, id));
        if (!CollectionUtils.isEmpty(followUpPlanContentList)) {
            //查询表单信息
            List<Integer> formIds = followUpPlanContentList.stream().map(FollowUpPlanContent::getFormId)
                    .collect(Collectors.toList());
            List<Form> forms = (List<Form>) formService.listByIds(formIds);
            Map<Integer, Form> formMap = forms.stream()
                    .collect(Collectors.toMap(Form::getId, t -> t));
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                followUpPlanContent.setForm(formMap.get(followUpPlanContent.getFormId()));
            }
        }
        followUpPlan.setFollowUpPlanPatientUserList(followUpPlanPatientUsers);
        followUpPlan.setFollowUpPlanContentList(followUpPlanContentList);
        return RestResponse.ok(followUpPlan);
    }

    /**
     * 查询医生绑定的患者
     *
     * @return
     */
    @GetMapping("/getPatientUserByDoctor")
    public RestResponse getPatientUserByDoctor() {
        List<UserFollowDoctor> list = userFollowDoctorService.list(new QueryWrapper<UserFollowDoctor>().lambda()
                .eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<Integer> userIds = list.stream().map(UserFollowDoctor::getUserId)
                .collect(Collectors.toList());
        return RestResponse.ok(userService.listByIds(userIds));
    }

    /**
     * 查询今日计划的患者数量
     *
     * @return
     */
    @GetMapping("/getThisDayPlanCount")
    public RestResponse getThisDayPlanCount() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = df.format(now);
        String startTime = format + " 00:00:00";
        String endTime = format + " 24:00:00";
        int count = followUpPlanNoticeService.count(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getDoctorId, SecurityUtils.getUser().getId())
                .ge(FollowUpPlanNotice::getNoticeTime, startTime)
        );
        return RestResponse.ok(count);
    }

    /**
     * 查询今日计划的患者
     *
     * @return
     */
    @GetMapping("/getThisDayPatient")
    public RestResponse getThisDayPatient() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = df.format(now);
        String startTime = format + " 00:00:00";
        String endTime = format + " 24:00:00";
        List<FollowUpPlanNotice> list = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getDoctorId, SecurityUtils.getUser().getId())
                .ge(FollowUpPlanNotice::getNoticeTime, startTime)
                .le(FollowUpPlanNotice::getNoticeTime, endTime));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        list = list.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FollowUpPlanNotice::getPatientUserId))), ArrayList::new)
        );

        //查询计划名称
        List<Integer> followUpPlanIds = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> followUpPlanContentIds = new ArrayList<>();
        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanIds.add(followUpPlanNotice.getFollowUpPlanId());
            userIds.add(followUpPlanNotice.getPatientUserId());
            followUpPlanContentIds.add(followUpPlanNotice.getFollowUpPlanContentId());
        }
        List<FollowUpPlan> followUpPlans = (List<FollowUpPlan>) service.listByIds(followUpPlanIds);
        Map<Integer, FollowUpPlan> followUpPlanMap = followUpPlans.stream()
                .collect(Collectors.toMap(FollowUpPlan::getId, t -> t));

        //查询推送内容
        List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);
        //查询表单名称
        List<Integer> formIds = new ArrayList<>();
        for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
            if (followUpPlanContent.getFormId() != null) {
                formIds.add(followUpPlanContent.getFormId());
            }
        }
        if (!CollectionUtils.isEmpty(formIds)) {
            List<Form> forms = (List<Form>) formService.listByIds(formIds);
            if (!CollectionUtils.isEmpty(forms)) {
                Map<Integer, Form> formMap = forms.stream()
                        .collect(Collectors.toMap(Form::getId, t -> t));
                for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
                    if (followUpPlanContent.getFormId() != null) {
                        followUpPlanContent.setForm(formMap.get(followUpPlanContent.getFormId()));
                    }
                }
            }


        }


        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = followUpPlanContents.stream()
                .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));

        //查询用户信息
        List<User> users = (List<User>) userService.listByIds(userIds);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, t -> t));
        //查询计划推送次数
        List<FollowUpPlanNoticeCount> followUpPlanNoticeCounts = followUpPlanNoticeCountService.list(new QueryWrapper<FollowUpPlanNoticeCount>()
                .lambda()
                .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds));
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
            followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
                    .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));


        }

        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanNotice.setFollowUpPlan(followUpPlanMap.get(followUpPlanNotice.getFollowUpPlanId()));
            followUpPlanNotice.setUser(userMap.get(followUpPlanNotice.getPatientUserId()));
            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
            FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountMap.get(followUpPlanNotice.getFollowUpPlanId());
            if (followUpPlanNoticeCount != null) {
                followUpPlanNotice.setTotalPush(followUpPlanNoticeCount.getTotalPush());
                followUpPlanNotice.setPush(followUpPlanNoticeCount.getPush());

            } else {
                followUpPlanNotice.setTotalPush(0);
                followUpPlanNotice.setPush(0);
            }
        }
        return RestResponse.ok(list);
    }

    /**
     * 查询今日推送的计划
     *
     * @return
     */
    @GetMapping("/getThisDayFollowUpPlan")
    public RestResponse getThisDayFollowUpPlan() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = df.format(now);
        String startTime = format + " 00:00:00";
        String endTime = format + " 24:00:00";
        List<FollowUpPlanNotice> list = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getDoctorId, SecurityUtils.getUser().getId())
                .ge(FollowUpPlanNotice::getNoticeTime, startTime)
                .le(FollowUpPlanNotice::getNoticeTime, endTime));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        list = list.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FollowUpPlanNotice::getFollowUpPlanId))), ArrayList::new)
        );

        //查询计划名称
        List<Integer> followUpPlanIds = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> followUpPlanContentIds = new ArrayList<>();
        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanIds.add(followUpPlanNotice.getFollowUpPlanId());
            userIds.add(followUpPlanNotice.getPatientUserId());
            followUpPlanContentIds.add(followUpPlanNotice.getFollowUpPlanContentId());
        }
        List<FollowUpPlan> followUpPlans = (List<FollowUpPlan>) service.listByIds(followUpPlanIds);
        Map<Integer, FollowUpPlan> followUpPlanMap = followUpPlans.stream()
                .collect(Collectors.toMap(FollowUpPlan::getId, t -> t));

        //查询推送内容
        List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);
        //查询表单名称
        List<Integer> formIds = new ArrayList<>();
        for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
            if (followUpPlanContent.getFormId() != null) {
                formIds.add(followUpPlanContent.getFormId());
            }
        }
        if (!CollectionUtils.isEmpty(formIds)) {
            List<Form> forms = (List<Form>) formService.listByIds(formIds);
            if (!CollectionUtils.isEmpty(forms)) {
                Map<Integer, Form> formMap = forms.stream()
                        .collect(Collectors.toMap(Form::getId, t -> t));
                for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
                    if (followUpPlanContent.getFormId() != null) {
                        followUpPlanContent.setForm(formMap.get(followUpPlanContent.getFormId()));
                    }
                }
            }


        }

        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = followUpPlanContents.stream()
                .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));
        //查询计划推送次数
        List<FollowUpPlanNoticeCount> followUpPlanNoticeCounts = followUpPlanNoticeCountService.list(new QueryWrapper<FollowUpPlanNoticeCount>()
                .lambda()
                .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds));
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
            followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
                    .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));


        }

        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanNotice.setFollowUpPlan(followUpPlanMap.get(followUpPlanNotice.getFollowUpPlanId()));
            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
            FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountMap.get(followUpPlanNotice.getFollowUpPlanId());
            if (followUpPlanNoticeCount != null) {
                followUpPlanNotice.setTotalPush(followUpPlanNoticeCount.getTotalPush());
                followUpPlanNotice.setPush(followUpPlanNoticeCount.getPush());

            } else {
                followUpPlanNotice.setTotalPush(0);
                followUpPlanNotice.setPush(0);
            }
        }
        return RestResponse.ok(list);
    }

    /**
     * 医生端查询患者详情
     *
     * @return
     */
    @GetMapping("/getPatientDetail")
    public RestResponse getPatientDetail(@RequestParam("patientId") Integer patientId) {
        User user = userService.getById(patientId);
        //查询患者的所有计划
        LocalDate now = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = df.format(now);
        String startTime = format + " 00:00:00";
        String endTime = format + " 24:00:00";

        List<FollowUpPlanNotice> list = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getPatientUserId, patientId)
                .ge(FollowUpPlanNotice::getNoticeTime, startTime)
                .le(FollowUpPlanNotice::getNoticeTime, endTime));
        list = list.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FollowUpPlanNotice::getFollowUpPlanId))), ArrayList::new)
        );

        //查询计划名称
        List<Integer> followUpPlanIds = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> followUpPlanContentIds = new ArrayList<>();
        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanIds.add(followUpPlanNotice.getFollowUpPlanId());
            userIds.add(followUpPlanNotice.getPatientUserId());
            followUpPlanContentIds.add(followUpPlanNotice.getFollowUpPlanContentId());
        }
        Map<Integer, FollowUpPlan> followUpPlanMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            List<FollowUpPlan> followUpPlans = (List<FollowUpPlan>) service.listByIds(followUpPlanIds);
            followUpPlanMap = followUpPlans.stream()
                    .collect(Collectors.toMap(FollowUpPlan::getId, t -> t));
        }


        //查询推送内容
        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanContentIds)) {
            List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);

            followUpPlanContentMap = followUpPlanContents.stream()
                    .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));
        }
        //查询计划推送次数
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            List<FollowUpPlanNoticeCount> followUpPlanNoticeCounts = followUpPlanNoticeCountService.list(new QueryWrapper<FollowUpPlanNoticeCount>()
                    .lambda().eq(FollowUpPlanNoticeCount::getPatientUserId, patientId)
                    .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds));
            if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
                followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
                        .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));


            }
        }


        //查询电子病例
        List<ElectronicCase> electronicCaseList = electronicCaseService.list(new QueryWrapper<ElectronicCase>().lambda()
                .eq(ElectronicCase::getPatientId, patientId).orderByDesc(ElectronicCase::getCreateTime).last(" limit 1"));

        if (!CollectionUtils.isEmpty(electronicCaseList)) {
            //查询医生名字
            List<Integer> doctorIds = electronicCaseList.stream().map(ElectronicCase::getCreateUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(doctorIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (ElectronicCase electronicCase : electronicCaseList) {
                if (userMap.get(electronicCase.getCreateUserId()) != null) {
                    electronicCase.setCreateUserName(userMap.get(electronicCase.getCreateUserId()).getNickname());

                }
            }
            user.setElectronicCase(electronicCaseList.get(0));
        } else {
            user.setElectronicCase(null);
        }

        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanNotice.setFollowUpPlan(followUpPlanMap.get(followUpPlanNotice.getFollowUpPlanId()));
            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
            FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountMap.get(followUpPlanNotice.getFollowUpPlanId());
            if (followUpPlanNoticeCount != null) {
                followUpPlanNotice.setTotalPush(followUpPlanNoticeCount.getTotalPush());
                followUpPlanNotice.setPush(followUpPlanNoticeCount.getPush());

            } else {
                followUpPlanNotice.setTotalPush(0);
                followUpPlanNotice.setPush(0);
            }
        }
        user.setFollowUpPlanNoticeList(list);
        return RestResponse.ok(user);
    }

    /**
     * 查询患者全部计划
     *
     * @return
     */
    @GetMapping("/getPatientAllPlan")
    public RestResponse getPatientAllPlan(@RequestParam("patientId") Integer patientId) {

        List<FollowUpPlanNotice> list = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                .eq(FollowUpPlanNotice::getPatientUserId, patientId).orderByDesc(FollowUpPlanNotice::getNoticeTime)
        );

        //查询计划名称
        List<Integer> followUpPlanIds = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> followUpPlanContentIds = new ArrayList<>();
        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanIds.add(followUpPlanNotice.getFollowUpPlanId());
            userIds.add(followUpPlanNotice.getPatientUserId());
            followUpPlanContentIds.add(followUpPlanNotice.getFollowUpPlanContentId());
        }
        Map<Integer, FollowUpPlan> followUpPlanMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            List<FollowUpPlan> followUpPlans = (List<FollowUpPlan>) service.listByIds(followUpPlanIds);
            followUpPlanMap = followUpPlans.stream()
                    .collect(Collectors.toMap(FollowUpPlan::getId, t -> t));
        }
        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanContentIds)) {
            //查询推送内容
            List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);
            followUpPlanContentMap = followUpPlanContents.stream()
                    .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));
        }
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            //查询计划推送次数
            List<FollowUpPlanNoticeCount> followUpPlanNoticeCounts = followUpPlanNoticeCountService.list(new QueryWrapper<FollowUpPlanNoticeCount>()
                    .lambda().eq(FollowUpPlanNoticeCount::getPatientUserId, patientId)
                    .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds));
            if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
                followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
                        .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));


            }
        }


        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanNotice.setFollowUpPlan(followUpPlanMap.get(followUpPlanNotice.getFollowUpPlanId()));
            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
            FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountMap.get(followUpPlanNotice.getFollowUpPlanId());
            if (followUpPlanNoticeCount != null) {
                followUpPlanNotice.setTotalPush(followUpPlanNoticeCount.getTotalPush());
                followUpPlanNotice.setPush(followUpPlanNoticeCount.getPush());

            } else {
                followUpPlanNotice.setTotalPush(0);
                followUpPlanNotice.setPush(0);
            }
        }
        return RestResponse.ok(list);
    }

    /**
     * 查询推送计划节点详情
     *
     * @return
     */
    @GetMapping("/getFollowUpPlanContentDetail")
    public RestResponse getFollowUpPlanContentDetail(@RequestParam("followUpPlanContentId") Integer followUpPlanContentId) {
        FollowUpPlanContent followUpPlanContent = followUpPlanContentService.getById(followUpPlanContentId);
        Integer formId = followUpPlanContent.getFormId();
        Form byId = formService.getById(formId);
        followUpPlanContent.setForm(byId);
        return RestResponse.ok(followUpPlanContent);
    }

    /**
     * 修改推送计划节点详情
     *
     * @return
     */
    @PostMapping("/updateFollowUpPlanContent")
    public RestResponse updateFollowUpPlanContent(@RequestBody FollowUpPlanContent followUpPlanContent) {
        followUpPlanContentService.updateById(followUpPlanContent);
        return RestResponse.ok();
    }

    /**
     * 查询患者病例列表
     *
     * @return
     */
    @GetMapping("/getElectronicCaseList")
    public RestResponse getElectronicCaseList(@RequestParam("patientId") Integer patientId) {
        List<ElectronicCase> electronicCaseList = electronicCaseService.list(new QueryWrapper<ElectronicCase>().lambda()
                .eq(ElectronicCase::getPatientId, patientId).orderByDesc(ElectronicCase::getCreateTime));

        if (!CollectionUtils.isEmpty(electronicCaseList)) {
            //查询医生名字
            List<Integer> doctorIds = new ArrayList<>();

            for (ElectronicCase electronicCase : electronicCaseList) {
                doctorIds.add(electronicCase.getCreateUserId());
            }
            List<User> users = (List<User>) userService.listByIds(doctorIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));

            for (ElectronicCase electronicCase : electronicCaseList) {
                if (userMap.get(electronicCase.getCreateUserId()) != null) {
                    electronicCase.setCreateUserName(userMap.get(electronicCase.getCreateUserId()).getNickname());

                }
            }
        }

        return RestResponse.ok(electronicCaseList);
    }

    /**
     * 查询患者病例详情
     *
     * @return
     */
    @GetMapping("/getElectronicCaseDetail")
    public RestResponse getElectronicCaseDetail(@RequestParam("id") Integer id) {
        ElectronicCase electronicCase = electronicCaseService.getOne(new QueryWrapper<ElectronicCase>().lambda()
                .eq(ElectronicCase::getId, id));
        User user = userService.getById(electronicCase.getCreateUserId());
        electronicCase.setCreateUserName(user.getNickname());
        String followUpPlanIdList = electronicCase.getFollowUpPlanIdList();
        String[] split = followUpPlanIdList.split(",");
        List<String> followUpPlanIds = Arrays.asList(split);
        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            //查询随访计划
            List<FollowUpPlan> followUpPlans = (List<FollowUpPlan>) service.listByIds(followUpPlanIds);
            if (!CollectionUtils.isEmpty(followUpPlans)) {
                List<FollowUpPlanNoticeCount> followUpPlanNoticeCounts = followUpPlanNoticeCountService.list(new QueryWrapper<FollowUpPlanNoticeCount>()
                        .lambda().in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds)
                        .eq(FollowUpPlanNoticeCount::getPatientUserId, electronicCase.getPatientId()));
                if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
                    Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
                            .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));
                    for (FollowUpPlan followUpPlan : followUpPlans) {
                        followUpPlan.setFollowUpPlanNoticeCount(followUpPlanNoticeCountMap.get(followUpPlan.getId()));
                    }
                }


            } else {
                followUpPlans = new ArrayList<>();
            }
            electronicCase.setFollowUpPlans(followUpPlans);
        }

        //查询问诊单信息
        List<Inquiry> inquiryList = inquiryService.list(new QueryWrapper<Inquiry>().lambda()
                .eq(Inquiry::getElectronicCaseId, id));
        electronicCase.setInquirys(inquiryList);


        return RestResponse.ok(electronicCase);
    }

    @Override
    protected Class<FollowUpPlan> getEntityClass() {
        return FollowUpPlan.class;
    }
}
