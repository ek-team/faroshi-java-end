package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.UploadXPianParam;
import cn.cuptec.faros.entity.PlanUserOtherInfo;
import cn.cuptec.faros.entity.PneumaticPlan;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.entity.XPic;
import cn.cuptec.faros.service.PlanUserOtherInfoService;
import cn.cuptec.faros.service.PlanUserService;
import cn.cuptec.faros.service.XPicService;
import cn.cuptec.faros.util.FileUtils;
import cn.cuptec.faros.util.UploadFileUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/xPic")
@Slf4j
public class XPicController extends AbstractBaseController<XPicService, XPic> {
    private final OssProperties ossProperties;
    @Resource
    private PlanUserService planUserService;

    @Resource
    private PlanUserOtherInfoService planUserOtherInfoService;

    /**
     * 根据身份证查询
     */
    @GetMapping("/getByIdCard")
    public RestResponse getByIdCard(@RequestParam(value = "idCard", required = false) String idCard, @RequestParam(value = "userId", required = false) String userId) {
        List<XPic> list = service.list(new QueryWrapper<XPic>().lambda().eq(XPic::getIdCard, idCard));
        if (!CollectionUtils.isEmpty(list)) {
            List<LocalDate> datas = list.stream().map(XPic::getCreateTime)
                    .collect(Collectors.toList());
            return RestResponse.ok(datas);
        }
        return RestResponse.ok(new ArrayList<>());
    }

    /**
     * 根据时间身份证查询
     */
    @GetMapping("/getByIdCardAndTime")
    public RestResponse getByIdCardAndTime(@RequestParam("idCard") String idCard, @RequestParam(value = "time", required = false) String time) {
        LambdaQueryWrapper<XPic> eq = new QueryWrapper<XPic>().lambda().eq(XPic::getIdCard, idCard);
        if (!StringUtils.isEmpty(time)) {
            eq.eq(XPic::getCreateTime, time);
        }
        List<XPic> list = service.list(eq);

        return RestResponse.ok(list);
    }

    /**
     * 根据身份证查询设备用户的其他信息
     */
    @GetMapping("/getPlanUserOtherByIdCard")
    public RestResponse getPlanUserOtherByIdCard(@RequestParam(value = "idCard", required = false) String idCard) {
        PlanUserOtherInfo planUserOtherInfo = planUserOtherInfoService.getOne(new QueryWrapper<PlanUserOtherInfo>().lambda().eq(PlanUserOtherInfo::getIdCard, idCard));

        return RestResponse.ok(planUserOtherInfo);
    }

    /**
     * 删除x片
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam(value = "id", required = false) Integer id) {
        service.removeById(id);
        return RestResponse.ok();
    }

    /**
     * 根据身份证查询设备用户的其他信息
     */
    @PostMapping("/saveOrUpdatePlanUserOtherByIdCard")
    public RestResponse saveOrUpdatePlanUserOtherByIdCard(@RequestBody PlanUserOtherInfo planUserOtherInfo) {
        PlanUserOtherInfo data = planUserOtherInfoService.getOne(new QueryWrapper<PlanUserOtherInfo>().lambda().eq(PlanUserOtherInfo::getIdCard, planUserOtherInfo.getIdCard()));
        if (data == null) {
            planUserOtherInfoService.save(planUserOtherInfo);
        } else {
            data.setRegistrationEvaluation(planUserOtherInfo.getRegistrationEvaluation());
            if (!StringUtils.isEmpty(planUserOtherInfo.getDegree())) {
                data.setDegree(planUserOtherInfo.getDegree());
            }
            if (!StringUtils.isEmpty(planUserOtherInfo.getSecondDiseaseName())) {
                data.setSecondDiseaseName(planUserOtherInfo.getSecondDiseaseName());
            }
            if (!StringUtils.isEmpty(planUserOtherInfo.getBodyPartName())) {
                data.setBodyPartName(planUserOtherInfo.getBodyPartName());
            }
            planUserOtherInfoService.updateById(data);
        }
        return RestResponse.ok(planUserOtherInfo);
    }

    /**
     * 上传X片
     */
    @PostMapping("/uploadXPian")
    public RestResponse uploadXPian(@RequestBody UploadXPianParam param) {
        try {
            TbTrainUser infoByUXtUserId = null;
            if (!StringUtils.isEmpty(param.getXtUserId())) {
                infoByUXtUserId = planUserService.getInfoByUXtUserId(Integer.parseInt(param.getXtUserId()));
            }

            if (infoByUXtUserId == null && StringUtils.isEmpty(param.getIdCard())) {
                return RestResponse.failed("未查询到设备用户");
            }
            List<XPic> xPics = new ArrayList<>();
            for (String url : param.getUrls()) {
                XPic xPic = new XPic();
                xPic.setUrl(url);
                if (!StringUtils.isEmpty(param.getCreateTime())) {
                    xPic.setCreateTime(LocalDate.parse(param.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    xPic.setCreateTime(LocalDate.now());
                }
                if (infoByUXtUserId == null) {
                    xPic.setIdCard(param.getIdCard());

                } else {
                    xPic.setIdCard(infoByUXtUserId.getIdCard());

                }
                xPics.add(xPic);
            }

            service.saveBatch(xPics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    /**
     * 上传X片 会把之前的删除掉
     */
    @PostMapping("/saveOrUpdate")
    public RestResponse saveOrUpdate(@RequestBody List<UploadXPianParam> params) {
        List<XPic> xPics = new ArrayList<>();
        for (UploadXPianParam param : params) {
            try {
                TbTrainUser infoByUXtUserId = null;
                if (!StringUtils.isEmpty(param.getXtUserId())) {
                    infoByUXtUserId = planUserService.getInfoByUXtUserId(Integer.parseInt(param.getXtUserId()));
                }

                if (infoByUXtUserId == null && StringUtils.isEmpty(param.getIdCard())) {
                    return RestResponse.failed("未查询到设备用户");
                }

                for (String url : param.getUrls()) {
                    XPic xPic = new XPic();
                    xPic.setUrl(url);
                    if (!StringUtils.isEmpty(param.getCreateTime())) {
                        xPic.setCreateTime(LocalDate.parse(param.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else {
                        xPic.setCreateTime(LocalDate.now());
                    }
                    if (infoByUXtUserId == null) {
                        xPic.setIdCard(param.getIdCard());

                    } else {
                        xPic.setIdCard(infoByUXtUserId.getIdCard());

                    }
                    xPics.add(xPic);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!CollectionUtils.isEmpty(xPics)) {
            String idCard = xPics.get(0).getIdCard();
            service.remove(new QueryWrapper<XPic>().lambda()
                    .eq(XPic::getIdCard, idCard));
        }
        service.saveBatch(xPics);
        return RestResponse.ok();
    }

    @Override
    protected Class<XPic> getEntityClass() {
        return XPic.class;
    }
}
