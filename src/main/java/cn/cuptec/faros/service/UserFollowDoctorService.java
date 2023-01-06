package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.UserFollowDoctor;
import cn.cuptec.faros.mapper.UserFollowDoctorMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFollowDoctorService extends ServiceImpl<UserFollowDoctorMapper, UserFollowDoctor> {
    public List<UserFollowDoctor> pageQueryPatientUserSort(Integer pageNum, Integer pageSize, Integer doctorId) {
        pageNum = (pageNum - 1) * pageSize;
        return baseMapper.pageQueryPatientUserSort(pageNum, pageSize, doctorId);
    }

    public int pageQueryPatientUserSortTotal( Integer doctorId) {
        return baseMapper.pageQueryPatientUserSortTotal( doctorId);
    }
}
