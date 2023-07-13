package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.controller.base.AbstractBaseController;

import cn.cuptec.faros.dto.QuerySpecSelect;
import cn.cuptec.faros.entity.*;

import cn.cuptec.faros.service.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 服务包管理
 */
@RestController
@RequestMapping("/servicePack")
public class ServicePackController extends AbstractBaseController<ServicePackService, ServicePack> {
    @Resource
    private ServicePackProductSpecService servicePackProductSpecService;//产品规格
    @Resource
    private OperationRecordService operationRecordService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;//服务信息
    @Resource
    private ServicePackDetailService servicePackDetailService;//服务详情页面
    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private DeptService deptService;

    @Resource
    private ProductSpecDescService productSpecDescService;//产品规格描述
    @Resource
    private SaleSpecService saleSpecService;//销售规格
    @Resource
    private SaleSpecGroupService saleSpecGroupService;
    @Resource
    private SaleSpecDescService saleSpecDescService;//销售规格子类
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private IntroductionService introductionService;//服务简介
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    @Resource
    private ServicePackDiseasesService servicePackDiseasesService;
    @Resource
    private DiseasesService diseasesService;
    @Resource
    private UserQrCodeService userQrCodeService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private RentRuleService rentRuleService;
    @Resource
    private RecyclingRuleService recyclingRuleService;


    /**
     * 设备二维码绑定服务包
     */
    @GetMapping("/removeBindProductStok")
    public RestResponse removeBindProductStok(@RequestParam("productStockId") Integer productStockId) {
        ProductStock productStock = new ProductStock();
        productStock.setId(productStockId);
        productStock.setServicePackId("");
        productStockService.updateById(productStock);
        return RestResponse.ok();
    }

    /**
     * 设备二维码绑定服务包
     */
    @GetMapping("/bindProductStok")
    public RestResponse saveProductSpec(@RequestParam("servicePackId") String servicePackId, @RequestParam("productStockId") Integer productStockId) {
        ProductStock productStock = new ProductStock();
        productStock.setId(productStockId);
        productStock.setServicePackId(servicePackId);
        productStockService.updateById(productStock);
        ServicePack byId = service.getById(servicePackId);
        if (StringUtils.isEmpty(byId.getMpQrCode())) {
            //生成公众号二维码
            StringBuilder sb = new StringBuilder();
            sb.append(servicePackId);
            sb.append(CommonConstants.VALUE_SEPARATOR);
            WxMpQrCodeTicket wxMpQrCodeTicket = null;
            try {
                wxMpQrCodeTicket = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodeCreateLastTicket(sb.toString());
                String qrCodePictureUrl = WxMpConfiguration.getWxMpService().getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
                byId.setMpQrCode(qrCodePictureUrl);
                service.updateById(byId);
            } catch (WxErrorException e) {
                e.printStackTrace();
            }

        }
        return RestResponse.ok();
    }


