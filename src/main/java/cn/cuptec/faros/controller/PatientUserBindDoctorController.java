package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DoctorTeam;
import cn.cuptec.faros.entity.PatientUserBindDoctor;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.PatientUserBindDoctorService;
import cn.cuptec.faros.service.ProductService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 就诊人和医生的绑定
 */
@RestController
@RequestMapping("/patientUserBindDoctor")
public class PatientUserBindDoctorController extends AbstractBaseController<PatientUserBindDoctorService, PatientUserBindDoctor> {
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public RestResponse add(@RequestBody PatientUserBindDoctor patientUserBindDoctor) {
        service.save(patientUserBindDoctor);
        return RestResponse.ok();
    }
    @GetMapping("/list")
    public RestResponse list(@RequestParam("patientId") Integer patientId) {
        List<PatientUserBindDoctor> list = service.list(new QueryWrapper<PatientUserBindDoctor>().lambda().eq(PatientUserBindDoctor::getPatientUserId, patientId));
        if(!CollectionUtils.isEmpty(list)){
            List<Integer> doctorIds=new ArrayList<>();
            List<Integer> doctorTeamIds=new ArrayList<>();
            for(PatientUserBindDoctor patientUserBindDoctor:list){
                if(patientUserBindDoctor.getDoctorId()!=null){
                    doctorIds.add(patientUserBindDoctor.getDoctorId());
                }
                if(patientUserBindDoctor.getDoctorTeamId()!=null){
                    doctorTeamIds.add(patientUserBindDoctor.getDoctorTeamId());
                }
            }
            Map<Integer, User> userMap=new HashMap<>();
            if(!CollectionUtils.isEmpty(doctorIds)){
                List<User> users = (List<User>) userService.listByIds(doctorIds);
              userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
            }
            Map<Integer, DoctorTeam> doctorTeamMap =new HashMap<>();
            if(!CollectionUtils.isEmpty(doctorTeamIds)){
                List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(doctorTeamIds);
                 doctorTeamMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
            }
            for(PatientUserBindDoctor patientUserBindDoctor:list){
                if(patientUserBindDoctor.getDoctorId()!=null){
                    patientUserBindDoctor.setDoctor(userMap.get(patientUserBindDoctor.getDoctorId()));
                }
                if(patientUserBindDoctor.getDoctorTeamId()!=null){
                    patientUserBindDoctor.setDoctorTeam(doctorTeamMap.get(patientUserBindDoctor.getDoctorTeamId()));
                }
            }
        }
        return RestResponse.ok(list);
    }
    @Override
    protected Class<PatientUserBindDoctor> getEntityClass() {
        return PatientUserBindDoctor.class;
    }
}
