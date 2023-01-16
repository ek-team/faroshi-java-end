package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户自己的服务信息
 */
@RestController
@RequestMapping("/userServicePackageInfo")
public class UserServicePackageInfoController extends AbstractBaseController<UserServicePackageInfoService, UserServicePackageInfo> {
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private UserService userService;
    @Resource
    private PatientUserService patientUserService;
    /**
     * 根据就诊人身份证查询用户的服务
     */
    @GetMapping("/listByIdCard")
    public RestResponse listByIdCard(@RequestParam(value = "idCard", required = false) String idCard) {
        List<PatientUser> patientUserList = patientUserService.list(new QueryWrapper<PatientUser>().lambda()
                .eq(PatientUser::getIdCard, idCard));
        if(CollectionUtils.isEmpty(patientUserList)){
            return RestResponse.ok(new ArrayList<>());
        }
        Integer userId = patientUserList.get(0).getUserId();
        User user = userService.getById(userId);

        LambdaQueryWrapper<UserServicePackageInfo> eq = new QueryWrapper<UserServicePackageInfo>().lambda()
                .eq(UserServicePackageInfo::getUserId, userId).orderByAsc(UserServicePackageInfo::getUseCount);

        List<UserServicePackageInfo> list = service.list(eq);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> ids = list.stream().map(UserServicePackageInfo::getServicePackageInfoId)
                    .collect(Collectors.toList());
            List<ServicePackageInfo> servicePackageInfos = (List<ServicePackageInfo>) servicePackageInfoService.listByIds(ids);
            Map<Integer, ServicePackageInfo> map = servicePackageInfos.stream()
                    .collect(Collectors.toMap(ServicePackageInfo::getId, t -> t));
            for (UserServicePackageInfo servicePackageInfo : list) {
                servicePackageInfo.setUser(user);
                servicePackageInfo.setServicePackageInfo(map.get(servicePackageInfo.getServicePackageInfoId()));
            }
        }
        return RestResponse.ok(list);
    }


    /**
     * 查询用户自己的服务信息
     * useStatus 1=待使用 2= 已使用
     *
     * @return
     */
    @GetMapping("/listByUserId")
    public RestResponse listByUserId(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        LambdaQueryWrapper<UserServicePackageInfo> eq = new QueryWrapper<UserServicePackageInfo>().lambda()
                .eq(UserServicePackageInfo::getUserId, SecurityUtils.getUser().getId());
        if (useStatus != null) {
            if (useStatus == 1) {
                eq.eq(UserServicePackageInfo::getUseCount, 0);
            } else {
                eq.ne(UserServicePackageInfo::getUseCount, 0);
            }
        }
        List<UserServicePackageInfo> list = service.list(eq);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> ids = list.stream().map(UserServicePackageInfo::getServicePackageInfoId)
                    .collect(Collectors.toList());
            List<ServicePackageInfo> servicePackageInfos = (List<ServicePackageInfo>) servicePackageInfoService.listByIds(ids);
            Map<Integer, ServicePackageInfo> map = servicePackageInfos.stream()
                    .collect(Collectors.toMap(ServicePackageInfo::getId, t -> t));
            for (UserServicePackageInfo servicePackageInfo : list) {
                servicePackageInfo.setServicePackageInfo(map.get(servicePackageInfo.getServicePackageInfoId()));
            }
        }
        return RestResponse.ok(list);
    }

    /**
     * 查询服务信息详情
     *
     * @return
     */
    @GetMapping("/getDetail")
    public RestResponse getDetail(@RequestParam("id") Integer id) {
        UserServicePackageInfo userServicePackageInfo = service.getById(id);

        ServicePackageInfo servicePackageInfo = servicePackageInfoService.getById(userServicePackageInfo.getServicePackageInfoId());

        userServicePackageInfo.setServicePackageInfo(servicePackageInfo);
        UserOrder userOrder = userOrdertService.getById(userServicePackageInfo.getOrderId());
        Integer doctorTeamId = userOrder.getDoctorTeamId();

        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getTeamId, doctorTeamId));
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                doctorTeamPeople.setUserName(userMap.get(doctorTeamPeople.getUserId()).getNickname());
                doctorTeamPeople.setAvatar(userMap.get(doctorTeamPeople.getUserId()).getAvatar());
            }

        }
        DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
        doctorTeam.setDoctorTeamPeopleList(doctorTeamPeopleList);
        userServicePackageInfo.setDoctorTeam(doctorTeam);
        return RestResponse.ok(userServicePackageInfo);
    }

    @Override
    protected Class<UserServicePackageInfo> getEntityClass() {
        return UserServicePackageInfo.class;
    }
}
