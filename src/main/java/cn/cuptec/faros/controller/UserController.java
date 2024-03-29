package cn.cuptec.faros.controller;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.ChangeDoctorDTO;
import cn.cuptec.faros.dto.DoctorQrCodeDto;
import cn.cuptec.faros.dto.UserPwdDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.IdCardUtil;
import cn.cuptec.faros.util.UploadFileUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends AbstractBaseController<UserService, User> {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private final OssProperties ossProperties;
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private UserRoleService userRoleService;

    @Resource
    private HospitalInfoService hospitalInfoService;

    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private UserFollowDoctorService userFollowDoctorService;
    @Resource
    private UserDoctorRelationService userDoctorRelationService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private PatientRelationTeamService patientRelationTeamService;
    private final Url urlData;
    @Resource
    private PatientUserBindDoctorService patientUserBindDoctorService;

    @GetMapping("/saveLoginLog")
    public RestResponse saveLoginLog(@RequestParam("data") String data) {
        LoginLog loginLog = new LoginLog();
        loginLog.setData(data);
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogService.save(loginLog);
        return RestResponse.ok();
    }


    @GetMapping("/copyUser")
    public RestResponse copyUser(@RequestParam("id") Integer id) {


        UserOrder userOrder = userOrdertService.getById(id);
        List<ChatUser> list = chatUserService.list(new QueryWrapper<ChatUser>().lambda().like(ChatUser::getUserIds, SecurityUtils.getUser().getId())
                .eq(ChatUser::getId, userOrder.getChatUserId()));
        if (!CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        PatientUser targetPatientUser = patientUserService.getById(userOrder.getPatientUserId());
        User myUSer = service.getById(SecurityUtils.getUser().getId());
        PatientUser myPatientUser = new PatientUser();
        BeanUtils.copyProperties(targetPatientUser, myPatientUser, "id");
        myPatientUser.setUserId(myUSer.getId());
        patientUserService.save(myPatientUser);

        Integer chatUserId = userOrder.getChatUserId();
        ChatUser chatUser = chatUserService.getById(chatUserId);
        chatUser.setUserIds(chatUser.getUserIds() + "," + SecurityUtils.getUser().getId());
        chatUser.setPatientId(myPatientUser.getId());
        chatUserService.updateById(chatUser);


        return RestResponse.ok();
    }

    /**
     * 患者添加医生好友
     */
    @GetMapping("/addPatientDoctorGroup")
    public RestResponse addPatientDoctorGroup(@RequestParam("doctorId") String doctorId) {


        return RestResponse.ok();
    }

    /**
     * 患者添加医生好友
     */
    @GetMapping("/addPatientUserByIdCard")
    public RestResponse addPatientUserByIdCard(@RequestParam("idCard") String idCard) {
        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, idCard));

        List<PatientUser> list = patientUserService.list(new QueryWrapper<PatientUser>().lambda()
                .eq(PatientUser::getUserId, SecurityUtils.getUser().getId())
                .eq(PatientUser::getIdCard, idCard));
        if (CollectionUtils.isEmpty(list)) {
            PatientUser patientUser = new PatientUser();
            patientUser.setUserId(SecurityUtils.getUser().getId());
            patientUser.setIdCard(idCard);
            patientUser.setCardType(1);
            if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                TbTrainUser tbTrainUser = tbTrainUsers.get(0);

                patientUser.setName(tbTrainUser.getName());
                patientUser.setPhone(tbTrainUser.getTelePhone());
            }
            patientUserService.save(patientUser);
        }
        return RestResponse.ok();
    }

    /**
     * 获取医生个人二维码 和医生所在团队二维码 患者扫码 添加
     */
    @GetMapping("/getDoctorQrCode")
    public RestResponse getDoctorQrCode() {
        List<DoctorQrCodeDto> doctorQrCodeDtoList = new ArrayList<>();


        User user = service.getById(SecurityUtils.getUser().getId());
        if (!StringUtils.isEmpty(user.getQrCode())) {
            DoctorQrCodeDto doctorQrCodeDto = new DoctorQrCodeDto();

            doctorQrCodeDto.setQrCode(user.getQrCode());
            doctorQrCodeDto.setName(user.getNickname());
            doctorQrCodeDtoList.add(doctorQrCodeDto);
        } else {
            DoctorQrCodeDto doctorQrCodeDto = new DoctorQrCodeDto();

            //生成一个图片返回
            String url = urlData.getUrl() + "index.html#/newPlatform/addFriends?doctorId=" + SecurityUtils.getUser().getId();
            BufferedImage png = null;
            try {
                png = QrCodeUtil.doctorImage(ServletUtils.getResponse().getOutputStream(), "", url, 300);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String name = "";
            //转换上传到oss
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = null;
            try {
                imOut = ImageIO.createImageOutputStream(bs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ImageIO.write(png, "png", imOut);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
            try {
                OSS ossClient = UploadFileUtils.getOssClient(ossProperties);
                Random random = new Random();
                name = random.nextInt(10000) + System.currentTimeMillis() + "_YES.png";
                // 上传文件
                PutObjectResult putResult = ossClient.putObject(ossProperties.getBucket(), "poster/" + name, inputStream);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/avatar/1673835893578_b9f1ad25.png
            String resultStr = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/" + "poster/" + name;
            user.setQrCode(resultStr);
            service.updateById(user);

            doctorQrCodeDto.setQrCode(user.getQrCode());
            doctorQrCodeDto.setName(user.getNickname());
            doctorQrCodeDtoList.add(doctorQrCodeDto);
        }
        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            List<Integer> teamIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
            if (!CollectionUtils.isEmpty(doctorTeams)) {
                List<Integer> hospitalIds = doctorTeams.stream().map(DoctorTeam::getHospitalId)
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(hospitalIds)) {
                    List<HospitalInfo> hospitalInfos = (List<HospitalInfo>) hospitalInfoService.listByIds(hospitalIds);
                    Map<Integer, HospitalInfo> hospitalInfoMap = hospitalInfos.stream()
                            .collect(Collectors.toMap(HospitalInfo::getId, t -> t));
                    for (DoctorTeam doctorTeam : doctorTeams) {
                        HospitalInfo hospitalInfo = hospitalInfoMap.get(doctorTeam.getHospitalId());
                        if (hospitalInfo != null) {
                            doctorTeam.setHospitalName(hospitalInfo.getName());

                        }
                    }
                }
                for (DoctorTeam doctorTeam : doctorTeams) {
                    DoctorQrCodeDto doctorQrCodeDto = new DoctorQrCodeDto();
                    doctorQrCodeDto.setQrCode(doctorTeam.getQrCode());
                    doctorQrCodeDto.setName(doctorTeam.getName());
                    doctorQrCodeDto.setHospitalName(doctorTeam.getHospitalName());
                    doctorQrCodeDtoList.add(doctorQrCodeDto);
                }
            }

        }
        return RestResponse.ok(doctorQrCodeDtoList);

    }

    /**
     * 初始化小程序openid
     *
     * @return
     */
    @GetMapping("/initMaOpenId")
    public RestResponse initMaOpenId(@RequestParam("code") String code) {
        WxMaUserService wxMaUserService = WxMaConfiguration.getWxMaService().getUserService();
        WxMaJscode2SessionResult sessionInfo = null;
        try {
            sessionInfo = wxMaUserService.getSessionInfo(code);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        log.info("小程序登录sessionInfo:{}" + sessionInfo.toString());
        User user = service.getBaseMapper().getUnionIdIsExist(sessionInfo.getUnionid());
        if (user == null) {
            user = new User();
            user.setPhone(sessionInfo.getOpenid());
            user.setMaOpenId(sessionInfo.getOpenid());
            user.setUnionId(sessionInfo.getUnionid());
            user.setIsSubscribe(false);
            user.setLockFlag(CommonConstants.STATUS_NORMAL);
            service.save(user);
        }

        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(user.getMaOpenId())) {
            //若小程序openId为空，更新
            user.setMaOpenId(sessionInfo.getOpenid());
            service.updateById(user);
        }
        return RestResponse.ok();
    }


    /**
     * 添加用户就诊人
     *
     * @param patientUser
     * @return
     */
    @PostMapping("/savePatientUser")
    public RestResponse savePatientUser(@RequestBody PatientUser patientUser) {
        patientUser.setUserId(SecurityUtils.getUser().getId());
        if (!StringUtils.isEmpty(patientUser.getIdCard()) && patientUser.getCardType() != null && patientUser.getCardType().equals(1)) {
            boolean validCard = IdCardUtil.isValidCard(patientUser.getIdCard());
            if (!validCard) {
                return RestResponse.failed("身份证格式错误");
            }
        }

        patientUserService.save(patientUser);
        return RestResponse.ok();
    }

    /**
     * 修改用户就诊人
     *
     * @param patientUser
     * @return
     */
    @PostMapping("/updatePatientUser")
    public RestResponse updatePatientUser(@RequestBody PatientUser patientUser) {
        if (!StringUtils.isEmpty(patientUser.getIdCard()) && patientUser.getCardType() != null && patientUser.getCardType().equals(1)) {
            boolean validCard = IdCardUtil.isValidCard(patientUser.getIdCard());
            if (!validCard) {
                return RestResponse.failed("身份证格式错误");
            }
        }

        patientUserService.updateById(patientUser);
        return RestResponse.ok();
    }

    /**
     * 用户就诊人查询
     *
     * @return
     */
    @GetMapping("/listPatientUser")
    public RestResponse listPatientUser() {
        List<PatientUser> list = patientUserService.list(new QueryWrapper<PatientUser>().lambda().eq(PatientUser::getUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(list)) {
            for (PatientUser patientUser : list) {
                if (!StringUtils.isEmpty(patientUser.getIdCard()) && StringUtils.isEmpty(patientUser.getAge())) {
                    Map<String, String> map = getAge(patientUser.getIdCard());
                    patientUser.setAge(map.get("birthday"));
                }
                if (!StringUtils.isEmpty(patientUser.getIdCard()) && StringUtils.isEmpty(patientUser.getSex())) {
                    Map<String, String> map = getAge(patientUser.getIdCard());

                    patientUser.setSex(map.get("sexCode"));//1-男0-女
                }

            }
        }


        return RestResponse.ok(list);
    }

    /**
     * 通过身份证号码获取出生日期、性别、年龄
     *
     * @return 返回的出生日期格式：1990-01-01   性别格式：F-女，M-男
     */
    @GetMapping("/calculate")
    public RestResponse calculate(@RequestParam("idCard") String idCard) {

        return RestResponse.ok(getAge(idCard));
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
     * 删除用户就诊人
     *
     * @return
     */
    @GetMapping("/deletePatientUser")
    public RestResponse deletePatientUser(@RequestParam("id") Integer id) {
        return RestResponse.ok(patientUserService.removeById(id));
    }

    /**
     * 查询用户就诊人详细
     *
     * @return
     */
    @GetMapping("/getPatientUserById")
    public RestResponse getPatientUserById(@RequestParam("id") Integer id) {
        return RestResponse.ok(patientUserService.getById(id));
    }

    /**
     * 获取个人信息
     *
     * @return
     */
    @GetMapping("/info")
    public RestResponse<User> user_me() {
        User user = service.selectUserVoById(SecurityUtils.getUser().getId());
        if (!StringUtils.isEmpty(user.getPatientId())) {
            PatientUser patientUser = patientUserService.getById(user.getPatientId());
            if (patientUser != null) {
                user.setIdCard(patientUser.getIdCard());
                user.setPatientName(patientUser.getName());
            }

        }
        //生成年龄性别
        String idCard = user.getIdCard();
        if (!StringUtils.isEmpty(idCard)) {
            Map<String, String> map = getAge(idCard);
            user.setAge(map.get("age"));

            user.setBirthday(map.get("birthday"));
            user.setSexCode(map.get("sexCode"));//1-男0-女

        }
        //查询医院信息
        List<User> users = new ArrayList<>();
        users.add(user);
        hospitalInfoService.getHospitalByUser(users);
        return RestResponse.ok(users.get(0));
    }

    /**
     * 判断是否关注公众号
     *
     * @return
     */
    @GetMapping("/isSubscribe")
    public RestResponse<User> isSubscribe(@RequestParam(value = "macAdd", required = false) String macAdd) {
        User user = service.selectUserVoById(SecurityUtils.getUser().getId());
        User updateUser = new User();
        user.setId(SecurityUtils.getUser().getId());
        user.setMacAdd(macAdd);
        service.updateById(updateUser);
        return RestResponse.ok(user);
    }

    @GetMapping("/infoByUid/{uid}")
    public RestResponse<User> user_me(@PathVariable int uid) {
        User user = service.getById(uid);
        if (!StringUtils.isEmpty(user.getPatientId())) {
            PatientUser patientUser = patientUserService.getById(user.getPatientId());
            if (patientUser != null) {
                user.setIdCard(patientUser.getIdCard());
                user.setPatientName(patientUser.getName());
            }

        }
        String idCard = user.getIdCard();
        if (!StringUtils.isEmpty(idCard)) {
            Map<String, String> map = getAge(idCard);
            user.setAge(map.get("age"));

            user.setBirthday(map.get("birthday"));
            user.setSexCode(map.get("sexCode"));//1-男0-女

        }
        return RestResponse.ok(user);
    }

    @GetMapping("/getByUid")
    public RestResponse<User> getByUid(@RequestParam(value = "uid", required = false) Integer uid) {
        if (uid == null && SecurityUtils.getUser() != null) {
            uid = SecurityUtils.getUser().getId();
        }
        //生成年龄性别
        User user = service.getById(uid);
        if (!StringUtils.isEmpty(user.getPatientId())) {
            PatientUser patientUser = patientUserService.getById(user.getPatientId());
            if (patientUser != null) {
                user.setIdCard(patientUser.getIdCard());
                user.setPatientName(patientUser.getName());
            }

        }
        String idCard = user.getIdCard();
        if (!StringUtils.isEmpty(idCard)) {
            Map<String, String> map = getAge(idCard);
            user.setAge(map.get("age"));

            user.setBirthday(map.get("birthday"));
            user.setSexCode(map.get("sexCode"));//1-男0-女

        }
        return RestResponse.ok(user);
    }

    /**
     * 更新个人信息
     *
     * @param user
     * @return
     */
    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody @Valid User user) {
        if (!StringUtils.isEmpty(user.getPhone())) {
            User one = service.getOne(new QueryWrapper<User>().lambda().eq(User::getPhone, user.getPhone())
            );
            if (one != null && !one.getId().equals(user.getId())) {
                return RestResponse.failed("手机号已被占用");
            }

        }
        if (!StringUtils.isEmpty(user.getNickname())) {
            if (user.getNickname().equals("微信用户")) {
                User user1 = new User();
                BeanUtils.copyProperties(user, user1, "nickname", "avatar");
                user = user1;
            }
        }
        if (user.getId() == null) {
            user.setId(SecurityUtils.getUser().getId());

        }
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(ENCODER.encode(user.getPassword()));
        }
        return service.updateById(user) ? RestResponse.ok() : RestResponse.failed();
    }

    public static void main(String[] args) {
        System.out.println(ENCODER.encode("123456"));
    }

    @PutMapping
    public RestResponse update(@RequestBody @Valid User user) {
        user.setId(SecurityUtils.getUser().getId());
        return service.updateById(user) ? RestResponse.ok() : RestResponse.failed();
    }

    @SysLog("添加用户")
    @PostMapping("/manage/add")
    public RestResponse<User> user(@RequestBody User user) {
        service.save(user);
        return RestResponse.ok();
    }

    @SysLog("医生账号注册")
    @PostMapping("/register")
    public RestResponse<User> register(@RequestBody User user) {
        Role role = new Role();
        role.setId(20);
        Role[] roles = new Role[]{role};
        user.setRoles(roles);
        user.setLockFlag("0");
        service.save(user);
        return RestResponse.ok();
    }

    @SysLog("查询医生")
    @GetMapping("/manage/queryDoctor")
    public RestResponse<IPage<User>> queryDoctor(@RequestParam(required = false, value = "nickname") String nickname) {
        //获取当前用户的部门
        User user = service.getById(SecurityUtils.getUser().getId());
        if (user == null || user.getDeptId() == null) {
            return RestResponse.failed("当前用户没有部门");
        }
        Page<User> page = getPage();
        QueryWrapper<User> queryWrapper = getQueryWrapper(getEntityClass());
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(20);
        if (SecurityUtils.getUser().getId() != 114) {
            queryWrapper.eq("user.dept_id", user.getDeptId());
        }
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.like("user.nickname", nickname);
        }
        IPage<User> data = service.queryUserByRole(roleIds, queryWrapper, page);
        //查询医生所属团队
        List<User> records = data.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> userIds = records.stream().map(User::getId)
                    .collect(Collectors.toList());
            List<DoctorTeamPeople> doctorTeamPeoples = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .in(DoctorTeamPeople::getUserId, userIds));
            if (!CollectionUtils.isEmpty(doctorTeamPeoples)) {
                List<Integer> teamIds = doctorTeamPeoples.stream().map(DoctorTeamPeople::getTeamId)
                        .collect(Collectors.toList());
                List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
                Map<Integer, DoctorTeam> teamMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));

                Map<Integer, List<DoctorTeamPeople>> doctorTeamPeopleMap = doctorTeamPeoples.stream()
                        .collect(Collectors.groupingBy(DoctorTeamPeople::getUserId));
                for (User user1 : records) {
                    List<DoctorTeamPeople> doctorTeamPeople = doctorTeamPeopleMap.get(user1.getId());
                    String doctorTeamName = "";
                    if (!CollectionUtils.isEmpty(doctorTeamPeople)) {
                        for (DoctorTeamPeople doctorTeamPeople1 : doctorTeamPeople) {
                            DoctorTeam doctorTeam = teamMap.get(doctorTeamPeople1.getTeamId());
                            doctorTeamName = doctorTeamName + doctorTeam.getName() + ",";
                        }
                        user1.setDoctorTeamName(doctorTeamName);
                    }
                }
            }
        }
        hospitalInfoService.getHospitalByUser(data.getRecords());
        return RestResponse.ok(data);
    }

    @SysLog("查询是业务员的用户")
    @GetMapping("/manage/querySalesman")
    public RestResponse<IPage<User>> querySalesman() {
        Page<User> page = getPage();
        QueryWrapper<User> queryWrapper = getQueryWrapper(getEntityClass());
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(18);
        queryWrapper.eq("user.del_flag", 0);
        return RestResponse.ok(service.queryUserByRole(roleIds, queryWrapper, page));
    }

    @SysLog("删除用户信息")
    @DeleteMapping("/manage/{id}")
    public RestResponse userDel(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, id));
        return service.deleteUserById(user) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED);
    }

    /**
     * 注销账号
     *
     * @return
     */
    @GetMapping("/cancel")
    public RestResponse cancel() {
        User user = new User();
        user.setId(SecurityUtils.getUser().getId());
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        return service.deleteUserById(user) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED);
    }

    @SysLog("更新用户信息")
    @PutMapping("/manage/update")
    public RestResponse updateUser(@Valid @RequestBody User user) {
        return service.updateUser(user) ? RestResponse.ok(DATA_UPDATE_SUCCESS, null) : RestResponse.failed(DATA_UPDATE_FAILED, null);
    }

    @GetMapping("/manage/pageAll")
    public RestResponse<IPage<User>> getUserPageAll(@RequestParam(required = false, value = "patientName") String patientName, @RequestParam(required = false, value = "nickname") String nickname, @RequestParam(required = false, value = "phone") String phone) {
        Page<User> page = getPage();
        QueryWrapper queryWrapper = new QueryWrapper<User>();
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.like("user.nickname", nickname);
        }
        if (!StringUtils.isEmpty(phone)) {
            queryWrapper.like("user.phone", phone);

        }
        if (!StringUtils.isEmpty(patientName)) {
            List<PatientUser> list = patientUserService.list(new QueryWrapper<PatientUser>().lambda()
                    .like(PatientUser::getName, patientName));
            if (!CollectionUtils.isEmpty(list)) {
                List<Integer> userIds = list.stream().map(PatientUser::getUserId)
                        .collect(Collectors.toList());
                queryWrapper.in("user.id", userIds);
            }


        }
        if (page != null) {
            Boolean aBoolean = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
            IPage iPage = service.pageScopedAllUserVo(page, queryWrapper, aBoolean);
            List<User> records = iPage.getRecords();
            if (!CollectionUtils.isEmpty(records)) {
                List<Integer> userIds = records.stream().map(User::getId)
                        .collect(Collectors.toList());
                List<PatientUser> patientUsers = patientUserService.list(new QueryWrapper<PatientUser>().lambda()
                        .in(PatientUser::getUserId, userIds));
                if (!CollectionUtils.isEmpty(patientUsers)) {
                    Map<Integer, List<PatientUser>> patientUserMap = patientUsers.stream()
                            .collect(Collectors.groupingBy(PatientUser::getUserId));

                    for (User user : records) {
                        user.setPatientUsers(patientUserMap.get(user.getId()));
                    }
                }
            }

            return RestResponse.ok(iPage);

        } else
            return RestResponse.ok(emptyPage());
    }


    @GetMapping("/manage/page")
    public RestResponse<IPage<User>> getUserPage(@RequestParam(required = false, value = "nickname") String nickname, @RequestParam(required = false, value = "phone") String phone) {
        Page<User> page = getPage();
        QueryWrapper queryWrapper = new QueryWrapper<User>();
        queryWrapper.isNull("user_role.user_id");
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.like("user.nickname", nickname);
        }
        if (!StringUtils.isEmpty(phone)) {
            queryWrapper.like("user.phone", phone);

        }

        if (page != null) {
            IPage iPage = service.pageScopedUserVo(page, queryWrapper);
            List<User> records = iPage.getRecords();
            if (!CollectionUtils.isEmpty(records)) {
                List<Integer> userIds = records.stream().map(User::getId)
                        .collect(Collectors.toList());
                List<PatientUser> patientUsers = patientUserService.list(new QueryWrapper<PatientUser>().lambda()
                        .in(PatientUser::getUserId, userIds));
                if (!CollectionUtils.isEmpty(patientUsers)) {
                    Map<Integer, List<PatientUser>> patientUserMap = patientUsers.stream()
                            .collect(Collectors.groupingBy(PatientUser::getUserId));

                    for (User user : records) {
                        user.setPatientUsers(patientUserMap.get(user.getId()));
                    }
                }
            }

            return RestResponse.ok(iPage);

        } else
            return RestResponse.ok(emptyPage());
    }

    @GetMapping("manage/list")
    public RestResponse list() {
        return RestResponse.ok(service.list());
    }

    //查询当前登录用户部门下的业务员
    @GetMapping("manage/listByDep")
    public RestResponse listByDep() {
        User user = service.getById(SecurityUtils.getUser().getId());
        Integer deptId = user.getDeptId();
        if (deptId == null) {
            return RestResponse.failed("没有查询到当前登录用户所属部门");
        }
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(user.getId());
        if (aBoolean) {
            List<User> users = service.list();
            if (CollUtil.isNotEmpty(users)) {
                List<Integer> userIds = users.stream().map(User::getId)
                        .collect(Collectors.toList());
                List<Integer> integers = CollUtil.toList(18);
                List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                        .in(UserRole::getRoleId, integers).in(UserRole::getUserId, userIds));
                if (CollUtil.isNotEmpty(userRoles)) {
                    List<Integer> collect = userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList());

                    List<User> collect1 = users.stream().filter(u -> collect.contains(u.getId())).collect(Collectors.toList());

                    return RestResponse.ok(collect1);
                }
            }
            return RestResponse.ok(new ArrayList<>());

        } else {
            List<User> list = userRoleService.getUsersByDeptIdAndRoleds(deptId, CollUtil.toList(18));
            return RestResponse.ok(list);

        }


    }

    @GetMapping("manage/salesmanListByDep")
    public RestResponse salesmanListByDep(@RequestParam("deptId") Integer deptId) {

        return RestResponse.ok(userRoleService.getUsersByDeptIdAndRoleds(deptId, CollUtil.toList(18)));
    }

    @GetMapping("/manage/{id}")
    public RestResponse<User> user(@PathVariable Integer id) {
        return RestResponse.ok(service.selectUserVoById(id));
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    /**
     * 设备穿件用户生成二维码扫码重定向绑定页面
     * ·
     *
     * @param uid
     * @param response
     */
    @SneakyThrows
    @GetMapping("/srBindAdress/{uid}/{macAdd}")
    public void handleFoo(@PathVariable Long uid, @PathVariable(value = "macAdd", required = false) String macAdd, HttpServletResponse response) {
        response.sendRedirect(urlData.getUrl() + QrCodeConstants.DEVICE_BIND_USER_URL + "?uid=" + uid + "&macAdd=" + macAdd);
    }

    @SneakyThrows
    @GetMapping("/srBindAdress/{uid}")
    public void handleFoo(@PathVariable Long uid, HttpServletResponse response) {
        response.sendRedirect(urlData.getUrl() + QrCodeConstants.DEVICE_BIND_USER_URL + "?uid=" + uid + "&macAdd=11");
    }

    @SysLog("更新用户信息 成为医生")
    @PostMapping("/manage/updateInfo")
    public RestResponse updateInfo(@RequestBody User user) {

        service.updateChangeDoctor(user);


        return RestResponse.ok();
    }

    /**
     * 修改密码
     *
     * @param userPwdDTO
     * @return
     */
    @PutMapping("/updatePwd")
    public RestResponse updatePwd(@RequestBody UserPwdDTO userPwdDTO) {

        service.updatePwd(userPwdDTO);

        return RestResponse.ok();
    }


    @SneakyThrows
    @GetMapping("/judgeUserIsAdmin")
    public RestResponse judgeUserIsAdmin() {
        Boolean isAdmin = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
        return RestResponse.ok(isAdmin ? 1 : 0);
    }

    /**
     * 根据身份证判断用户是否可以使用训练小程序
     */

    @GetMapping("/checkUserUseMa")
    public RestResponse checkUserUseMa(@RequestParam("idCard") String idCard) {

        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, idCard));
        if (CollectionUtils.isEmpty(tbTrainUsers)) {
            return RestResponse.ok(0);
        }
        TbTrainUser tbTrainUser = tbTrainUsers.get(0);
        if (tbTrainUser.getXtUserId() == null) {
            return RestResponse.ok(0);
        }
        if (tbTrainUser.getFirstTrainTime() == null) {
            return RestResponse.ok(0);
        }
        List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getUserId, tbTrainUser.getXtUserId()).orderByDesc(UserOrder::getPayTime));
        if (CollectionUtils.isEmpty(userOrders)) {
            return RestResponse.ok(0);
        }
        UserOrder userOrder = userOrders.get(0);
        Integer saleSpecServiceEndTime = userOrder.getSaleSpecServiceEndTime();
        if (saleSpecServiceEndTime == null) {
            return RestResponse.ok(0);
        }
        LocalDateTime localDateTime = tbTrainUser.getFirstTrainTime().plusDays(saleSpecServiceEndTime);

        if (localDateTime.isAfter(LocalDateTime.now())) {
            return RestResponse.ok(0);
        }

        return RestResponse.ok(Duration.between(LocalDateTime.now(), localDateTime).toDays());
    }
}
