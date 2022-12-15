package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.entity.HospitalDoctorRelation;
import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.UserDoctorRelation;
import cn.cuptec.faros.mapper.HospitalDoctorRelationMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalDoctorRelationService extends ServiceImpl<HospitalDoctorRelationMapper, HospitalDoctorRelation> {


    @Resource
    private HospitalInfoService hospitalInfoService;

    @Transactional(rollbackFor = Exception.class)
    public void bind(Integer hospitalId, Integer userId) {
        LambdaQueryWrapper wrapper = new QueryWrapper<HospitalInfo>().lambda()
                .eq(HospitalInfo::getId, hospitalId);


        HospitalInfo one = hospitalInfoService.getOne(wrapper);
        if (one == null) {
            throw new InnerException("请选择正确的医院", new Object[]{}, null);
        }
        wrapper = new QueryWrapper<HospitalDoctorRelation>().lambda()
                .eq(HospitalDoctorRelation::getUserId, userId);
        int count = this.count(wrapper);
        if (count != 0) throw new InnerException("医生已经绑定医院", new Object[]{}, null);

        this.save(new HospitalDoctorRelation(hospitalId, userId, 1));

    }

    public HospitalDoctorRelation getHospitalByUserId(Integer userId) {
        LambdaQueryWrapper wrapper = new QueryWrapper<HospitalDoctorRelation>().lambda()
                .eq(HospitalDoctorRelation::getUserId, userId);


        return this.getOne(wrapper);

    }

    public List<HospitalDoctorRelation> getHospitalByUserIds(List<Integer> userIds) {
        LambdaQueryWrapper wrapper = new QueryWrapper<HospitalDoctorRelation>().lambda()
                .in(HospitalDoctorRelation::getUserId, userIds);

        return this.list(wrapper);
    }

    public List<Integer> getUserIdsByHospitalId(IPage<HospitalDoctorRelation> page, Integer hospitalId) {
        IPage<HospitalDoctorRelation> p = this.page(page, new QueryWrapper<HospitalDoctorRelation>().lambda().eq(HospitalDoctorRelation::getHospitalId, hospitalId).eq(HospitalDoctorRelation::getType, 2));
        if (CollectionUtils.isEmpty(p.getRecords())) {
            return CollUtil.toList();
        }


        return p.getRecords().stream().map(HospitalDoctorRelation::getUserId).collect(Collectors.toList());
    }
}