package cn.cuptec.faros.service;

import cn.cuptec.faros.bo.DoctorInfoBO;
import cn.cuptec.faros.entity.HospitalDoctorRelation;
import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.mapper.HospitalInfoMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalInfoService extends ServiceImpl<HospitalInfoMapper, HospitalInfo> {
    @Resource
    private UserService userService;
    
    @Resource
    private HospitalDoctorRelationService hospitalDoctorRelationService;
    
    

    public Object listByDoctor(int id) {
        LambdaQueryWrapper wrapper = new QueryWrapper<HospitalDoctorRelation>().lambda()
                .eq(HospitalDoctorRelation::getHospitalId,id);
        List<HospitalDoctorRelation> list = hospitalDoctorRelationService.list(wrapper);
        if(CollUtil.isEmpty(list))  return  CollUtil.toList();

        List<Integer> userIds = list.stream().map(HospitalDoctorRelation::getUserId).collect(Collectors.toList());

        return userService.getBaseMapper().selectBatchIds(userIds) ;
    }

    public DoctorInfoBO getDoctorInfoByUserId(Integer userId) {

        DoctorInfoBO bo = new DoctorInfoBO();




        User user = userService.getById(userId);
        bo.setUserInfo(user);
        HospitalDoctorRelation hospitalByUserId = hospitalDoctorRelationService.getHospitalByUserId(userId);
        if(hospitalByUserId != null)
            bo.setHospitalInfo(this.getById(hospitalByUserId.getHospitalId()));

        return bo;
    }

    public void getHospitalByUser(List<User> records) {
        if(CollUtil.isNotEmpty(records)){
            List<Integer> userIds = records.stream().map(User::getId).collect(Collectors.toList());
            List<HospitalDoctorRelation> hospitalByUserIds = hospitalDoctorRelationService.getHospitalByUserIds(userIds);
            if(CollUtil.isNotEmpty(hospitalByUserIds)){
                List<Integer> hospitalIds = hospitalByUserIds.stream().map(HospitalDoctorRelation::getHospitalId).collect(Collectors.toList());
                Collection<HospitalInfo> hospitalInfos = this.listByIds(hospitalIds);
                if(CollUtil.isNotEmpty(hospitalInfos)){
                    x:for (User user: records) {
                        for (HospitalDoctorRelation hospitalDoctorRelation: hospitalByUserIds) {
                            if(user.getId().equals(hospitalDoctorRelation.getUserId())){
                                for (HospitalInfo hospitalInfo: hospitalInfos   ) {
                                    if(hospitalInfo.getId().equals(hospitalDoctorRelation.getHospitalId())){
                                        user.setHospitalInfo(hospitalInfo);
                                        user.setHospitalName(hospitalInfo.getName());
                                        continue x;
                                    }
                                }
                            }

                        }
                    }
                }

            }
        }

    }
    public void getHospitalNameByUser(List<User> records) {
        if(CollUtil.isNotEmpty(records)){
            List<Integer> userIds = records.stream().map(User::getId).collect(Collectors.toList());
            List<HospitalDoctorRelation> hospitalByUserIds = hospitalDoctorRelationService.getHospitalByUserIds(userIds);
            if(CollUtil.isNotEmpty(hospitalByUserIds)){
                List<Integer> hospitalIds = hospitalByUserIds.stream().map(HospitalDoctorRelation::getHospitalId).collect(Collectors.toList());
                Collection<HospitalInfo> hospitalInfos = this.listByIds(hospitalIds);
                if(CollUtil.isNotEmpty(hospitalInfos)){
                    x:for (User user: records) {
                        for (HospitalDoctorRelation hospitalDoctorRelation: hospitalByUserIds) {
                            if(user.getId().equals(hospitalDoctorRelation.getUserId())){
                                for (HospitalInfo hospitalInfo: hospitalInfos   ) {
                                    if(hospitalInfo.getId().equals(hospitalDoctorRelation.getHospitalId())){
                                        user.setHospitalName(hospitalInfo.getName());
                                        continue x;
                                    }
                                }
                            }

                        }
                    }
                }

            }
        }

    }


}