    /**
     * 生成规格值
     */
    @PostMapping("/saveSale")
    public RestResponse saveSale(@RequestBody ServicePack servicePack) {
        List<SaleSpec> saleSpecs = servicePack.getSaleSpec();
        List<SaleSpec> saleSpecList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(saleSpecs)) {
            for (SaleSpec saleSpec : saleSpecs) {
                if (!StringUtils.isEmpty(saleSpec.getSaleSpecDescs())) {
                    saleSpec.setServicePackId(servicePack.getId());
                    saleSpecList.add(saleSpec);
                }


            }

            saleSpecService.saveBatch(saleSpecList);

            for (SaleSpec saleSpec : saleSpecList) {
                List<SaleSpecDesc> saleSpecDescs1 = saleSpec.getSaleSpecDescs();
                if (!CollectionUtils.isEmpty(saleSpecDescs1)) {
                    for (SaleSpecDesc saleSpecDesc : saleSpecDescs1) {
                        saleSpecDesc.setSaleSpecId(saleSpec.getId());
                    }
                    saleSpecDescService.saveBatch(saleSpecDescs1);
                }
            }

        }
        Collections.sort(saleSpecList);
        return RestResponse.ok(saleSpecList);
    }


    /**
     * 添加服务包
     *
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody ServicePack servicePack) {
        //添加修改记录
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(servicePack.getId() + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("创建服务包");
        operationRecordService.save(operationRecord);


        User byId = userService.getById(SecurityUtils.getUser().getId());
        servicePack.setDeptId(byId.getDeptId());
        servicePack.setDeptIdList(byId.getDeptId() + "");
        Dept byId1 = deptService.getById(byId.getDeptId());

        servicePack.setDeptName(byId1.getName());
        servicePack.setCreateTime(LocalDateTime.now());
        servicePack.setCreateUserId(byId.getId());
        service.save(servicePack);
        List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
        if (!CollectionUtils.isEmpty(servicePackProductPics)) {
            for (ServicePackProductPic servicePackProductPic : servicePackProductPics) {
                servicePackProductPic.setServicePackId(servicePack.getId());
            }
            servicePackProductPicService.saveBatch(servicePackProductPics);
        }
        //回收规则
        List<RecyclingRule> recyclingRuleList = servicePack.getRecyclingRuleList();
        if (!CollectionUtils.isEmpty(recyclingRuleList)) {
            for (RecyclingRule recyclingRule : recyclingRuleList) {
                recyclingRule.setServicePackId(servicePack.getId());
            }
            recyclingRuleService.saveBatch(recyclingRuleList);
        }
        //续租规则
        List<RentRule> rentRuleList = servicePack.getRentRuleList();
        if (!CollectionUtils.isEmpty(rentRuleList)) {
            for (RentRule rentRule : rentRuleList) {
                rentRule.setServicePackId(servicePack.getId());
            }
            rentRuleService.saveBatch(rentRuleList);
        }
        //添加病种
        List<Diseases> diseasesList = servicePack.getDiseasesList();
        if (!CollectionUtils.isEmpty(diseasesList)) {
            List<Integer> diseasesIds = diseasesList.stream().map(Diseases::getId)
                    .collect(Collectors.toList());
            List<ServicePackDiseases> servicePackDiseases = new ArrayList<>();
            for (Integer diseasesId : diseasesIds) {
                ServicePackDiseases diseases = new ServicePackDiseases();
                diseases.setDiseasesId(diseasesId);
                diseases.setServicePackId(servicePack.getId());
                servicePackDiseases.add(diseases);
            }
            servicePackDiseasesService.saveBatch(servicePackDiseases);

        }
        //添加规格
        List<SaleSpec> saleSpecs = servicePack.getSaleSpec();

        if (!CollectionUtils.isEmpty(saleSpecs)) {
            for (SaleSpec saleSpec : saleSpecs) {
                saleSpec.setServicePackId(servicePack.getId());
            }
            saleSpecService.updateBatchById(saleSpecs);


        }
        //生成规格组合值
        List<SaleSpecGroup> saleSpecGroupList = servicePack.getSaleSpecGroupList();
        if (!CollectionUtils.isEmpty(saleSpecGroupList)) {
            for (SaleSpecGroup saleSpecGroup : saleSpecGroupList) {
                saleSpecGroup.setServicePackId(servicePack.getId());
                String saleSpecIds = "";
                String querySaleSpecIds = "";
                List<SaleSpecDesc> saleSpecDescList = saleSpecGroup.getSaleSpecDescList();
                if (!CollectionUtils.isEmpty(saleSpecDescList)) {
                    for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                        if (StringUtils.isEmpty(saleSpecIds)) {
                            saleSpecIds = saleSpecDesc.getId() + "";
                        } else {
                            saleSpecIds = saleSpecIds + "," + saleSpecDesc.getId();
                        }

                        if (StringUtils.isEmpty(querySaleSpecIds)) {
                            querySaleSpecIds = saleSpecDesc.getId() + "";
                        } else {
                            querySaleSpecIds = querySaleSpecIds + saleSpecDesc.getId();
                        }
                    }

                }
                saleSpecGroup.setSaleSpecIds(saleSpecIds);
                querySaleSpecIds = querySaleSpecIds.chars()        // IntStream
                        .sorted()
                        .collect(StringBuilder::new,
                                StringBuilder::appendCodePoint,
                                StringBuilder::append)
                        .toString();
                saleSpecGroup.setQuerySaleSpecIds(querySaleSpecIds);
            }

            saleSpecGroupService.saveBatch(saleSpecGroupList);
        }

        //服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePack.getServicePackageInfos();
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                servicePackageInfo.setServicePackageId(servicePack.getId());
                List<Integer> doctorTeamIds = servicePackageInfo.getDoctorTeamIds();
                if (!CollectionUtils.isEmpty(doctorTeamIds)) {
                    String doctorTeamId = "";
                    for (Integer id : doctorTeamIds) {
                        if (StringUtils.isEmpty(doctorTeamId)) {
                            doctorTeamId = id + "";
                        } else {
                            doctorTeamId = doctorTeamId + "," + id;

                        }
                    }
                    servicePackageInfo.setDoctorTeamId(doctorTeamId);
                }
            }
            servicePackageInfoService.saveBatch(servicePackageInfos);
        }
        //服务详情
        List<ServicePackDetail> servicePackDetails = servicePack.getServicePackDetails();
        if (!CollectionUtils.isEmpty(servicePackDetails)) {
            for (ServicePackDetail servicePackDetail : servicePackDetails) {
                servicePackDetail.setServicePackId(servicePack.getId());
            }
            servicePackDetailService.saveBatch(servicePackDetails);
        }
        //添加服务简介
        List<Introduction> introductions = servicePack.getIntroductions();
        if (!CollectionUtils.isEmpty(introductions)) {
            for (Introduction introduction : introductions) {
                introduction.setServicePackId(servicePack.getId());
            }
            introductionService.saveBatch(introductions);
        }
        return RestResponse.ok();
    }

    /**
     * 获取所有属性，包括父类
     *
     * @param object
     * @return
     */
    public static Field[] getAllFields(Object object) {
        Class clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public static StringBuilder compareContractServicePack(ServicePack sign, ServicePack existSign) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Field[] fields = getAllFields(sign);
            for (int j = 0; j < fields.length; j++) {
                Field field = fields[j];
                field.setAccessible(true);
                // 字段值
                if (field.get(sign) != null && fields[j].get(existSign) != null && !field.get(sign).equals(fields[j].get(existSign))) {


                    stringBuilder.append(fields[j].getName() + "字段从" + fields[j].get(existSign) + "修改为" + field.get(sign) + ",");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    public static StringBuilder compareContractServicePackageInfo(ServicePackageInfo sign, ServicePackageInfo existSign) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Field[] fields = getAllFields(sign);
            for (int j = 0; j < fields.length; j++) {
                Field field = fields[j];
                field.setAccessible(true);
                // 字段值
                if (field.get(sign) != null && fields[j].get(existSign) != null && !field.get(sign).equals(fields[j].get(existSign))) {


                    stringBuilder.append(fields[j].getName() + "字段从" + fields[j].get(existSign) + "修改为" + field.get(sign) + ",");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    /**
     * 编辑服务包
     *
     * @return
     */
    @PostMapping("/edit")
    public RestResponse edit(@RequestBody ServicePack servicePack) {
        //添加修改记录
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(servicePack.getId() + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("修改服务包");
        operationRecordService.save(operationRecord);


        servicePackProductPicService.remove(new QueryWrapper<ServicePackProductPic>().lambda().eq(ServicePackProductPic::getServicePackId, servicePack.getId()));
        service.updateById(servicePack);

        List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
        if (!CollectionUtils.isEmpty(servicePackProductPics)) {
            for (ServicePackProductPic servicePackProductPic : servicePackProductPics) {
                servicePackProductPic.setServicePackId(servicePack.getId());
            }
            servicePackProductPicService.saveBatch(servicePackProductPics);
        }

        //回收规则
        List<RecyclingRule> recyclingRuleList = servicePack.getRecyclingRuleList();
        recyclingRuleService.update(Wrappers.<RecyclingRule>lambdaUpdate()
                .eq(RecyclingRule::getServicePackId, servicePack.getId())
                .set(RecyclingRule::getServicePackId, -1)
        );
        recyclingRuleService.remove(new QueryWrapper<RecyclingRule>().lambda().eq(RecyclingRule::getServicePackId, servicePack.getId()));
        if (!CollectionUtils.isEmpty(recyclingRuleList)) {
            for (RecyclingRule recyclingRule : recyclingRuleList) {
                recyclingRule.setServicePackId(servicePack.getId());
            }
            recyclingRuleService.saveBatch(recyclingRuleList);
        }
        //续租规则
        rentRuleService.remove(new QueryWrapper<RentRule>().lambda().eq(RentRule::getServicePackId, servicePack.getId()));
        List<RentRule> rentRuleList = servicePack.getRentRuleList();
        if (!CollectionUtils.isEmpty(rentRuleList)) {
            for (RentRule rentRule : rentRuleList) {
                rentRule.setServicePackId(servicePack.getId());
            }
            rentRuleService.saveBatch(rentRuleList);
        }

//
//        //添加病种
//        List<Diseases> diseasesList = servicePack.getDiseasesList();
//        servicePackDiseasesService.remove(new QueryWrapper<ServicePackDiseases>().lambda()
//                .eq(ServicePackDiseases::getServicePackId, servicePack.getId()));
//        if (!CollectionUtils.isEmpty(diseasesList)) {
//            List<Integer> diseasesIds = diseasesList.stream().map(Diseases::getId)
//                    .collect(Collectors.toList());
//            List<ServicePackDiseases> servicePackDiseases = new ArrayList<>();
//            for (Integer diseasesId : diseasesIds) {
//                ServicePackDiseases diseases = new ServicePackDiseases();
//                diseases.setDiseasesId(diseasesId);
//                diseases.setServicePackId(servicePack.getId());
//                servicePackDiseases.add(diseases);
//            }
//            servicePackDiseasesService.saveBatch(servicePackDiseases);
//
//        }

        //添加销售规格
        List<SaleSpec> saleSpecs = servicePack.getSaleSpec();

        if (!CollectionUtils.isEmpty(saleSpecs)) {
            List<SaleSpec> saleSpecList = saleSpecService.list(new QueryWrapper<SaleSpec>().lambda()
                    .eq(SaleSpec::getServicePackId, servicePack.getId()));

            for (SaleSpec saleSpec : saleSpecs) {

                saleSpec.setServicePackId(servicePack.getId());

            }
            List<Integer> saleSpecIds = saleSpecs.stream().map(SaleSpec::getId)
                    .collect(Collectors.toList());
            saleSpecService.updateBatchById(saleSpecs);
            List<Integer> removeIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(saleSpecList)) {
                for (SaleSpec saleSpec : saleSpecList) {
                    if (!saleSpecIds.contains(saleSpec.getId())) {
                        removeIds.add(saleSpec.getId());
                    }
                }

            }
            if (!CollectionUtils.isEmpty(removeIds)) {
                saleSpecService.removeByIds(removeIds);
            }
        } else {
            saleSpecService.remove(new QueryWrapper<SaleSpec>().lambda()
                    .eq(SaleSpec::getServicePackId, servicePack.getId()));
        }
        //编辑规格组合值
        saleSpecGroupService.remove(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getServicePackId, servicePack.getId()));
        List<SaleSpecGroup> saleSpecGroupList = servicePack.getSaleSpecGroupList();
        if (!CollectionUtils.isEmpty(saleSpecGroupList)) {
            for (SaleSpecGroup saleSpecGroup : saleSpecGroupList) {
                saleSpecGroup.setServicePackId(servicePack.getId());
                String saleSpecIds = "";
                String querySaleSpecIds = "";
                List<SaleSpecDesc> saleSpecDescList = saleSpecGroup.getSaleSpecDescList();
                if (!CollectionUtils.isEmpty(saleSpecDescList)) {
                    for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                        if (StringUtils.isEmpty(saleSpecIds)) {
                            saleSpecIds = saleSpecDesc.getId() + "";
                        } else {
                            saleSpecIds = saleSpecIds + "," + saleSpecDesc.getId();
                        }

                        if (StringUtils.isEmpty(querySaleSpecIds)) {
                            querySaleSpecIds = saleSpecDesc.getId() + "";
                        } else {
                            querySaleSpecIds = querySaleSpecIds + saleSpecDesc.getId();
                        }
                    }

                }
                saleSpecGroup.setSaleSpecIds(saleSpecIds);
                querySaleSpecIds = querySaleSpecIds.chars()        // IntStream
                        .sorted()
                        .collect(StringBuilder::new,
                                StringBuilder::appendCodePoint,
                                StringBuilder::append)
                        .toString();
                saleSpecGroup.setQuerySaleSpecIds(querySaleSpecIds);
            }

            saleSpecGroupService.saveBatch(saleSpecGroupList);
        }


        //servicePackageInfoService.remove(new QueryWrapper<ServicePackageInfo>().lambda().eq(ServicePackageInfo::getServicePackageId, servicePack.getId()));
        //服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePack.getServicePackageInfos();
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {


                servicePackageInfo.setServicePackageId(servicePack.getId());
                List<Integer> doctorTeamIds = servicePackageInfo.getDoctorTeamIds();
                if (!CollectionUtils.isEmpty(doctorTeamIds)) {
                    String doctorTeamId = "";
                    for (Integer id : doctorTeamIds) {
                        if (StringUtils.isEmpty(doctorTeamId)) {
                            doctorTeamId = id + "";
                        } else {
                            doctorTeamId = doctorTeamId + "," + id;

                        }
                    }
                    servicePackageInfo.setDoctorTeamId(doctorTeamId);
                } else {
                    servicePackageInfo.setDoctorTeamId(null);

                }

            }
            servicePackageInfoService.saveOrUpdateBatch(servicePackageInfos);
        }

        //服务详情
        List<ServicePackDetail> servicePackDetails = servicePack.getServicePackDetails();
        if (!CollectionUtils.isEmpty(servicePackDetails)) {
            List<ServicePackDetail> servicePackDetailList = servicePackDetailService.list(new QueryWrapper<ServicePackDetail>().lambda()
                    .eq(ServicePackDetail::getServicePackId, servicePack.getId()));
            if (!CollectionUtils.isEmpty(servicePackDetailList)) {
                List<Integer> servicePackDetailIds = servicePackDetails.stream().map(ServicePackDetail::getId)
                        .collect(Collectors.toList());
                List<Integer> removeDetailIds = new ArrayList<>();
                for (ServicePackDetail servicePackDetail : servicePackDetailList) {
                    if (!servicePackDetailIds.contains(servicePackDetail.getId())) {
                        removeDetailIds.add(servicePackDetail.getId());
                    }
                }
                if (!CollectionUtils.isEmpty(removeDetailIds)) {
                    servicePackDetailService.removeByIds(removeDetailIds);
                }
            }


            for (ServicePackDetail servicePackDetail : servicePackDetails) {
                servicePackDetail.setServicePackId(servicePack.getId());
            }
            servicePackDetailService.saveOrUpdateBatch(servicePackDetails);
        } else {
            servicePackDetailService.remove(new QueryWrapper<ServicePackDetail>().lambda()
                    .eq(ServicePackDetail::getServicePackId, servicePack.getId()));
        }
        operationRecordService.save(operationRecord);
        return RestResponse.ok();
    }

    /**
     * 查询规格组合值根据条件筛选
     */
    @PostMapping("/listSpecGroup")
    public RestResponse listSpecGroup(@RequestBody SaleSpecGroup saleSpecGroup) {
        LambdaQueryWrapper<SaleSpecGroup> eq = new QueryWrapper<SaleSpecGroup>().lambda();
        if (saleSpecGroup.getPrice() != null) {
            eq.eq(SaleSpecGroup::getPrice, saleSpecGroup.getPrice());
        }
        if (saleSpecGroup.getRecovery() != null) {
            eq.eq(SaleSpecGroup::getRecovery, saleSpecGroup.getRecovery());
        }
        if (saleSpecGroup.getStock() != null) {
            eq.eq(SaleSpecGroup::getStock, saleSpecGroup.getStock());
        }
        if (saleSpecGroup.getRemark() != null) {
            eq.eq(SaleSpecGroup::getRemark, saleSpecGroup.getRemark());
        }
        if (saleSpecGroup.getStatus() != null) {
            eq.eq(SaleSpecGroup::getStatus, saleSpecGroup.getStatus());
        }
        if (saleSpecGroup.getRecoveryPrice() != null) {
            eq.eq(SaleSpecGroup::getRecoveryPrice, saleSpecGroup.getRecoveryPrice());
        }
        if (saleSpecGroup.getServicePackId() != null) {
            eq.eq(SaleSpecGroup::getServicePackId, saleSpecGroup.getServicePackId());
        }
        //规格值查询

        List<SaleSpecGroup> list = saleSpecGroupService.list(eq);
        return RestResponse.ok(list);
    }

    /**
     * 服务包列表查询分页
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped(@RequestParam(value = "startTime", required = false) String startTime, @RequestParam(value = "endTime", required = false) String endTime) {
        Page<ServicePack> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            queryWrapper.le("service_pack.create_time", endTime);
            queryWrapper.ge("service_pack.create_time", startTime);
        }
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
        if (!aBoolean) {
            User userDept = userService.getById(SecurityUtils.getUser().getId());

            queryWrapper.eq("service_pack.dept_id", userDept.getDeptId());

        }
        queryWrapper.eq("service_pack.del_status", 1);
        IPage<ServicePack> servicePackIPage = service.pageScoped(aBoolean, page, queryWrapper);
        List<ServicePack> records = servicePackIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> deptIds = records.stream().map(ServicePack::getDeptId)
                    .collect(Collectors.toList());
            List<Dept> depts = (List<Dept>) deptService.listByIds(deptIds);
            Map<Integer, Dept> deptMap = depts.stream()
                    .collect(Collectors.toMap(Dept::getId, t -> t));
            for (ServicePack servicePack : records) {
                Dept dept = deptMap.get(servicePack.getDeptId());
                if (dept != null) {
                    servicePack.setDeptName(dept.getName());
                }
            }
        }

        return RestResponse.ok(servicePackIPage);
    }

    /**
     * 添加所属代理商
     */
    @GetMapping("/updateDept")
    public RestResponse addDept(@RequestParam("servicePackId") Integer servicePackId, @RequestParam("deptId") Integer deptId) {
        ServicePack servicePack = service.getById(servicePackId);
        servicePack.setDeptId(deptId);

        service.updateById(servicePack);
        return RestResponse.ok();
    }

    /**
     * 删除所属代理商
     */
    @GetMapping("/deleteDept")
    public RestResponse deleteDept(@RequestParam("servicePackId") Integer servicePackId, @RequestParam("deptId") Integer deptId) {
        ServicePack servicePack = service.getById(servicePackId);
        String deptIdList = servicePack.getDeptIdList();
        List<String> split = Arrays.asList(deptIdList.split("/"));
        String newDeptIds = "";
        for (String deptIdSplit : split) {
            if (!deptIdSplit.equals(deptId + "")) {
                if (StringUtils.isEmpty(newDeptIds)) {
                    newDeptIds = deptIdSplit;
                } else {
                    newDeptIds = newDeptIds + "/" + deptIdSplit;
                }
            }


        }
        servicePack.setDeptIdList(newDeptIds);
        service.updateById(servicePack);
        return RestResponse.ok();
    }

    /**
     * 查询所属代理商
     */
    @GetMapping("/getDept")
    public RestResponse getDept(@RequestParam("servicePackId") Integer servicePackId) {
        ServicePack servicePack = service.getById(servicePackId);
        String deptIdList = servicePack.getDeptIdList();
        List<String> deptIds = Arrays.asList(deptIdList.split("/"));
        List<Dept> depts = (List<Dept>) deptService.listByIds(deptIds);

        return RestResponse.ok(depts);
    }


    /**
     * 服务包列表查询不分页
     *
     * @return
     */
    @GetMapping("/listScoped")
    public RestResponse listScoped() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        List<ServicePack> servicePackIPage = service.listScoped(queryWrapper);

        return RestResponse.ok(servicePackIPage);
    }

    /**
     * 查询服务包所在的医院
     */
    @GetMapping("/getHospitalInfoByServicePackId")
    public RestResponse getHospitalInfoByServicePackId(@RequestParam(value = "servicePackId", required = false) Integer servicePackId) {
        if (servicePackId == null) {
            return RestResponse.ok();
        }
        ServicePack byId = service.getById(servicePackId);
        Integer hospitalId = byId.getHospitalId();
        if (hospitalId != null) {
            HospitalInfo hospitalInfo = hospitalInfoService.getById(hospitalId);
            return RestResponse.ok(hospitalInfo);
        }
        return RestResponse.ok();
    }

    /**
     * 业务员和服务包绑定
     */
    @PostMapping("/bindServicePack")
    public RestResponse bindServicePack(@RequestBody UserQrCodeParam userQrCodeParam) {
        userQrCodeService.remove(new QueryWrapper<UserQrCode>().lambda()
                .eq(UserQrCode::getUserId, userQrCodeParam.getUserId()));
        List<String> qrCodeIds = userQrCodeParam.getQrCodeIds();
        String userId = userQrCodeParam.getUserId();
        if (CollectionUtils.isEmpty(qrCodeIds)) {
            return RestResponse.ok();
        }
        if (StringUtils.isEmpty(userId)) {
            return RestResponse.ok();
        }
        qrCodeIds = qrCodeIds.stream().distinct().collect(Collectors.toList());

        List<UserQrCode> userQrCodeList = new ArrayList<>();
        for (String qrCodeId : qrCodeIds) {
            UserQrCode userQrCode = new UserQrCode();
            userQrCode.setUserId(userId);
            userQrCode.setQrCodeId(qrCodeId);
            userQrCodeList.add(userQrCode);
        }
        userQrCodeService.saveBatch(userQrCodeList);
        return RestResponse.ok();
    }

    /**
     * 查询业务员已绑定的服务包
     */
    @GetMapping("/queryBndServicePack")
    public RestResponse queryBndServicePack(@RequestParam("userId") Integer userId) {
        List<UserQrCode> userQrCodeList = userQrCodeService.list(new QueryWrapper<UserQrCode>().lambda()
                .eq(UserQrCode::getUserId, userId));
        if (!CollectionUtils.isEmpty(userQrCodeList)) {
            List<String> qrCodeIds = userQrCodeList.stream().map(UserQrCode::getQrCodeId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) service.listByIds(qrCodeIds);
            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));
            for (UserQrCode userQrCode : userQrCodeList) {
                ServicePack servicePack = servicePackMap.get(Integer.parseInt(userQrCode.getQrCodeId()));
                if (servicePack != null) {
                    userQrCode.setServicePackName(servicePack.getName());
                }
            }
        }

        return RestResponse.ok(userQrCodeList);
    }

    /**
     * 根据服务包id查询所对应的 医生团队
     *
     * @return
     */
    @GetMapping("/queryDoctorTeamByServicePackId")
    public RestResponse queryDoctorTeamByServicePackId(@RequestParam(value = "servicePackId") Integer servicePackId) {
        //查询服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>()
                .lambda().eq(ServicePackageInfo::getServicePackageId, servicePackId));
        //查询服务的医生团队
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            List<Integer> teamIds = new ArrayList<>();
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                String doctorTeamId = servicePackageInfo.getDoctorTeamId();
                List<Integer> teamId = new ArrayList<>();
                if (!StringUtils.isEmpty(doctorTeamId)) {
                    String[] split = doctorTeamId.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String n = split[i];
                        teamIds.add(Integer.parseInt(n));
                        teamId.add(Integer.parseInt(n));
                    }
                }
            }
            ServicePack servicePack = service.getById(servicePackId);

            if (!CollectionUtils.isEmpty(teamIds)) {
                List<DoctorTeam> doctorTeams = doctorTeamService.list(new QueryWrapper<DoctorTeam>().lambda()
                        .in(DoctorTeam::getId, teamIds)
                        .eq(DoctorTeam::getHospitalId, servicePack.getHospitalId()));

                List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda().in(DoctorTeamPeople::getTeamId, teamIds));
                if (!CollectionUtils.isEmpty(list)) {
                    List<Integer> userIds = list.stream().map(DoctorTeamPeople::getUserId)
                            .collect(Collectors.toList());
                    List<User> users = (List<User>) userService.listByIds(userIds);
                    Map<Integer, User> userMap = users.stream()
                            .collect(Collectors.toMap(User::getId, t -> t));
                    for (DoctorTeamPeople doctorTeamPeople : list) {
                        if (userMap.get(doctorTeamPeople.getUserId()) != null) {
                            doctorTeamPeople.setUserName(userMap.get(doctorTeamPeople.getUserId()).getNickname());
                            doctorTeamPeople.setAvatar(userMap.get(doctorTeamPeople.getUserId()).getAvatar());
                        }
                    }

                    Map<Integer, List<DoctorTeamPeople>> doctorTeamPeopleMap = list.stream()
                            .collect(Collectors.groupingBy(DoctorTeamPeople::getTeamId));
                    for (DoctorTeam doctorTeam : doctorTeams) {
                        doctorTeam.setDoctorTeamPeopleList(doctorTeamPeopleMap.get(doctorTeam.getId()));
                        String pingYin = getPingYin(doctorTeam.getName());
                        doctorTeam.setPingYin(pingYin);
                    }
                }
                Collections.sort(doctorTeams);
                return RestResponse.ok(doctorTeams);
            }

        }

        return RestResponse.ok(new ArrayList<>());
    }

    /**
     * 根据服务包id查询所对应的病种
     *
     * @return
     */
    @GetMapping("/queryDiseasesByServicePackId")
    public RestResponse queryDiseasesByServicePackId(@RequestParam(value = "servicePackId") Integer servicePackId) {
        //查询病种
        List<ServicePackDiseases> servicePackDiseasesList = servicePackDiseasesService.list(new QueryWrapper<ServicePackDiseases>().lambda().eq(ServicePackDiseases::getServicePackId, servicePackId));
        if (!CollectionUtils.isEmpty(servicePackDiseasesList)) {
            List<Integer> diseasesIds = servicePackDiseasesList.stream().map(ServicePackDiseases::getDiseasesId)
                    .collect(Collectors.toList());
            List<Diseases> diseases = (List<Diseases>) diseasesService.listByIds(diseasesIds);
            return RestResponse.ok(diseases);
        } else {
            return RestResponse.ok(new ArrayList<>());
        }

    }

    /**
     * 根据医院查询服务包
     */
    @GetMapping("/getByIdHospitalInfo")
    public RestResponse getByIdHospitalInfo(@RequestParam("id") Integer id) {

        User user = userService.getById(SecurityUtils.getUser().getId());
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(user.getId());
        if (aBoolean) {
            return RestResponse.ok(service.list(new QueryWrapper<ServicePack>().lambda().eq(ServicePack::getHospitalId, id)));
        } else {
            return RestResponse.ok(service.list(new QueryWrapper<ServicePack>().lambda().eq(ServicePack::getHospitalId, id).eq(ServicePack::getDeptId, user.getDeptId())));

        }
    }

    /**
     * 服务包详情查询
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {
        ServicePack servicePack = service.getById(id);
        //回收规则
        List<RentRule> rentRuleList = rentRuleService.list(new QueryWrapper<RentRule>().lambda().eq(RentRule::getServicePackId, id));
        servicePack.setRentRuleList(rentRuleList);
        //续租规则
        List<RecyclingRule> recyclingRuleList = recyclingRuleService.list(new QueryWrapper<RecyclingRule>().lambda().eq(RecyclingRule::getServicePackId, id));
        servicePack.setRecyclingRuleList(recyclingRuleList);


        //查询病种
        List<ServicePackDiseases> servicePackDiseasesList = servicePackDiseasesService.list(new QueryWrapper<ServicePackDiseases>().lambda().eq(ServicePackDiseases::getServicePackId, id));
        if (!CollectionUtils.isEmpty(servicePackDiseasesList)) {
            List<Integer> diseasesIds = servicePackDiseasesList.stream().map(ServicePackDiseases::getDiseasesId)
                    .collect(Collectors.toList());
            List<Diseases> diseases = (List<Diseases>) diseasesService.listByIds(diseasesIds);
            servicePack.setDiseasesList(diseases);
        } else {
            servicePack.setDiseasesList(new ArrayList<>());
        }
        //查询产品图片
        List<ServicePackProductPic> list = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>()
                .lambda().eq(ServicePackProductPic::getServicePackId, id));
        if (!CollectionUtils.isEmpty(list)) {
            Map<Integer, List<ServicePackProductPic>> servicePackProductPicMap = list.stream()
                    .collect(Collectors.groupingBy(ServicePackProductPic::getType));
            List<ServicePackProductPic> servicePackProductPics = servicePackProductPicMap.get(0);
            if (CollectionUtils.isEmpty(servicePackProductPics)) {
                servicePack.setServicePackProductPics(new ArrayList<>());
            } else {
                servicePack.setServicePackProductPics(servicePackProductPics);
            }
            List<ServicePackProductPic> servicePackProductPics1 = servicePackProductPicMap.get(1);
            if (CollectionUtils.isEmpty(servicePackProductPics1)) {
                servicePack.setServicePackProductPicsBuy(new ArrayList<>());
            } else {
                servicePack.setServicePackProductPicsBuy(servicePackProductPics1);
            }

        } else {
            servicePack.setServicePackProductPics(new ArrayList<>());

            servicePack.setServicePackProductPicsBuy(new ArrayList<>());
        }

        //查询规格信息
        List<SaleSpec> saleSpecs = saleSpecService.list(new QueryWrapper<SaleSpec>()
                .lambda().eq(SaleSpec::getServicePackId, id));
        List<SaleSpecDesc> saleSpecDescList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(saleSpecs)) {
            //查询规格值
            List<Integer> saleSpecIds = saleSpecs.stream().map(SaleSpec::getId)
                    .collect(Collectors.toList());
            saleSpecDescList = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .in(SaleSpecDesc::getSaleSpecId, saleSpecIds));
            if (!CollectionUtils.isEmpty(saleSpecDescList)) {

                Map<Integer, List<SaleSpecDesc>> saleSpecDescMap = saleSpecDescList.stream()
                        .collect(Collectors.groupingBy(SaleSpecDesc::getSaleSpecId));
                for (SaleSpec saleSpec : saleSpecs) {
                    saleSpec.setSaleSpecDescs(saleSpecDescMap.get(saleSpec.getId()));
                }
            }
        }
        //查询规格组合值
        List<SaleSpecGroup> saleSpecGroupList = saleSpecGroupService.list(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getServicePackId, id));
        if (!CollectionUtils.isEmpty(saleSpecGroupList)) {

            for (SaleSpecGroup saleSpecGroup : saleSpecGroupList) {
                String saleSpecIds = saleSpecGroup.getSaleSpecIds();
                String[] split = saleSpecIds.split(",");
                List<String> saleSpecDescIds = Arrays.asList(split);
                Map<Integer, SaleSpecDesc> saleSpecDescMap = saleSpecDescList.stream()
                        .collect(Collectors.toMap(SaleSpecDesc::getId, t -> t));
                List<SaleSpecDesc> specDescList = new ArrayList<>();
                for (String saleSpecDescId : saleSpecDescIds) {
                    specDescList.add(saleSpecDescMap.get(Integer.parseInt(saleSpecDescId)));

                }
                saleSpecGroup.setSaleSpecDescList(specDescList);
            }
        }
        servicePack.setSaleSpecGroupList(saleSpecGroupList);
        Collections.sort(saleSpecs);
        servicePack.setSaleSpec(saleSpecs);
        //查询服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>()
                .lambda().eq(ServicePackageInfo::getServicePackageId, id));
        //查询服务的医生团队
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            List<Integer> teamIds = new ArrayList<>();
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                String doctorTeamId = servicePackageInfo.getDoctorTeamId();
                List<Integer> teamId = new ArrayList<>();
                if (!StringUtils.isEmpty(doctorTeamId)) {
                    String[] split = doctorTeamId.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String n = split[i];
                        teamIds.add(Integer.parseInt(n));
                        teamId.add(Integer.parseInt(n));
                    }
                }
                servicePackageInfo.setDoctorTeamIds(teamId);
            }
            if (!CollectionUtils.isEmpty(teamIds)) {
                List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
                for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                    String doctorTeamId = servicePackageInfo.getDoctorTeamId();
                    List<DoctorTeam> doctorTeamList = servicePackageInfo.getDoctorTeamList();
                    for (DoctorTeam doctorTeam : doctorTeams) {
                        if (doctorTeamId.contains(doctorTeam.getId() + "")) {
                            if (CollectionUtils.isEmpty(doctorTeamList)) {
                                doctorTeamList = new ArrayList<>();
                            }
                            doctorTeamList.add(doctorTeam);
                        }
                    }
                    servicePackageInfo.setDoctorTeamList(doctorTeamList);
                }
            }

        }

        servicePack.setServicePackageInfos(servicePackageInfos);
        //查询服务详情
        List<ServicePackDetail> servicePackDetails = servicePackDetailService.list(new QueryWrapper<ServicePackDetail>()
                .lambda().eq(ServicePackDetail::getServicePackId, id));
        servicePack.setServicePackDetails(servicePackDetails);

        //查询服务简介
        List<Introduction> introductions = introductionService.list(new QueryWrapper<Introduction>().lambda()
                .eq(Introduction::getServicePackId, id));
        servicePack.setIntroductions(introductions);

        return RestResponse.ok(servicePack);
    }

    /**
     * 查询可选子规格值
     */
    @PostMapping("/querySpecSelect")
    public RestResponse querySpecSelect(@RequestBody QuerySpecSelect param) {
        List<String> specDescIds = new ArrayList<>();
        List<Integer> specDescId1 = param.getSpecDescId();
        if (CollectionUtils.isEmpty(param.getSpecDescId())) {
            List<SaleSpec> list = saleSpecService.list(new QueryWrapper<SaleSpec>().lambda()
                    .eq(SaleSpec::getServicePackId, param.getServicePackId()));
            List<Integer> saleSpecIds = list.stream().map(SaleSpec::getId)
                    .collect(Collectors.toList());
            List<SaleSpecDesc> saleSpecDescs = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .in(SaleSpecDesc::getSaleSpecId, saleSpecIds));
            for (SaleSpecDesc saleSpecDesc : saleSpecDescs) {
                specDescIds.add(saleSpecDesc.getId() + "");
            }
            return RestResponse.ok(specDescIds);
        }
        LambdaQueryWrapper<SaleSpecGroup> wrapper = new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getServicePackId, param.getServicePackId())
                .gt(SaleSpecGroup::getStock, 0);
