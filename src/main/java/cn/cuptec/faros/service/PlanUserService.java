package cn.cuptec.faros.service;

import java.util.*;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.PlanUserMapper;
import cn.cuptec.faros.util.SnowflakeIdWorker;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.stream.Collectors;

@Service
public class PlanUserService extends ServiceImpl<PlanUserMapper, TbTrainUser> {

    @Resource
    private PatientUserService patientUserService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private UserService userService;
    @Resource
    private HospitalDoctorRelationService hospitalDoctorRelationService;
    @Resource
    private ProductStockService productStockService;

    @Resource
    private DoctorTeamService doctorTeamService;

    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private DeviceScanSignLogService deviceScanSignLogService;
    @Resource
    private PlanUserOtherInfoService planUserOtherInfoService;

    @Transactional(rollbackFor = Exception.class)
    public void bindSystemUserId(long uid, String macAdd) {
        Integer userId = SecurityUtils.getUser().getId();
        TbTrainUser one = getInfoByUXtUserId(userId);
        TbTrainUser tbTrainUser = getOne(new QueryWrapper<TbTrainUser>().lambda()
                .eq(TbTrainUser::getUserId, uid));
        deviceScanSignLogService.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, tbTrainUser.getMacAdd()).eq(DeviceScanSignLog::getUserId, uid));
        DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

        deviceScanSignLog.setUserId(uid + "");

        deviceScanSignLog.setMacAddress(macAdd);


        deviceScanSignLogService.save(deviceScanSignLog);

        User user = userService.getById(userId);
        if (user != null) {

        }
        if (one == null) {
            one = this.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getUserId, uid).last(" for update "));
            if (one != null && one.getXtUserId() == null) {
                //添加医院
                if (!StringUtils.isEmpty(one.getMacAdd())) {
                    ProductStock productStock = productStockService.getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getMacAddress, one.getMacAdd()).eq(ProductStock::getDel, 1));
                    if (productStock != null && productStock.getHospitalId() != null) {
                        hospitalDoctorRelationService.remove(new QueryWrapper<HospitalDoctorRelation>().lambda().eq(HospitalDoctorRelation::getUserId, userId));

                        //绑定医院
                        HospitalDoctorRelation hospitalDoctorRelation = new HospitalDoctorRelation();
                        hospitalDoctorRelation.setUserId(userId);
                        hospitalDoctorRelation.setHospitalId(productStock.getHospitalId());
                        hospitalDoctorRelation.setType(2);
                        hospitalDoctorRelationService.save(hospitalDoctorRelation);
                    }
                }
                this.update(new UpdateWrapper<TbTrainUser>().lambda().set(TbTrainUser::getUpdateDate, new Date()).set(TbTrainUser::getTelePhone, user.getPhone()).set(TbTrainUser::getXtUserId, userId).eq(TbTrainUser::getId, one.getId()));
                return;
            } else if (one != null && one.getXtUserId() != null && one.getXtUserId().equals(userId)) {
                return;

            } else if (one != null && one.getXtUserId() != null && !one.getXtUserId().equals(userId)) {
                throw new RuntimeException("设备账号已被绑定;");
            } else
                throw new RuntimeException("设备账号未同步;");

        } else {
            if (one.getUserId().equals(uid)) return;
        }

        throw new RuntimeException("已绑定设备账号;");

    }

    public IPage<TbTrainUser> getPageUserByDoctorId(IPage<TbTrainUser> page, Integer doctorId, Integer hospitalId) {
        //查询医生所在的医院
        if (hospitalId == null) {
            HospitalDoctorRelation hospitalDoctorRelation = hospitalDoctorRelationService.getHospitalByUserId(doctorId);
            if (hospitalDoctorRelation == null) return page;
            hospitalId = hospitalDoctorRelation.getHospitalId();
        }
        LambdaQueryWrapper<TbTrainUser> eq = Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getHospitalId, hospitalId);
        if (hospitalId != null) {
            HospitalInfo byId = hospitalInfoService.getById(hospitalId);
            eq.eq(TbTrainUser::getHospitalId, hospitalId);
            if (byId != null) {
                eq.or();
                eq.eq(TbTrainUser::getHospitalName, byId.getName());
            }
        }

        IPage page1 = this.page(new Page(page.getCurrent(), page.getSize()), eq);

