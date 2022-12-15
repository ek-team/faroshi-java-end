package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.UserDoctorRelationMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDoctorRelationService extends ServiceImpl<UserDoctorRelationMapper, UserDoctorRelation> {
    @Resource
    private UserService userService;

    @Resource
    private HospitalInfoService hospitalInfoService;

    public User getDoctorByUser(Integer userId) {

        List<UserDoctorRelation> list = list(new QueryWrapper<UserDoctorRelation>().lambda().eq(UserDoctorRelation::getUserId, userId));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        // UserDoctorRelation byId = this.getById(userId);
        UserDoctorRelation userDoctorRelation = list.get(0);
        if (userDoctorRelation == null) {
            return null;
        }

        return userService.getById(userDoctorRelation.getDoctorId());

    }

    public List<Integer> getUserIdsByDoctorId(IPage<UserDoctorRelation> page,Integer doctorId) {

        IPage<UserDoctorRelation> p = this.page(page,new QueryWrapper<UserDoctorRelation>().lambda().eq(UserDoctorRelation::getDoctorId, doctorId));
        if (CollectionUtils.isEmpty(p.getRecords())) {
            return CollUtil.toList();
        }


        return p.getRecords().stream().map(UserDoctorRelation::getUserId).collect(Collectors.toList());

    }



    public void getDoctorByUserList(List<User> records) {
        if(CollUtil.isNotEmpty(records)) {
            List<Integer> userIds = records.stream().map(User::getId).collect(Collectors.toList());
            List<UserDoctorRelation> userDoctorRelations = this.getInfoByUserIds(userIds);
            if (CollUtil.isNotEmpty(userDoctorRelations)) {
                List<Integer> doctorIds = userDoctorRelations.stream().map(UserDoctorRelation::getDoctorId).collect(Collectors.toList());
                Collection<User> doctorUsers = userService.listByIds(doctorIds);
                if (CollUtil.isNotEmpty(doctorUsers)) {
                    x:for (User user : records) {
                        for (UserDoctorRelation  userDoctorRelation: userDoctorRelations) {
                            if (user.getId().equals(userDoctorRelation.getUserId())) {
                                for (User doctor : doctorUsers) {
                                    if (doctor.getId().equals(userDoctorRelation.getDoctorId())) {
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

    private List<UserDoctorRelation> getInfoByUserIds(List<Integer> userIds) {
        if(CollUtil.isEmpty(userIds)) return  CollUtil.toList();
        LambdaQueryWrapper<UserDoctorRelation> wrapper = new QueryWrapper<UserDoctorRelation>().lambda().in(UserDoctorRelation::getUserId, userIds);
          return   this.list(wrapper);
    }


    public IPage<User> getMyDoctorPage(Page<UserDoctorRelation> page, Integer userId) {


         this.page(page,new QueryWrapper<UserDoctorRelation>().lambda().eq(UserDoctorRelation::getUserId,userId));
         IPage<User> p = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());

        if(CollUtil.isNotEmpty(page.getRecords())){

            List<Integer> doctorIds = page.getRecords().stream().map(UserDoctorRelation::getDoctorId).collect(Collectors.toList());
            Collection<User> doctorList = userService.listByIds(doctorIds);
            ArrayList<User> records = new ArrayList<>(doctorList);
            p.setRecords(records);
            if(CollUtil.isNotEmpty(records)){
                hospitalInfoService.getHospitalNameByUser(records);
            }
        }

        return p;


    }


}