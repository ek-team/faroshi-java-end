package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorTeamPeopleService;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医生团队管理
 */
@RestController
@RequestMapping("/doctorTeam")
public class DoctorTeamController extends AbstractBaseController<DoctorTeamService, DoctorTeam> {
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private UserService userService;

    /**
     * 添加医生团队
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody DoctorTeam doctorTeam) {
        User byId = userService.getById(SecurityUtils.getUser().getId());

        doctorTeam.setStatus(0);
        doctorTeam.setDeptId(byId.getDeptId());
        doctorTeam.setCreateTime(LocalDateTime.now());
        service.save(doctorTeam);
        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeam.getDoctorTeamPeopleList();
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                doctorTeamPeople.setTeamId(doctorTeam.getId());
            }

            doctorTeamPeopleService.saveBatch(doctorTeamPeopleList);
        }
        return RestResponse.ok();
    }

    /**
     * 编辑医生团队
     *
     * @return
     */
    @PostMapping("/update")
    public RestResponse update(@RequestBody DoctorTeam doctorTeam) {
        service.updateById(doctorTeam);
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, doctorTeam.getId()));
        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeam.getDoctorTeamPeopleList();
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                doctorTeamPeople.setTeamId(doctorTeam.getId());
            }
            doctorTeamPeopleService.saveBatch(doctorTeamPeopleList);
        }
        return RestResponse.ok();
    }

    /**
     * 删除医生团队
     *
     * @return
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") int id) {
        service.removeById(id);
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, id));
        return RestResponse.ok();
    }

    /**
     * 分页查询医生团队
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped() {
        Page<DoctorTeam> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        User user = userService.getById(SecurityUtils.getUser().getId());
        DataScope dataScope = new DataScope();

        if (user.getDeptId().equals(1)) {
            dataScope.setIsOnly(false);
        } else {
            dataScope.setIsOnly(true);
            queryWrapper.eq("status", 1);
        }

        IPage<DoctorTeam> doctorTeamIPage = service.pageScoped(page, queryWrapper, dataScope);
        return RestResponse.ok(doctorTeamIPage);
    }

    /**
     * 根据id查询详情
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") int id) {
        DoctorTeam byId = service.getById(id);
        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, id));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> userIds = list.stream().map(DoctorTeamPeople::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (DoctorTeamPeople doctorTeamPeople : list) {
                doctorTeamPeople.setUserName(userMap.get(doctorTeamPeople.getUserId()).getNickname());
            }
        }
        byId.setDoctorTeamPeopleList(list);
        return RestResponse.ok(byId);
    }

    /**
     * 审核医生团队
     * @return
     */
    @PostMapping("/checkDoctorTeam")
    public RestResponse checkDoctorTeam(@RequestBody DoctorTeam doctorTeam) {
        service.updateById(doctorTeam);
        return RestResponse.ok();
    }
    @Override
    protected Class<DoctorTeam> getEntityClass() {
        return DoctorTeam.class;
    }

}