//

        return page1;

    }


    public TbTrainUser getInfoByUXtUserId(Integer xtUserId) {


        return this.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, xtUserId));


    }


    public TbTrainUser getInfoByPhoneAndIdCard(String phone, String idCard, String xtUserId) {

        LambdaQueryWrapper<TbTrainUser> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(phone)) {
            queryWrapper.eq(TbTrainUser::getTelePhone, phone);
        }
        if (!StringUtils.isEmpty(idCard)) {
            queryWrapper.eq(TbTrainUser::getIdCard, idCard);
        }
        if (!StringUtils.isEmpty(xtUserId)) {
            queryWrapper.eq(TbTrainUser::getXtUserId, xtUserId);
        }
        TbTrainUser one = this.getOne(queryWrapper);
        if (one != null) {
            if (StringUtils.isEmpty(one.getTelePhone())) {
                LambdaQueryWrapper<PatientUser> eq = new QueryWrapper<PatientUser>().lambda();
                if (!StringUtils.isEmpty(idCard)) {
                    eq.eq(PatientUser::getIdCard, idCard);

                }
                if (!StringUtils.isEmpty(phone)) {
                    eq.eq(PatientUser::getPhone, phone);

                }
                List<PatientUser> patientUsers = patientUserService.list(eq);
                if (!CollectionUtils.isEmpty(patientUsers)) {
                    one.setTelePhone(patientUsers.get(0).getPhone());
                }
            }
        }
        return one;


    }

    public List<TbTrainUser> getInfosByUXtUserIds(List<Integer> xtUserIds) {


        return this.list(Wrappers.<TbTrainUser>lambdaQuery().in(TbTrainUser::getXtUserId, xtUserIds));


    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSaveOrUpdate(List<TbTrainUser> userBeanList) {

        if (!CollectionUtils.isEmpty(userBeanList)) {
            SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0, 0);
            for (TbTrainUser tbTrainUser : userBeanList) {
                if (!StringUtils.isEmpty(tbTrainUser.getBodyPartName()) && !StringUtils.isEmpty(tbTrainUser.getSecondDiseaseName())) {
                    PlanUserOtherInfo planUserOtherInfo = planUserOtherInfoService.getOne(new QueryWrapper<PlanUserOtherInfo>().lambda().eq(PlanUserOtherInfo::getIdCard, tbTrainUser.getIdCard()));
                    if (planUserOtherInfo == null) {
                        planUserOtherInfo = new PlanUserOtherInfo();

                        planUserOtherInfo.setBodyPartName(tbTrainUser.getBodyPartName());
                        planUserOtherInfo.setSecondDiseaseName(tbTrainUser.getSecondDiseaseName());
                        planUserOtherInfoService.save(planUserOtherInfo);
                    } else {
                        planUserOtherInfo.setBodyPartName(tbTrainUser.getBodyPartName());
                        planUserOtherInfo.setSecondDiseaseName(tbTrainUser.getSecondDiseaseName());
                        planUserOtherInfoService.updateById(planUserOtherInfo);
                    }
                }

                //判断订单是否有团队 没有则同步
                if (tbTrainUser.getDoctorTeamId() == null || tbTrainUser.getDoctorTeamId().equals(0)) {
                    List<PatientUser> list = patientUserService.list(new QueryWrapper<PatientUser>().
                            lambda().eq(PatientUser::getIdCard, tbTrainUser.getIdCard()));
                    if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
                        List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().
                                lambda().eq(UserOrder::getPatientUserId, list.get(0).getId()));
                        if (!org.springframework.util.CollectionUtils.isEmpty(userOrders)) {
                            tbTrainUser.setDeptId(userOrders.get(0).getDeptId());
                            Integer doctorTeamId = userOrders.get(0).getDoctorTeamId();
                            DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
                            tbTrainUser.setDoctorTeamId(doctorTeamId);
                            tbTrainUser.setDoctorTeam(doctorTeam.getName());
                        }
                    }
                }
                if (tbTrainUser.getDoctorTeamId() != null) {
                    DoctorTeam doctorTeam = doctorTeamService.getById(tbTrainUser.getDoctorTeamId());
                    if (doctorTeam != null) {
                        tbTrainUser.setDeptId(doctorTeam.getDeptId());
                        tbTrainUser.setHospitalId(doctorTeam.getHospitalId() + "");
                        HospitalInfo hospitalInfo = hospitalInfoService.getById(doctorTeam.getHospitalId());
                        if (hospitalInfo != null) {
                            tbTrainUser.setHospitalName(hospitalInfo.getName());
                        }
                    }
                }

                if (StringUtils.isEmpty(tbTrainUser.getIdCard()) && !StringUtils.isEmpty(tbTrainUser.getCaseHistoryNo())) {
                    tbTrainUser.setIdCard(tbTrainUser.getCaseHistoryNo());
                }
                List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().
                        lambda().eq(ProductStock::getMacAddress, tbTrainUser.getMacAdd()).eq(ProductStock::getDel, 1));
                if (!CollectionUtils.isEmpty(productStocks)) {
                    tbTrainUser.setRegisterProductSn(productStocks.get(0).getProductSn());
                    tbTrainUser.setDeptId(productStocks.get(0).getDeptId());
                }
                if (StringUtils.isEmpty(tbTrainUser.getUserId())) {
                    tbTrainUser.setUserId(idUtil.nextId() + "");
                }
                if (tbTrainUser.getKeyId() == null) {
                    tbTrainUser.setKeyId(idUtil.nextId());
                }
                //如果userId存在 则修改 否则添加
                List<TbTrainUser> oldUser = list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, tbTrainUser.getUserId()));
                if (CollectionUtils.isEmpty(oldUser)) {
                    Integer xtUserId = null;
                    //如果身份证号存在则 覆盖
                    List<TbTrainUser> list = list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, tbTrainUser.getIdCard()));
                    if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
                        TbTrainUser tbTrainUser1 = list.get(0);
                        xtUserId = tbTrainUser1.getXtUserId();
                        tbTrainUser1.setXtUserId(null);
                        tbTrainUser1.setIdCard("被覆盖" + tbTrainUser.getIdCard());
                        updateById(tbTrainUser1);
                    }
                    tbTrainUser.setXtUserId(xtUserId);
                    save(tbTrainUser);
                } else {
                    //修改
                    TbTrainUser tbTrainUser1 = oldUser.get(0);
                    tbTrainUser.setId(tbTrainUser1.getId());
                    tbTrainUser.setXtUserId(tbTrainUser1.getXtUserId());
                    updateById(tbTrainUser);
                }
            }

        }


    }

    @Transactional(rollbackFor = Exception.class)
    public TbTrainUser newSaveBatch(List<TbTrainUser> userBeanList) {

        if (!CollectionUtils.isEmpty(userBeanList)) {
            SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0, 0);
            for (TbTrainUser tbTrainUser : userBeanList) {

                PlanUserOtherInfo planUserOtherInfo = planUserOtherInfoService.getOne(new QueryWrapper<PlanUserOtherInfo>().lambda().eq(PlanUserOtherInfo::getIdCard, tbTrainUser.getIdCard()));
                if (planUserOtherInfo == null) {
                    planUserOtherInfo = new PlanUserOtherInfo();
                    planUserOtherInfo.setBodyPartName(tbTrainUser.getBodyPartName());
                    planUserOtherInfo.setSecondDiseaseName(tbTrainUser.getSecondDiseaseName());
                    planUserOtherInfoService.save(planUserOtherInfo);
                } else {
                    planUserOtherInfo.setBodyPartName(tbTrainUser.getBodyPartName());
                    planUserOtherInfo.setSecondDiseaseName(tbTrainUser.getSecondDiseaseName());
                    planUserOtherInfoService.updateById(planUserOtherInfo);
                }
                //判断订单是否有团队 没有则同步
                if (tbTrainUser.getDoctorTeamId() == null || tbTrainUser.getDoctorTeamId().equals(0)) {
                    List<PatientUser> list = patientUserService.list(new QueryWrapper<PatientUser>().
                            lambda().eq(PatientUser::getIdCard, tbTrainUser.getIdCard()));
                    if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
                        List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().
                                lambda().eq(UserOrder::getPatientUserId, list.get(0).getId()));
                        if (!org.springframework.util.CollectionUtils.isEmpty(userOrders)) {
                            tbTrainUser.setDeptId(userOrders.get(0).getDeptId());
                            Integer doctorTeamId = userOrders.get(0).getDoctorTeamId();
                            DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
                            tbTrainUser.setDoctorTeamId(doctorTeamId);
                            tbTrainUser.setDoctorTeam(doctorTeam.getName());
                        }
                    }
                }
                if (tbTrainUser.getDoctorTeamId() != null) {
                    DoctorTeam doctorTeam = doctorTeamService.getById(tbTrainUser.getDoctorTeamId());
                    if (doctorTeam != null) {
                        tbTrainUser.setDeptId(doctorTeam.getDeptId());
                        tbTrainUser.setHospitalId(doctorTeam.getHospitalId() + "");
                        HospitalInfo hospitalInfo = hospitalInfoService.getById(doctorTeam.getHospitalId());
                        if (hospitalInfo != null) {
                            tbTrainUser.setHospitalName(hospitalInfo.getName());
                        }
                    }
                }


                if (StringUtils.isEmpty(tbTrainUser.getIdCard()) && !StringUtils.isEmpty(tbTrainUser.getCaseHistoryNo())) {
                    tbTrainUser.setIdCard(tbTrainUser.getCaseHistoryNo());
                }
                List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().
                        lambda().eq(ProductStock::getMacAddress, tbTrainUser.getMacAdd()).eq(ProductStock::getDel, 1));
                if (!CollectionUtils.isEmpty(productStocks)) {
                    tbTrainUser.setRegisterProductSn(productStocks.get(0).getProductSn());
                    tbTrainUser.setDeptId(productStocks.get(0).getDeptId());
                }
                if (StringUtils.isEmpty(tbTrainUser.getUserId())) {
                    tbTrainUser.setUserId(idUtil.nextId() + "");
                }
                if (tbTrainUser.getKeyId() == null) {
                    tbTrainUser.setKeyId(idUtil.nextId());
                }
                List<TbTrainUser> tbTrainUsers = list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, tbTrainUser.getIdCard()));
                if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                    return tbTrainUsers.get(0);
                }
                save(tbTrainUser);
            }

        }

        return null;
    }

}
