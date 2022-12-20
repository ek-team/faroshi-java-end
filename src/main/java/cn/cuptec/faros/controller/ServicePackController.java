package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.controller.base.AbstractBaseController;

import cn.cuptec.faros.entity.*;

import cn.cuptec.faros.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private ServicePackSaleSpecService servicePackSaleSpecService;//销售规格
    @Resource
    private ServicePackageInfoService servicePackageInfoService;//服务信息
    @Resource
    private ServicePackDetailService servicePackDetailService;//服务详情页面
    @Resource
    private UserService userService;
    @Resource
    private ProductSpecService productSpecService;//产品规格
    @Resource
    private ProductSpecDescService productSpecDescService;//产品规格描述
    @Resource
    private SaleSpecService saleSpecService;//销售规格
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private IntroductionService introductionService;//服务简介
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    /**
     * 设备二维码绑定服务包
     */
    @GetMapping("/bindProductStok")
    public RestResponse saveProductSpec(@RequestParam("servicePackId") Integer servicePackId, @RequestParam("productStockId") Integer productStockId) {
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
     * 添加产品规格
     *
     * @return
     */
    @PostMapping("/saveProductSpec")
    public RestResponse saveProductSpec(@RequestBody ProductSpec productSpec) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        productSpec.setDeptId(byId.getDeptId());
        productSpecService.save(productSpec);
        List<ProductSpecDesc> productSpecDesc = productSpec.getProductSpecDesc();
        if (!CollectionUtils.isEmpty(productSpecDesc)) {
            for (ProductSpecDesc productSpecDesc1 : productSpecDesc) {
                productSpecDesc1.setProductSpecId(productSpec.getId());
            }
            productSpecDescService.saveBatch(productSpecDesc);
        }

        return RestResponse.ok();
    }

    /**
     * 编辑产品规格
     *
     * @return
     */
    @PostMapping("/updateProductSpec")
    public RestResponse updateProductSpec(@RequestBody ProductSpec productSpec) {

        productSpecService.updateById(productSpec);
        productSpecDescService.remove(new QueryWrapper<ProductSpecDesc>().lambda()
                .eq(ProductSpecDesc::getProductSpecId, productSpec.getId()));
        List<ProductSpecDesc> productSpecDesc = productSpec.getProductSpecDesc();
        if (!CollectionUtils.isEmpty(productSpecDesc)) {
            for (ProductSpecDesc productSpecDesc1 : productSpecDesc) {
                productSpecDesc1.setProductSpecId(productSpec.getId());
            }
            productSpecDescService.saveBatch(productSpecDesc);
        }

        return RestResponse.ok();
    }

    /**
     * 删除产品规格
     *
     * @return
     */
    @GetMapping("/deleteProductSpec")
    public RestResponse listProductSpec(@RequestParam("id") Integer id) {
        productSpecService.removeById(id);
        productSpecDescService.remove(new QueryWrapper<ProductSpecDesc>().lambda()
                .eq(ProductSpecDesc::getProductSpecId, id));
        return RestResponse.ok();
    }

    /**
     * 产品规格列表查询
     *
     * @return
     */
    @GetMapping("/listProductSpec")
    public RestResponse listProductSpec() {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        List<ProductSpec> list = productSpecService.list(new QueryWrapper<ProductSpec>().lambda()
                .eq(ProductSpec::getDeptId, byId.getDeptId()));
        List<Integer> productSpecIds = list.stream().map(ProductSpec::getId)
                .collect(Collectors.toList());
        List<ProductSpecDesc> productSpecDescs = productSpecDescService.list(new QueryWrapper<ProductSpecDesc>().lambda()
                .in(ProductSpecDesc::getProductSpecId, productSpecIds));
        if (!CollectionUtils.isEmpty(productSpecDescs)) {
            Map<Integer, List<ProductSpecDesc>> map = productSpecDescs.stream()
                    .collect(Collectors.groupingBy(ProductSpecDesc::getProductSpecId));
            for (ProductSpec productSpec : list) {
                productSpec.setProductSpecDesc(map.get(productSpec.getId()));
            }
        }
        return RestResponse.ok(list);


    }

    /**
     * 添加销售规格
     *
     * @return
     */
    @PostMapping("/saveSaleSpec")
    public RestResponse saveSaleSpec(@RequestBody SaleSpec saleSpec) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        saleSpec.setDeptId(byId.getDeptId());
        saleSpecService.save(saleSpec);
        return RestResponse.ok();
    }

    /**
     * 编辑销售规格
     *
     * @return
     */
    @PostMapping("/updateSaleSpec")
    public RestResponse updateSaleSpec(@RequestBody SaleSpec saleSpec) {
        saleSpecService.updateById(saleSpec);
        return RestResponse.ok();
    }

    /**
     * 删除销售规格
     *
     * @return
     */
    @GetMapping("/deleteSaleSpec")
    public RestResponse deleteSaleSpec(@RequestParam("id") Integer id) {
        saleSpecService.removeById(id);
        return RestResponse.ok();
    }

    /**
     * 销售规格列表查询
     *
     * @return
     */
    @GetMapping("/listSaleSpec")
    public RestResponse listSaleSpec() {
        User byId = userService.getById(SecurityUtils.getUser().getId());

        return RestResponse.ok(saleSpecService.list(new QueryWrapper<SaleSpec>().lambda()
                .eq(SaleSpec::getDeptId, byId.getDeptId())));


    }

    /**
     * 添加服务包
     *
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody ServicePack servicePack) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        servicePack.setDeptId(byId.getDeptId());
        servicePack.setCreateTime(LocalDateTime.now());
        servicePack.setCreateUserId(byId.getId());
        service.save(servicePack);
        List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
        if(!CollectionUtils.isEmpty(servicePackProductPics)){
            for (ServicePackProductPic servicePackProductPic : servicePackProductPics) {
                servicePackProductPic.setServicePackId(servicePack.getId());
            }
            servicePackProductPicService.saveBatch(servicePackProductPics);
        }
        //添加产品规格
        List<Integer> productSpecs = servicePack.getProductSpecs();
        if (!CollectionUtils.isEmpty(productSpecs)) {
            List<ServicePackProductSpec> servicePackProductSpecs = new ArrayList<>();
            for (Integer productSpec : productSpecs) {
                ServicePackProductSpec servicePackProductSpec = new ServicePackProductSpec();
                servicePackProductSpec.setServicePackId(servicePack.getId());
                servicePackProductSpec.setProductSpecId(productSpec);
                servicePackProductSpecs.add(servicePackProductSpec);
            }
            servicePackProductSpecService.saveBatch(servicePackProductSpecs);
        }
        //添加销售规格
        List<Integer> saleSpecs = servicePack.getSaleSpecs();
        if (!CollectionUtils.isEmpty(saleSpecs)) {
            List<ServicePackSaleSpec> servicePackSaleSpecs = new ArrayList<>();
            for (Integer saleSpec : saleSpecs) {
                ServicePackSaleSpec servicePackSaleSpec = new ServicePackSaleSpec();
                servicePackSaleSpec.setServicePackId(servicePack.getId());
                servicePackSaleSpec.setSaleSpecId(saleSpec);
                servicePackSaleSpecs.add(servicePackSaleSpec);
            }
            servicePackSaleSpecService.saveBatch(servicePackSaleSpecs);
        }
        //服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePack.getServicePackageInfos();
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                servicePackageInfo.setServicePackageId(servicePack.getId());
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
     * 编辑服务包
     *
     * @return
     */
    @PostMapping("/edit")
    public RestResponse edit(@RequestBody ServicePack servicePack) {
        service.updateById(servicePack);
        //产品规格
        List<Integer> productSpecs = servicePack.getProductSpecs();
        servicePackProductSpecService.remove(new QueryWrapper<ServicePackProductSpec>().lambda().eq(ServicePackProductSpec::getServicePackId, servicePack.getId()));
        if (!CollectionUtils.isEmpty(productSpecs)) {

            List<ServicePackProductSpec> servicePackProductSpecs = new ArrayList<>();
            for (Integer productSpec : productSpecs) {
                ServicePackProductSpec servicePackProductSpec = new ServicePackProductSpec();
                servicePackProductSpec.setServicePackId(servicePack.getId());
                servicePackProductSpec.setProductSpecId(productSpec);
                servicePackProductSpecs.add(servicePackProductSpec);
            }
            servicePackProductSpecService.saveBatch(servicePackProductSpecs);
        }
        servicePackProductPicService.remove(new QueryWrapper<ServicePackProductPic>().lambda().eq(ServicePackProductPic::getServicePackId, servicePack.getId()));

        List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
        if(!CollectionUtils.isEmpty(servicePackProductPics)){
            for (ServicePackProductPic servicePackProductPic : servicePackProductPics) {
                servicePackProductPic.setServicePackId(servicePack.getId());
            }
            servicePackProductPicService.saveBatch(servicePackProductPics);
        }
        //添加销售规格
        servicePackSaleSpecService.remove(new QueryWrapper<ServicePackSaleSpec>().lambda().eq(ServicePackSaleSpec::getServicePackId, servicePack.getId()));

        List<Integer> saleSpecs = servicePack.getSaleSpecs();
        if (!CollectionUtils.isEmpty(saleSpecs)) {
            List<ServicePackSaleSpec> servicePackSaleSpecs = new ArrayList<>();
            for (Integer saleSpec : saleSpecs) {
                ServicePackSaleSpec servicePackSaleSpec = new ServicePackSaleSpec();
                servicePackSaleSpec.setServicePackId(servicePack.getId());
                servicePackSaleSpec.setSaleSpecId(saleSpec);
                servicePackSaleSpecs.add(servicePackSaleSpec);
            }
            servicePackSaleSpecService.saveBatch(servicePackSaleSpecs);
        }
        servicePackageInfoService.remove(new QueryWrapper<ServicePackageInfo>().lambda().eq(ServicePackageInfo::getServicePackageId, servicePack.getId()));
        //服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePack.getServicePackageInfos();
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                servicePackageInfo.setServicePackageId(servicePack.getId());
            }
            servicePackageInfoService.saveBatch(servicePackageInfos);
        }
        servicePackDetailService.remove(new QueryWrapper<ServicePackDetail>().lambda().eq(ServicePackDetail::getServicePackId, servicePack.getId()));

        //服务详情
        List<ServicePackDetail> servicePackDetails = servicePack.getServicePackDetails();
        if (!CollectionUtils.isEmpty(servicePackDetails)) {
            for (ServicePackDetail servicePackDetail : servicePackDetails) {
                servicePackDetail.setServicePackId(servicePackDetail.getId());
            }
            servicePackDetailService.saveBatch(servicePackDetails);
        }
        //服务简介
        introductionService.remove(new QueryWrapper<Introduction>().lambda().eq(Introduction::getServicePackId, servicePack.getId()));
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
     * 服务包列表查询分页
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped(@RequestParam(value = "startTime",required = false) String startTime, @RequestParam(value = "endTime",required =false) String endTime) {
        Page<ServicePack> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            queryWrapper.le("create_time", endTime);
            queryWrapper.ge("create_time", startTime);
        }
        IPage<ServicePack> servicePackIPage = service.pageScoped(page, queryWrapper);

        return RestResponse.ok(servicePackIPage);
    }

    /**
     * 服务包详情查询
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {
        ServicePack servicePack = service.getById(id);
        //查询产品规格
        List<ServicePackProductSpec> servicePackProductSpecs = servicePackProductSpecService.list(new QueryWrapper<ServicePackProductSpec>()
                .lambda().eq(ServicePackProductSpec::getServicePackId, id));
        if (!CollectionUtils.isEmpty(servicePackProductSpecs)) {
            List<Integer> productSpecIds = servicePackProductSpecs.stream().map(ServicePackProductSpec::getProductSpecId)
                    .collect(Collectors.toList());
            List<ProductSpec> productSpecs = (List<ProductSpec>) productSpecService.listByIds(productSpecIds);
            servicePack.setProductSpec(productSpecs);
        }
        //查询产品图片
        List<ServicePackProductPic> list = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>()
                .lambda().eq(ServicePackProductPic::getServicePackId, id));
        servicePack.setServicePackProductPics(list);
        //查询销售规格
        List<ServicePackSaleSpec> servicePackSaleSpecs = servicePackSaleSpecService.list(new QueryWrapper<ServicePackSaleSpec>()
                .lambda().eq(ServicePackSaleSpec::getServicePackId, id));
        if (!CollectionUtils.isEmpty(servicePackSaleSpecs)) {
            List<Integer> saleSpecIds = servicePackSaleSpecs.stream().map(ServicePackSaleSpec::getSaleSpecId)
                    .collect(Collectors.toList());
            List<SaleSpec> saleSpecs = (List<SaleSpec>) saleSpecService.listByIds(saleSpecIds);
            servicePack.setSaleSpec(saleSpecs);
        }
        //查询服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>()
                .lambda().eq(ServicePackageInfo::getServicePackageId, id));
        //查询服务的医生团队
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {
            List<Integer> teamIds = new ArrayList<>();
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                String doctorTeamIds = servicePackageInfo.getDoctorTeamIds();
                String replace = doctorTeamIds.replace("[", "");
                String replace1 = replace.replace("]", "");
                String[] split = replace1.split(",");
                for (int i = 0; i < split.length; i++) {
                    String n = split[i];
                    teamIds.add(Integer.parseInt(n));
                }
            }
            List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(teamIds);
            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                String doctorTeamIds = servicePackageInfo.getDoctorTeamIds();
                List<DoctorTeam> doctorTeamList = servicePackageInfo.getDoctorTeamList();
                for (DoctorTeam doctorTeam : doctorTeams) {
                    if (doctorTeamIds.contains(doctorTeam.getId() + "")) {
                        if(CollectionUtils.isEmpty(doctorTeamList)){
                            doctorTeamList=new ArrayList<>();
                        }
                        doctorTeamList.add(doctorTeam);
                    }
                }
                servicePackageInfo.setDoctorTeamList(doctorTeamList);
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

    @Override
    protected Class<ServicePack> getEntityClass() {
        return ServicePack.class;
    }
}
