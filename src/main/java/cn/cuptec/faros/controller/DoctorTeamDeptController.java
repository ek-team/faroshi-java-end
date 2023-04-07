package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.DoctorTeam;
import cn.cuptec.faros.entity.DoctorTeamDept;
import cn.cuptec.faros.service.DeptService;
import cn.cuptec.faros.service.DoctorTeamDeptService;
import cn.cuptec.faros.service.DoctorTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医生团队和部门的关联
 */

@AllArgsConstructor
@RestController
@RequestMapping("/doctorTeamDept")
public class DoctorTeamDeptController extends AbstractBaseController<DoctorTeamDeptService, DoctorTeamDept> {

    @Resource
    private DeptService deptService;
    @Resource
    private DoctorTeamService doctorTeamService;

    @PostMapping("/add")
    public RestResponse add(@RequestBody DoctorTeamDept doctorTeamDept) {

        service.save(doctorTeamDept);
        Integer teamId = doctorTeamDept.getTeamId();
        DoctorTeam doctorTeam = doctorTeamService.getById(teamId);
        String deptIdList = doctorTeam.getDeptIdList();
        deptIdList = deptIdList + "," + doctorTeamDept.getDept();
        doctorTeam.setDeptIdList(deptIdList);
        doctorTeamService.updateById(doctorTeam);
        return RestResponse.ok();
    }

    @GetMapping("/delete")
    public RestResponse delete(@RequestParam("teamId") Integer teamId, @RequestParam("deptId") Integer deptId) {

        service.remove(new QueryWrapper<DoctorTeamDept>().lambda().eq(DoctorTeamDept::getDeptId, deptId)
                .eq(DoctorTeamDept::getTeamId, teamId));

        DoctorTeam doctorTeam = doctorTeamService.getById(teamId);
        String deptIdList = doctorTeam.getDeptIdList();
        String replace = deptIdList.replace(deptId + "", "");
        doctorTeam.setDeptIdList(replace);
        doctorTeamService.updateById(doctorTeam);

        return RestResponse.ok();
    }

    @GetMapping("/list")
    public RestResponse list(@RequestParam("teamId") Integer teamId) {

        List<DoctorTeamDept> list = service.list(new QueryWrapper<DoctorTeamDept>().lambda().eq(DoctorTeamDept::getTeamId, teamId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(new ArrayList<>());
        }
        List<Integer> deptIds = list.stream().map(DoctorTeamDept::getDeptId)
                .collect(Collectors.toList());
        List<Dept> depts = (List<Dept>) deptService.listByIds(deptIds);
        Map<Integer, Dept> deptMap = depts.stream()
                .collect(Collectors.toMap(Dept::getId, t -> t));
        for (DoctorTeamDept doctorTeamDept : list) {
            doctorTeamDept.setDept(deptMap.get(doctorTeamDept.getDeptId()));
        }

        return RestResponse.ok(list);
    }

    @Override
    protected Class<DoctorTeamDept> getEntityClass() {
        return DoctorTeamDept.class;
    }

}
