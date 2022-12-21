package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 删除用户就诊人
     *
     * @return
     */
    @GetMapping("/deletePatientUser")
    public RestResponse deletePatientUser(@RequestParam("id") Integer id) {
        return RestResponse.ok(patientUserService.removeById(id));
    }

    /**
     * 获取个人信息
     *
     * @return
     */
    @GetMapping("/info")
    public RestResponse<User> user_me() {
        return RestResponse.ok(service.selectUserVoById(SecurityUtils.getUser().getId()));
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
        return RestResponse.ok(service.getById(uid));
    }

    @GetMapping("/getByUid")
    public RestResponse<User> getByUid(@RequestParam(value = "uid", required = false) Integer uid) {
        if (uid == null && SecurityUtils.getUser() != null) {
            uid = SecurityUtils.getUser().getId();
        }
        return RestResponse.ok(service.getById(uid));
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
