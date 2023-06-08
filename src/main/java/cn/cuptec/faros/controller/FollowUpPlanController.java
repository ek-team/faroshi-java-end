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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.Collator;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Calendar.DAY_OF_MONTH;

/**
 * 随访计划管理
 */
@Slf4j
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
    private PlanUserService planUserService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private FormService formService;
    @Resource
    private ArticleService articleService;
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
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private UserGroupRelationUserService userGroupRelationUserService;
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private DoctorUserRemarkService doctorUserRemarkService;
    @Resource
    private UserDoctorRelationService userDoctorRelationService;

    /**
     * 查询随访计划模板
     */
    @GetMapping("/copyFollowUpPlan")
    public RestResponse copyFollowUpPlan(@RequestParam("followUpPlanId") Integer followUpPlanId) {

        User byId = userService.getById(SecurityUtils.getUser().getId());
        FollowUpPlan followUpPlan = service.getById(followUpPlanId);

        FollowUpPlan newFollowUpPlan = new FollowUpPlan();
        BeanUtils.copyProperties(followUpPlan, newFollowUpPlan, "id");
        newFollowUpPlan.setCreateUserId(SecurityUtils.getUser().getId());
        newFollowUpPlan.setCreateType(0);
        newFollowUpPlan.setFollowUpStatus(0);
        service.save(newFollowUpPlan);

        List<FollowUpPlanContent> followUpPlanContents = followUpPlanContentService.list(new QueryWrapper<FollowUpPlanContent>().lambda()
                .eq(FollowUpPlanContent::getFollowUpPlanId, followUpPlanId));
        List<FollowUpPlanContent> newFollowUpPlanContents = new ArrayList<>();
        if (!CollectionUtils.isEmpty(followUpPlanContents)) {
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
                FollowUpPlanContent newFollowUpPlanContent = new FollowUpPlanContent();
                BeanUtils.copyProperties(followUpPlanContent, newFollowUpPlanContent, "id");
                newFollowUpPlanContent.setFollowUpPlanId(newFollowUpPlan.getId());
                newFollowUpPlanContents.add(newFollowUpPlanContent);
            }


            followUpPlanContentService.saveBatch(newFollowUpPlanContents);
        }

        return RestResponse.ok();
    }


    /**
     * 添加随访计划
     *
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody FollowUpPlan followUpPlan) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Integer> userIds = followUpPlan.getFollowUpPlanPatientUsers();
        followUpPlan.setCreateUserId(SecurityUtils.getUser().getId());
        followUpPlan.setCreateTime(LocalDateTime.now());

        service.save(followUpPlan);
        String serviceDay = "0";//计划周期
        List<FollowUpPlanContent> followUpPlanContentList = followUpPlan.getFollowUpPlanContentList();
        if (!CollectionUtils.isEmpty(followUpPlanContentList)) {
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {

                followUpPlanContent.setFollowUpPlanId(followUpPlan.getId());
                Integer number = followUpPlanContent.getNumber();
                Integer numberType = followUpPlanContent.getNumberType();
                Integer hour = followUpPlanContent.getHour();
                LocalDateTime pushDay = LocalDateTime.now().plusMinutes(2);
                if (!numberType.equals(1)) {
                    LocalDate date = LocalDate.now();
                    if (numberType.equals(2)) {
                        serviceDay = number + "天";
                        date = date.plusDays(number);
                    } else if (numberType.equals(3)) {
                        serviceDay = number + "周";
                        date = date.plusWeeks(number);
                    } else if (numberType.equals(4)) {
                        serviceDay = number + "月";
                        date = date.plusMonths(number);
                    } else if (numberType.equals(5)) {
                        serviceDay = number + "年";
                        date = date.plusYears(number);
                    }
                    pushDay = date.atTime(hour, 0);
                }
                followUpPlanContent.setPushDay(pushDay);
                followUpPlanContent.setDay(formatter.format(pushDay));
            }
            followUpPlanContentService.saveBatch(followUpPlanContentList);
        }
        followUpPlanContentList.sort(Comparator.comparing(FollowUpPlanContent::getPushDay));

        LocalDateTime day1 = followUpPlanContentList.get(0).getPushDay();
        LocalDateTime day2 = followUpPlanContentList.get(followUpPlanContentList.size() - 1).getPushDay();
        if (followUpPlanContentList.size() == 1) {
            followUpPlan.setServiceDay(serviceDay);
        } else {
            Duration duration = Duration.between(day1, day2);
            followUpPlan.setServiceDay(Integer.parseInt(duration.toDays() + "") + "天");
        }


        if (!CollectionUtils.isEmpty(userIds)) {
            followUpPlan.setFollowUpStatus(1);
            userIds = userIds.stream().distinct().collect(Collectors.toList());
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
        addRecord(followUpPlanContentList, followUpPlan, userIds, formatter);

        return RestResponse.ok(followUpPlan);
    }

    public void addRecord(List<FollowUpPlanContent> followUpPlanContentList, FollowUpPlan followUpPlan, List<Integer> userIds, DateTimeFormatter formatter) {
        Map<Integer, List<TbTrainUser>> trainUserMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getXtUserId, userIds));
            if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                trainUserMap = tbTrainUsers.stream()
                        .collect(Collectors.groupingBy(TbTrainUser::getXtUserId));


            }
        }
        //添加随访计划通知记录
        List<FollowUpPlanNotice> followUpPlanNoticeList = new ArrayList<>();
        List<FollowUpPlanNoticeCount> followUpPlanNoticeCountList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(followUpPlanContentList) && !CollectionUtils.isEmpty(userIds)) {
            Integer teamId = followUpPlan.getTeamId();
            Map<Integer, ChatUser> chatUserMap = new HashMap<>();
            if (teamId != null) {
                List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                        .eq(ChatUser::getTeamId, teamId)
                        .in(ChatUser::getTargetUid, userIds));
                if (!CollectionUtils.isEmpty(chatUsers)) {
                    chatUserMap = chatUsers.stream()
                            .collect(Collectors.toMap(ChatUser::getTargetUid, t -> t));
                }
            }
            List<Integer> countUserIds = new ArrayList<>();
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {

                for (Integer userId : userIds) {
                    FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();

                    List<TbTrainUser> tbTrainUsers = trainUserMap.get(userId);
                    if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                        TbTrainUser tbTrainUser = tbTrainUsers.get(0);
                        Date dateTime = tbTrainUser.getDate();
                        if (dateTime != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateTime);
                            LocalDate date = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(DAY_OF_MONTH)).plusMonths(1);
                            LocalDateTime localDateTime = LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
                            Integer number = followUpPlanContent.getNumber();
                            Integer numberType = followUpPlanContent.getNumberType();
                            Integer hour = followUpPlanContent.getHour();
                            LocalDateTime pushDay = localDateTime.plusMinutes(2);
//                            if (!numberType.equals(1)) {
//                                if (numberType.equals(2)) {
//                                    date = date.plusDays(number);
//                                } else if (numberType.equals(3)) {
//                                    date = date.plusWeeks(number);
//                                } else if (numberType.equals(4)) {
//                                    date = date.plusMonths(number);
//                                } else if (numberType.equals(5)) {
//                                    date = date.plusYears(number);
//                                }
//                                pushDay = date.atTime(hour, 0);
//                            }
                            //随访计划通知时间
                            if (pushDay.isAfter(LocalDateTime.now())) {
                                followUpPlanNotice.setNoticeTime(pushDay);
                            }

                        }

                    }

                    ChatUser chatUser = chatUserMap.get(userId);
                    if (chatUser != null) {
                        followUpPlanNotice.setChatUserId(chatUser.getId());
                    }

                    followUpPlanNotice.setFollowUpPlanId(followUpPlan.getId());
                    followUpPlanNotice.setPatientUserId(userId);

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
                        followUpPlanNoticeCountService.remove(new QueryWrapper<FollowUpPlanNoticeCount>()
                                .lambda().eq(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanNotice.getFollowUpPlanId())
                                .eq(FollowUpPlanNoticeCount::getPatientUserId, userId));
                    }

                }

            }
            followUpPlanNoticeCountService.saveBatch(followUpPlanNoticeCountList);
            if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                followUpPlanNoticeService.saveBatch(followUpPlanNoticeList);
                for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                    LocalDateTime noticeTime = followUpPlanNotice.getNoticeTime();
                    if (noticeTime != null) {
                        LocalDateTime thisNow = LocalDateTime.now();
                        if (noticeTime.isBefore(thisNow)) {
                            noticeTime = noticeTime.plusMinutes(3);
                        }
                        Duration duration = Duration.between(thisNow, noticeTime);
                        long hours = duration.toMinutes();//分钟
                        log.info("添加随访计划redis时间" + hours + "========" + noticeTime + "====" + thisNow);
                        String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                        redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), hours, TimeUnit.MINUTES);//设置过期时间

                    }

                }

            }


        }

    }

    public static void main(String[] args) {
        Date date = new Date();
        // Date 转 LocalDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(DAY_OF_MONTH));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        System.out.println(localDateTime);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        followUpPlanContentService.remove(new QueryWrapper<FollowUpPlanContent>().lambda()
                .eq(FollowUpPlanContent::getFollowUpPlanId, followUpPlan.getId()));
        List<FollowUpPlanContent> followUpPlanContents = followUpPlan.getFollowUpPlanContentList();
        String serviceDay = "0";//计划周期
        if (!CollectionUtils.isEmpty(followUpPlanContents)) {
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {

                followUpPlanContent.setFollowUpPlanId(followUpPlan.getId());
                Integer number = followUpPlanContent.getNumber();
                Integer numberType = followUpPlanContent.getNumberType();
                Integer hour = followUpPlanContent.getHour();
                LocalDateTime pushDay = LocalDateTime.now().plusMinutes(2);
                if (!numberType.equals(1)) {
                    LocalDate date = LocalDate.now();
                    if (numberType.equals(2)) {
                        serviceDay = number + "天";
                        date = date.plusDays(number);
                    } else if (numberType.equals(3)) {
                        serviceDay = number + "周";
                        date = date.plusWeeks(number);
                    } else if (numberType.equals(4)) {
                        serviceDay = number + "月";
                        date = date.plusMonths(number);
                    } else if (numberType.equals(5)) {
                        serviceDay = number + "年";
                        date = date.plusYears(number);
                    }
                    pushDay = date.atTime(hour, 0);
                }
                followUpPlanContent.setPushDay(pushDay);
                followUpPlanContent.setDay(formatter.format(pushDay));
            }
            followUpPlanContentService.saveBatch(followUpPlanContents);
        }
        followUpPlanContents.sort(Comparator.comparing(FollowUpPlanContent::getPushDay));

        LocalDateTime day1 = followUpPlanContents.get(0).getPushDay();
        LocalDateTime day2 = followUpPlanContents.get(followUpPlanContents.size() - 1).getPushDay();
        if (followUpPlanContents.size() == 1) {
            followUpPlan.setServiceDay(serviceDay);
        } else {
            Duration duration = Duration.between(day1, day2);
            followUpPlan.setServiceDay(Integer.parseInt(duration.toDays() + "") + "天");
        }


        List<Integer> userIds = followUpPlan.getFollowUpPlanPatientUsers();
        List<FollowUpPlanPatientUser> followUpPlanPatientUserList = followUpPlanPatientUserService.list(new QueryWrapper<FollowUpPlanPatientUser>().lambda()
                .eq(FollowUpPlanPatientUser::getFollowUpPlanId, followUpPlan.getId()));
        if (CollectionUtils.isEmpty(followUpPlanPatientUserList) && !CollectionUtils.isEmpty(userIds)) {
            //全部添加 原先的患者是空
            userIds = userIds.stream().distinct().collect(Collectors.toList());
            followUpPlanPatientUserList = new ArrayList<>();
            for (Integer userId : userIds) {
                FollowUpPlanPatientUser followUpPlanPatientUser = new FollowUpPlanPatientUser();
                followUpPlanPatientUser.setFollowUpPlanId(followUpPlan.getId());
                followUpPlanPatientUser.setUserId(userId);
                followUpPlanPatientUserList.add(followUpPlanPatientUser);

            }
            followUpPlanPatientUserService.saveBatch(followUpPlanPatientUserList);
            followUpPlan.setPatientUserCount(followUpPlanPatientUserList.size());
            addRecord(followUpPlanContents, followUpPlan, userIds, formatter);
            followUpPlan.setFollowUpStatus(1);
            service.updateById(followUpPlan);

            return RestResponse.ok(followUpPlan);
        }
        if (CollectionUtils.isEmpty(userIds)) {
            //全部删除患者
            followUpPlanPatientUserService.remove(new QueryWrapper<FollowUpPlanPatientUser>().lambda()
                    .eq(FollowUpPlanPatientUser::getFollowUpPlanId, followUpPlan.getId()));
            followUpPlan.setPatientUserCount(0);
            service.updateById(followUpPlan);
            //修改所有通知记录为已取消
            List<FollowUpPlanNotice> followUpPlanNoticeList = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                    .eq(FollowUpPlanNotice::getFollowUpPlanId, followUpPlan.getId()));
            if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                    followUpPlanNotice.setStatus(2);
                }
                followUpPlanNoticeService.updateBatchById(followUpPlanNoticeList);
            }
            return RestResponse.ok(followUpPlan);
        }

        if (!CollectionUtils.isEmpty(userIds) && !CollectionUtils.isEmpty(followUpPlanPatientUserList)) {
            List<Integer> oldUserIds = followUpPlanPatientUserList.stream().map(FollowUpPlanPatientUser::getUserId)
                    .collect(Collectors.toList());
            List<Integer> addPatientUserIds = new ArrayList<>();
            List<Integer> removePatientUserIds = new ArrayList<>();
            for (Integer userId : userIds) {
                if (!oldUserIds.contains(userId)) {
                    addPatientUserIds.add(userId);
                }
            }
            followUpPlan.setFollowUpStatus(1);
            followUpPlan.setPatientUserCount(userIds.size());
            for (Integer userId : oldUserIds) {
                if (!userIds.contains(userId)) {
                    removePatientUserIds.add(userId);
                }
            }
            if (!CollectionUtils.isEmpty(addPatientUserIds)) {
                for (Integer userId : addPatientUserIds) {
                    FollowUpPlanPatientUser followUpPlanPatientUser = new FollowUpPlanPatientUser();
                    followUpPlanPatientUser.setFollowUpPlanId(followUpPlan.getId());
                    followUpPlanPatientUser.setUserId(userId);
                    followUpPlanPatientUserList.add(followUpPlanPatientUser);

                }
                followUpPlanPatientUserService.saveBatch(followUpPlanPatientUserList);
                addRecord(followUpPlanContents, followUpPlan, addPatientUserIds, formatter);
            }
            if (!CollectionUtils.isEmpty(removePatientUserIds)) {
                followUpPlanPatientUserService.remove(new QueryWrapper<FollowUpPlanPatientUser>().lambda()
                        .eq(FollowUpPlanPatientUser::getFollowUpPlanId, followUpPlan.getId())
                        .in(FollowUpPlanPatientUser::getUserId, removePatientUserIds));

                //修改所有通知记录为已取消
                List<FollowUpPlanNotice> followUpPlanNoticeList = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                        .eq(FollowUpPlanNotice::getFollowUpPlanId, followUpPlan.getId())
                        .in(FollowUpPlanNotice::getPatientUserId, removePatientUserIds));
                if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                    for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                        followUpPlanNotice.setStatus(2);
                    }
                    followUpPlanNoticeService.updateBatchById(followUpPlanNoticeList);
                }
            }

        }


        service.updateById(followUpPlan);

        return RestResponse.ok(followUpPlan);
    }

    /**
     * 列表查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageScoped(@RequestParam(value = "createType", required = false) Integer createType) {
        Page<FollowUpPlan> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (createType != null) {
            queryWrapper.eq("create_type", createType);
        } else {
            queryWrapper.eq("create_user_id", SecurityUtils.getUser().getId());
            queryWrapper.eq("create_type", 0);
        }

        queryWrapper.orderByDesc("create_time");
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
            //查询手术名称
            List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getXtUserId, userIds));
            Map<Integer, List<TbTrainUser>> map = new HashMap<>();
            if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                map = tbTrainUsers.stream()
                        .collect(Collectors.groupingBy(TbTrainUser::getXtUserId));

            }

            List<User> users = (List<User>) userService.listByIds(userIds);
            if (!CollectionUtils.isEmpty(users)) {
                for (User user : users) {
                    if (!StringUtils.isEmpty(user.getPatientName())) {
                        user.setNickname(user.getPatientName());

                    }
                    List<TbTrainUser> tbTrainUsers1 = map.get(user.getId());
                    if (!CollectionUtils.isEmpty(tbTrainUsers1)) {
                        TbTrainUser tbTrainUser = tbTrainUsers1.get(0);
                        user.setDiagnosis(tbTrainUser.getDiagnosis());
                    }
                }
            }

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
            List<Integer> formIds = new ArrayList<>();
            List<Integer> articleIds = new ArrayList<>();
            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                if (followUpPlanContent.getFormId() != null) {
                    formIds.add(followUpPlanContent.getFormId());

                }
                if (followUpPlanContent.getArticleId() != null) {
                    articleIds.add(followUpPlanContent.getArticleId());

                }
            }
            Map<Integer, Form> formMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(formIds)) {
                List<Form> forms = (List<Form>) formService.listByIds(formIds);
                formMap = forms.stream()
                        .collect(Collectors.toMap(Form::getId, t -> t));
            }
            //查询文章信息
            Map<Integer, Article> articleMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(articleIds)) {
                List<Article> articles = (List<Article>) articleService.listByIds(articleIds);
                articleMap = articles.stream()
                        .collect(Collectors.toMap(Article::getId, t -> t));
            }

            for (FollowUpPlanContent followUpPlanContent : followUpPlanContentList) {
                followUpPlanContent.setForm(formMap.get(followUpPlanContent.getFormId()));
                followUpPlanContent.setArticle(articleMap.get(followUpPlanContent.getArticleId()));
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

        List<UserDoctorRelation> list = userDoctorRelationService.list(new QueryWrapper<UserDoctorRelation>().lambda()
                .eq(UserDoctorRelation::getDoctorId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<Integer> userIds = list.stream().map(UserDoctorRelation::getUserId)
                .collect(Collectors.toList());
        //查询手术名称
        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getXtUserId, userIds));
        Map<Integer, List<TbTrainUser>> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(tbTrainUsers)) {
            map = tbTrainUsers.stream()
                    .collect(Collectors.groupingBy(TbTrainUser::getXtUserId));

        }

        List<User> users = (List<User>) userService.listByIds(userIds);
        if (!CollectionUtils.isEmpty(users)) {
            for (User user : users) {
                if (!StringUtils.isEmpty(user.getPatientName())) {
                    user.setNickname(user.getPatientName());

                }
                List<TbTrainUser> tbTrainUsers1 = map.get(user.getId());
                if (!CollectionUtils.isEmpty(tbTrainUsers1)) {
                    TbTrainUser tbTrainUser = tbTrainUsers1.get(0);
                    user.setDiagnosis(tbTrainUser.getDiagnosis());
                }
            }


        }
        return RestResponse.ok(users);
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
                .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds)
                .in(FollowUpPlanNoticeCount::getPatientUserId, userIds));
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {
            for (FollowUpPlanNoticeCount followUpPlanNoticeCount : followUpPlanNoticeCounts) {
                FollowUpPlanNoticeCount followUpPlanNoticeCount1 = followUpPlanNoticeCountMap.get(followUpPlanNoticeCount.getPatientUserId());
                if (followUpPlanNoticeCount1 == null) {
                    followUpPlanNoticeCountMap.put(followUpPlanNoticeCount.getPatientUserId(), followUpPlanNoticeCount);
                }
            }
//            followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
//                    .collect(Collectors.toMap(FollowUpPlanNoticeCount::getPatientUserId, t -> t));


        }

        for (FollowUpPlanNotice followUpPlanNotice : list) {
            followUpPlanNotice.setFollowUpPlan(followUpPlanMap.get(followUpPlanNotice.getFollowUpPlanId()));
            followUpPlanNotice.setUser(userMap.get(followUpPlanNotice.getPatientUserId()));
            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
            FollowUpPlanNoticeCount followUpPlanNoticeCount = followUpPlanNoticeCountMap.get(followUpPlanNotice.getPatientUserId());
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
                .in(FollowUpPlanNoticeCount::getFollowUpPlanId, followUpPlanIds)
                .in(FollowUpPlanNoticeCount::getPatientUserId, userIds));
        Map<Integer, FollowUpPlanNoticeCount> followUpPlanNoticeCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(followUpPlanNoticeCounts)) {

            for (FollowUpPlanNoticeCount followUpPlanNoticeCount : followUpPlanNoticeCounts) {
                FollowUpPlanNoticeCount followUpPlanNoticeCount1 = followUpPlanNoticeCountMap.get(followUpPlanNoticeCount.getPatientUserId());
                if (followUpPlanNoticeCount1 == null) {
                    followUpPlanNoticeCountMap.put(followUpPlanNoticeCount.getPatientUserId(), followUpPlanNoticeCount);
                }
            }
//            followUpPlanNoticeCountMap = followUpPlanNoticeCounts.stream()
//                    .collect(Collectors.toMap(FollowUpPlanNoticeCount::getFollowUpPlanId, t -> t));


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

    private static Map<String, String> getAge(String idCard) {
        String birthday = "";
        String age = "";
        Integer sexCode = 0;

        int year = Calendar.getInstance().get(Calendar.YEAR);
        char[] number = idCard.toCharArray();
        boolean flag = true;

        if (number.length == 15) {
            for (int x = 0; x < number.length; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        } else if (number.length == 18) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        }

        if (flag && idCard.length() == 15) {
            birthday = "19" + idCard.substring(6, 8) + "-"
                    + idCard.substring(8, 10) + "-"
                    + idCard.substring(10, 12);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 3, idCard.length())) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt("19" + idCard.substring(6, 8))) + "";
        } else if (flag && idCard.length() == 18) {
            birthday = idCard.substring(6, 10) + "-"
                    + idCard.substring(10, 12) + "-"
                    + idCard.substring(12, 14);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 4, idCard.length() - 1)) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt(idCard.substring(6, 10))) + "";
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("birthday", birthday);
        map.put("age", age);
        map.put("sexCode", sexCode + "");
        return map;
    }

    /**
     * 医生端查询患者详情
     *
     * @return
     */
    @GetMapping("/getPatientDetail")
    public RestResponse getPatientDetail(@RequestParam("patientId") Integer patientId) {
        User user = userService.getById(patientId);
        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getXtUserId, user.getId()));
        if (!CollectionUtils.isEmpty(tbTrainUsers)) {
            TbTrainUser tbTrainUser = tbTrainUsers.get(0);
            user.setTbTrainUser(tbTrainUser);
        }
        if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(user.getPatientId())) {
            PatientUser patientUser = patientUserService.getById(user.getPatientId());
            if (patientUser != null) {
                user.setIdCard(patientUser.getIdCard());
                user.setPatientName(patientUser.getName());

                if (patientUser.getCardType() != null && patientUser.getCardType().equals(2)) {
                    user.setAge(patientUser.getAge());

                    user.setBirthday(patientUser.getAge());
                    user.setSexCode(patientUser.getSex());//1-男0-女
                }
            }

        }
        //生成年龄性别
        String idCard = user.getIdCard();
        if (!StringUtils.isEmpty(idCard) && StringUtils.isEmpty(user.getAge())) {
            Map<String, String> map = getAge(idCard);
            user.setAge(map.get("age"));

            user.setBirthday(map.get("birthday"));
            user.setSexCode(map.get("sexCode"));//1-男0-女

        }
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
        //查询用户所在的分组
        user.setUserGroupName("暂无分组");
        List<UserGroup> userGroupList = userGroupService.list(new QueryWrapper<UserGroup>().lambda()
                .eq(UserGroup::getCreateUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(userGroupList)) {
            List<Integer> userGroupIds = userGroupList.stream().map(UserGroup::getId)
                    .collect(Collectors.toList());
            List<UserGroupRelationUser> userGroupRelationUsers = userGroupRelationUserService.list(new QueryWrapper<UserGroupRelationUser>().lambda()
                    .eq(UserGroupRelationUser::getUserId, patientId)
                    .in(UserGroupRelationUser::getUserGroupId, userGroupIds));
            if (!CollectionUtils.isEmpty(userGroupRelationUsers)) {
                user.setUserGroupName(userGroupService.getById(userGroupRelationUsers.get(0).getUserGroupId()).getName());
                user.setUserGroupId(userGroupService.getById(userGroupRelationUsers.get(0).getUserGroupId()).getId() + "");

            }
        }
        //查询医生对患者的备注
        DoctorUserRemark doctorUserRemark = doctorUserRemarkService.getOne(new QueryWrapper<DoctorUserRemark>().lambda()
                .eq(DoctorUserRemark::getDoctorId, SecurityUtils.getUser().getId())
                .eq(DoctorUserRemark::getUserId, patientId));
        if (doctorUserRemark != null) {
            user.setRemark(doctorUserRemark.getRemark());
        }
        return RestResponse.ok(user);
    }

    /**
     * 医生备注
     *
     * @return
     */
    @GetMapping("/doctorRemark")
    public RestResponse doctorRemark(@RequestParam("userId") Integer userId, @RequestParam("remark") String remark) {
        //查询医生对患者的备注
        DoctorUserRemark doctorUserRemark = doctorUserRemarkService.getOne(new QueryWrapper<DoctorUserRemark>().lambda()
                .eq(DoctorUserRemark::getDoctorId, SecurityUtils.getUser().getId())
                .eq(DoctorUserRemark::getUserId, userId));
        if (doctorUserRemark == null) {
            doctorUserRemark = new DoctorUserRemark();
            doctorUserRemark.setUserId(userId);
        }
        doctorUserRemark.setRemark(remark);
        doctorUserRemark.setDoctorId(SecurityUtils.getUser().getId());
        doctorUserRemarkService.saveOrUpdate(doctorUserRemark);
        return RestResponse.ok();
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
        if (!StringUtils.isEmpty(followUpPlanIdList)) {
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
        } else {
            electronicCase.setFollowUpPlans(new ArrayList<>());
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