//        for (Integer str : param.getSpecDescId()) {
//            wrapper.and(wq0 -> wq0.like(SaleSpecGroup::getSaleSpecIds, str));
//        }


        List<SaleSpecGroup> list = saleSpecGroupService.list(wrapper);


        if (!CollectionUtils.isEmpty(list)) {
            Collections.sort(specDescId1);
            for (Integer paramDescId : specDescId1) {
                List<String> thisSpecDescIds = new ArrayList<>();
                for (SaleSpecGroup saleSpecGroup : list) {
                    if (saleSpecGroup.getSaleSpecIds().indexOf(paramDescId + "") != -1) {
                        String saleSpecIds = saleSpecGroup.getSaleSpecIds();
                        String[] split = saleSpecIds.split(",");
                        List<String> ids = Arrays.asList(split);
                        thisSpecDescIds.addAll(ids);
                    }
                }

                if (!CollectionUtils.isEmpty(specDescIds)) {
                    for (String id : thisSpecDescIds) {
                        if (specDescIds.contains(id)) {
                            specDescIds.add(id);
                        }
                    }
                } else {
                    specDescIds.addAll(thisSpecDescIds);

                }
            }
//            for (SaleSpecGroup saleSpecGroup : list) {
//                String saleSpecIds = saleSpecGroup.getSaleSpecIds();
//                String[] split = saleSpecIds.split(",");
//                List<String> ids = Arrays.asList(split);
//                specDescIds.addAll(ids);
//            }
            SaleSpecDesc saleSpecDescs1 = saleSpecDescService.getById(specDescId1.get(0));


            SaleSpec saleSpec = saleSpecService.getById(saleSpecDescs1.getSaleSpecId());
            List<SaleSpecDesc> list1 = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .eq(SaleSpecDesc::getSaleSpecId, saleSpec.getId()));
            for (SaleSpecDesc saleSpecDesc : list1) {
                specDescIds.add(saleSpecDesc.getId() + "");
            }
            Set<String> set = new HashSet<>(specDescIds);
            return RestResponse.ok(set);
        }
        return RestResponse.ok();


    }

    public static String getPingYin(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return "#";
        }
        String name = userName.substring(0, 1);
        char[] charArray = name.toCharArray();
        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 设置大小写格式
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        // 设置声调格式：
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < charArray.length; i++) {
            // 匹配中文,非中文转换会转换成null
            if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
                String[] hanyuPinyinStringArray = new String[0];
                try {
                    hanyuPinyinStringArray = PinyinHelper
                            .toHanyuPinyinStringArray(charArray[i], defaultFormat);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
                if (hanyuPinyinStringArray != null) {
                    pinyin.append(hanyuPinyinStringArray[0].charAt(0));
                }
            }
        }
        return StringUtils.isEmpty(pinyin.toString()) ? "#" : pinyin.toString();
    }


    /**
     * 删除服务包
     *
     * @return
     */
    @GetMapping("/deleteServicePackById")
    public RestResponse deleteServicePackById(@RequestParam("id") Integer id) {
        ServicePack servicePack = new ServicePack();
        servicePack.setId(id);
        servicePack.setDelStatus(2);
        service.updateById(servicePack);

        return RestResponse.ok();


    }

    /**
     * 查询服务简介富文本
     *
     * @return
     */
    @GetMapping("/getServicePackById")
    public RestResponse getServicePackById(@RequestParam("id") Integer id) {
        QueryWrapper<ServicePack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("introductions_content", "id").eq("id", id);
        return RestResponse.ok(service.getBaseMapper().selectOne(queryWrapper));


    }

    /**
     * 查询服务包 续租规则
     *
     * @return
     */
    @GetMapping("/queryRule")
    public RestResponse queryRule(@RequestParam("id") Integer id) {
        List<RentRule> rentRuleList = rentRuleService.list(new QueryWrapper<RentRule>().lambda().eq(RentRule::getServicePackId, id));
        ServicePack servicePack = service.getById(id);
        servicePack.setRentRuleList(rentRuleList);
        return RestResponse.ok(servicePack);
    }

    /**
     * 复制服务包
     *
     * @return
     */
    @GetMapping("/copyServicePack")
    public RestResponse copyServicePack(@RequestParam("id") Integer id) {
        //添加修改记录
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(id + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("复制服务包");
        operationRecordService.save(operationRecord);

        ServicePack servicePack = service.getById(id);
        servicePack.setName(servicePack.getName() + "复制");
        ServicePack newServicePack = new ServicePack();
        BeanUtils.copyProperties(servicePack, newServicePack, "id");
        newServicePack.setName(servicePack.getName() + "复制");
        newServicePack.setCreateTime(LocalDateTime.now());
        newServicePack.setStatus(1);
        service.save(newServicePack);


        //回收规则
        List<RecyclingRule> recyclingRuleList = recyclingRuleService.list(new QueryWrapper<RecyclingRule>().lambda().eq(RecyclingRule::getServicePackId, id));
        if (!CollectionUtils.isEmpty(recyclingRuleList)) {
            List<RecyclingRule> newRecyclingRules = new ArrayList<>();
            for (RecyclingRule recyclingRule : recyclingRuleList) {
                RecyclingRule newRewRentRule = new RecyclingRule();
                BeanUtils.copyProperties(recyclingRule, newRewRentRule, "id");
                newRewRentRule.setServicePackId(newServicePack.getId());
                newRecyclingRules.add(newRewRentRule);
            }

            recyclingRuleService.saveBatch(newRecyclingRules);
        }

        //续租规则
        List<RentRule> rentRuleList = rentRuleService.list(new QueryWrapper<RentRule>().lambda().eq(RentRule::getServicePackId, id));

        if (!CollectionUtils.isEmpty(rentRuleList)) {
            List<RentRule> newRentRules = new ArrayList<>();
            for (RentRule rentRule : rentRuleList) {
                RentRule newRentRule = new RentRule();
                BeanUtils.copyProperties(rentRule, newRentRule, "id");
                newRentRule.setServicePackId(newServicePack.getId());
                newRentRules.add(newRentRule);
            }

            rentRuleService.saveBatch(newRentRules);
        }


        //查询产品图片
        List<ServicePackProductPic> list = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>()
                .lambda().eq(ServicePackProductPic::getServicePackId, id));
        if (!CollectionUtils.isEmpty(list)) {
            List<ServicePackProductPic> newServicePackProductPics = new ArrayList<>();
            for (ServicePackProductPic servicePackProductPic : list) {
                ServicePackProductPic newServicePackProductPic = new ServicePackProductPic();
                BeanUtils.copyProperties(servicePackProductPic, newServicePackProductPic, "id");
                newServicePackProductPic.setServicePackId(newServicePack.getId());
                newServicePackProductPics.add(newServicePackProductPic);
            }

            servicePackProductPicService.saveBatch(newServicePackProductPics);
        }
        //查询规格信息
        List<SaleSpec> saleSpecs = saleSpecService.list(new QueryWrapper<SaleSpec>()
                .lambda().eq(SaleSpec::getServicePackId, id));
        List<SaleSpecDesc> saleSpecDescList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(saleSpecs)) {
            //查询规格值
            List<Integer> saleSpecIds = saleSpecs.stream().map(SaleSpec::getId)
                    .collect(Collectors.toList());
            saleSpecDescList = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .in(SaleSpecDesc::getSaleSpecId, saleSpecIds));
            List<SaleSpec> newSaleSpecs = new ArrayList<>();

            Map<Integer, List<SaleSpecDesc>> saleSpecDescMap = saleSpecDescList.stream()
                    .collect(Collectors.groupingBy(SaleSpecDesc::getSaleSpecId));


            for (SaleSpec saleSpec : saleSpecs) {
                SaleSpec newSaleSpec = new SaleSpec();
                BeanUtils.copyProperties(saleSpec, newSaleSpec, "id");
                newSaleSpec.setServicePackId(newServicePack.getId());
                newSaleSpec.setOldId(saleSpec.getId());
                newSaleSpecs.add(newSaleSpec);


            }
            saleSpecService.saveBatch(newSaleSpecs);
            List<SaleSpecDesc> newSaleSpecDescs = new ArrayList<>();
            for (SaleSpec saleSpec : newSaleSpecs) {
                List<SaleSpecDesc> saleSpecDescs = saleSpecDescMap.get(saleSpec.getOldId());//规格值
                if (!CollectionUtils.isEmpty(saleSpecDescs)) {

                    for (SaleSpecDesc saleSpecDesc : saleSpecDescs) {
                        SaleSpecDesc newSaleSpecDesc = new SaleSpecDesc();
                        BeanUtils.copyProperties(saleSpecDesc, newSaleSpecDesc, "id");
                        newSaleSpecDesc.setSaleSpecId(saleSpec.getId());
                        newSaleSpecDescs.add(newSaleSpecDesc);
                    }
                }

            }
            if (!CollectionUtils.isEmpty(newSaleSpecDescs)) {
                saleSpecDescService.saveBatch(newSaleSpecDescs);
            }
        }
        //查询规格组合值
        List<SaleSpecGroup> saleSpecGroupList = saleSpecGroupService.list(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getServicePackId, id));
        if (!CollectionUtils.isEmpty(saleSpecGroupList)) {
            List<SaleSpecGroup> newSaleSpecGroups = new ArrayList<>();
            for (SaleSpecGroup saleSpecGroup : saleSpecGroupList) {
                SaleSpecGroup newSaleSpecGroup = new SaleSpecGroup();
                BeanUtils.copyProperties(saleSpecGroup, newSaleSpecGroup, "id");
                newSaleSpecGroup.setServicePackId(newServicePack.getId());
                newSaleSpecGroups.add(newSaleSpecGroup);
            }
            saleSpecGroupService.saveBatch(newSaleSpecGroups);
        }


        //查询服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>()
                .lambda().eq(ServicePackageInfo::getServicePackageId, id));
        //查询服务的医生团队
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            List<ServicePackageInfo> newServicePackageInfos = new ArrayList<>();
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                ServicePackageInfo newServicePackageInfo = new ServicePackageInfo();
                BeanUtils.copyProperties(servicePackageInfo, newServicePackageInfo, "id");
                newServicePackageInfo.setServicePackageId(newServicePack.getId());
                newServicePackageInfos.add(newServicePackageInfo);
            }
            servicePackageInfoService.saveBatch(newServicePackageInfos);
        }

        //查询服务详情
        List<ServicePackDetail> servicePackDetails = servicePackDetailService.list(new QueryWrapper<ServicePackDetail>()
                .lambda().eq(ServicePackDetail::getServicePackId, id));
        if (!CollectionUtils.isEmpty(servicePackDetails)) {
            List<ServicePackDetail> newServicePackDetails = new ArrayList<>();
            for (ServicePackDetail servicePackDetail : servicePackDetails) {
                ServicePackDetail newServicePackDetail = new ServicePackDetail();
                BeanUtils.copyProperties(servicePackDetail, newServicePackDetail, "id");
                newServicePackDetail.setServicePackId(newServicePack.getId());
                newServicePackDetails.add(newServicePackDetail);
            }
            servicePackDetailService.saveBatch(newServicePackDetails);
        }

        //查询服务简介
        List<Introduction> introductions = introductionService.list(new QueryWrapper<Introduction>().lambda()
                .eq(Introduction::getServicePackId, id));
        if (!CollectionUtils.isEmpty(introductions)) {

            List<Introduction> newIntroductions = new ArrayList<>();
            for (Introduction introduction : introductions) {
                Introduction newIntroduction = new Introduction();
                BeanUtils.copyProperties(introduction, newIntroduction, "id");
                newIntroduction.setServicePackId(newServicePack.getId());
                newIntroductions.add(newIntroduction);
            }
            introductionService.saveBatch(newIntroductions);
        }


        return RestResponse.ok(newServicePack);


    }


    @Override
    protected Class<ServicePack> getEntityClass() {
        return ServicePack.class;
    }
}
