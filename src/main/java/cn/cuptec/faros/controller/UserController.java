package cn.cuptec.faros.controller;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.ChangeDoctorDTO;
import cn.cuptec.faros.dto.UserPwdDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends AbstractBaseController<UserService, User> {

    @Resource
    private DeptService deptService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private HospitalInfoService hospitalInfoService;

    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private PatientUserService patientUserService;

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
        return RestResponse.ok(patientUserService.list(new QueryWrapper<PatientUser>().lambda().eq(PatientUser::getUserId, SecurityUtils.getUser().getId())));
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
    public RestResponse<User> isSubscribe() {
        User user = service.selectUserVoById(SecurityUtils.getUser().getId());
        return RestResponse.ok(user);
    }

    @GetMapping("/infoByUid/{uid}")
    public RestResponse<User> user_me(@PathVariable int uid) {
        User user = service.getById(uid);
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
        user.setId(SecurityUtils.getUser().getId());
        return service.updateById(user) ? RestResponse.ok() : RestResponse.failed();
    }

    @PutMapping
    public RestResponse update(@RequestBody @Valid User user) {
        user.setId(SecurityUtils.getUser().getId());
        return service.updateById(user) ? RestResponse.ok() : RestResponse.failed();
    }

    @SysLog("添加用户")
    @PostMapping("/manage/add")
    @PreAuthorize("@pms.hasPermission('user_add')")
    public RestResponse<User> user(@RequestBody User user) {
        service.save(user);
        return RestResponse.ok();
    }

    @SysLog("查询医生")
    @GetMapping("/manage/queryDoctor")
    public RestResponse<IPage<User>> queryDoctor() {
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
        return RestResponse.ok(service.queryUserByRole(roleIds, queryWrapper, page));
    }

    @SysLog("删除用户信息")
    @DeleteMapping("/manage/{id}")
    @PreAuthorize("@pms.hasPermission('user_del')")
    public RestResponse userDel(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        return service.deleteUserById(user) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED);
    }

    @SysLog("更新用户信息")
    @PutMapping("/manage/update")
    @PreAuthorize("@pms.hasPermission('user_edit')")
    public RestResponse updateUser(@Valid @RequestBody User user) {
        return service.updateUser(user) ? RestResponse.ok(DATA_UPDATE_SUCCESS, null) : RestResponse.failed(DATA_UPDATE_FAILED, null);
    }


    @PreAuthorize("@pms.hasPermission('user_manage')")
    @GetMapping("/manage/page")
    public RestResponse<IPage<User>> getUserPage(@RequestParam(required = false, value = "nickname") String nickname, @RequestParam(required = false, value = "phone") String phone) {
        Page<User> page = getPage();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.like(User::getNickname, nickname);
        }
        if (!StringUtils.isEmpty(phone)) {
            queryWrapper.like(User::getPhone, phone);

        }

        if (page != null)
            return RestResponse.ok(service.pageScopedUserVo(page, queryWrapper));
        else
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
        //获取子部门
        List<Dept> childrenDepts = deptService.list(new QueryWrapper<Dept>().lambda().eq(Dept::getParentId, deptId));
        List<Integer> depIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(childrenDepts)) {
            depIds = childrenDepts.stream().map(Dept::getId)
                    .collect(Collectors.toList());
        }
        depIds.add(deptId);
        if (user.getNickname().equals("管理员")) {
            List<User> list = service.list(new QueryWrapper<User>().lambda().isNotNull(User::getDeptId));
            return RestResponse.ok(list);
        } else {
            List<User> list = service.list(new QueryWrapper<User>().lambda().in(User::getDeptId, depIds));
            return RestResponse.ok(list);
        }

    }

    @GetMapping("manage/salesmanListByDep")
    public RestResponse salesmanListByDep(@RequestParam("deptId") Integer deptId) {

        return RestResponse.ok(userRoleService.getUsersByDeptIdAndRoleds(deptId, CollUtil.toList(17, 18)));
    }

    @PreAuthorize("@pms.hasPermission('user_manage')")
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
        response.sendRedirect(QrCodeConstants.DEVICE_BIND_USER_URL + "?uid=" + uid + "&macAdd=" + macAdd);
    }

    @SneakyThrows
    @GetMapping("/srBindAdress/{uid}")
    public void handleFoo(@PathVariable Long uid, HttpServletResponse response) {
        response.sendRedirect(QrCodeConstants.DEVICE_BIND_USER_URL + "?uid=" + uid + "&macAdd=11");
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
}
