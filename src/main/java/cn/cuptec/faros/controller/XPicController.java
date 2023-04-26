package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.UploadXPianParam;
import cn.cuptec.faros.entity.PneumaticPlan;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.entity.XPic;
import cn.cuptec.faros.service.PlanUserService;
import cn.cuptec.faros.service.XPicService;
import cn.cuptec.faros.util.FileUtils;
import cn.cuptec.faros.util.UploadFileUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
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

    /**
     * 根据身份证查询
     */
    @GetMapping("/getByIdCard")
    public RestResponse getByIdCard(@RequestParam(value = "idCard",required = false) String idCard,@RequestParam(value = "userId",required = false) String userId) {
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
    public RestResponse getByIdCardAndTime(@RequestParam("idCard") String idCard, @RequestParam("time") String time) {
        List<XPic> list = service.list(new QueryWrapper<XPic>().lambda().eq(XPic::getIdCard, idCard).eq(XPic::getCreateTime, time));

        return RestResponse.ok(list);
    }

    /**
     * 上传X片
     */
    @PostMapping("/uploadXPian")
    public RestResponse uploadXPian(@RequestBody UploadXPianParam param) {
        try {

            TbTrainUser infoByUXtUserId = planUserService.getInfoByUXtUserId(Integer.parseInt(param.getXtUserId()));
            if (infoByUXtUserId == null) {
                return RestResponse.failed("未查询到设备用户");
            }
            List<XPic> xPics = new ArrayList<>();
            for (String url : param.getUrls()) {
                XPic xPic = new XPic();
                xPic.setUrl(url);
                xPic.setCreateTime(LocalDate.parse(param.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                xPic.setIdCard(infoByUXtUserId.getIdCard());
                xPics.add(xPic);
            }

            service.saveBatch(xPics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    @Override
    protected Class<XPic> getEntityClass() {
        return XPic.class;
    }
}
