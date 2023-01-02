package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.wx.WxMpProperties;
import cn.cuptec.faros.dto.DownloadProductStockInfoResult;
import cn.cuptec.faros.entity.MacAddDownloadType;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.service.MacAddDownloadTypeService;
import cn.cuptec.faros.service.ProductStockService;
import cn.cuptec.faros.util.FileUtils;
import cn.cuptec.faros.util.UploadFileUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/file")
@Api(value = "file", tags = "文件上传")
public class UploadFileController {
    private final OssProperties ossProperties;
    @Resource
    private MacAddDownloadTypeService macAddDownloadTypeService;
    @Resource
    private ProductStockService productStockService;

    /**
     * 上传文件
     *
     * @param mulFile
     * @param dir     文件存放目录
     * @return
     */
    @ApiOperation(value = "上传文件")
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile mulFile,
                             @RequestParam(value = "dir",required = false) String dir
    ) throws Exception {
        File file = FileUtils.multipartFileToFile(mulFile);
        String originalFilename = mulFile.getOriginalFilename();
        String[] split = originalFilename.split("\\.");
        dir="image/";
        return UploadFileUtils.uploadFile(file, dir, ossProperties,split[0] );
    }

    public static void main(String[] args) {
        String a="11.jpeg";
        System.out.println(a.split("\\.")[0]);
    }

    /**
     * 上传设备记录信息
     */
    @ApiOperation(value = "上传设备记录信息")
    @PostMapping("/uploadProductStockInfo")
    public RestResponse uploadProductStockInfo(@RequestParam("file") MultipartFile[] files,
                                               @RequestParam("macAdd") String macAdd
    ) throws Exception {
        productStockService.update(Wrappers.<ProductStock>lambdaUpdate()
                .eq(ProductStock::getMacAddress, macAdd)
                .set(ProductStock::getUpload, 1));//修改上传标识
        for (int i = 0; i <= files.length - 1; i++) {
            MultipartFile file = files[i];
            File fileResult = FileUtils.multipartFileToFile(file);
            //将macadd去除：号
            macAdd = macAdd.replaceAll(":", "");
            //获取文件名字
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("_");
            fileName = macAdd + "_" + split[1].split("\\.")[0];
            UploadFileUtils.uploadFile(fileResult, "productStockInfo/", ossProperties, fileName);

        }

        return RestResponse.ok();
    }

    /**
     * 下载设备记录信息
     */
    @ApiOperation(value = "下载设备记录信息")
    @GetMapping("/downloadProductStockInfo")
    public RestResponse downloadProductStockInfo(
            @RequestParam(value = "macAdd", required = false) String macAdd,
            @RequestParam(value = "sourceProductSn", required = false) String sourceProductSn
    ) throws Exception {
        //查询设备下载信息
        List<MacAddDownloadType> list = new ArrayList<>();

        if (!StringUtils.isEmpty(sourceProductSn)) {
            ProductStock productStock = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().
                    eq(ProductStock::getProductSn, sourceProductSn));
            list = macAddDownloadTypeService.list(new QueryWrapper<MacAddDownloadType>().lambda().eq(MacAddDownloadType::getMacAdd, productStock.getMacAddress()));
            macAdd = productStock.getMacAddress();
        } else {

            ProductStock one = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAdd));
            if (one == null || StringUtils.isEmpty(one.getTargetMacAdd())) {
                return RestResponse.ok("没有该mac地址的目标迁移数据");
            }
            list = macAddDownloadTypeService.list(new QueryWrapper<MacAddDownloadType>().lambda().eq(MacAddDownloadType::getMacAdd, macAdd));

            macAdd = one.getTargetMacAdd();
        }


        if (StringUtils.isEmpty(macAdd)) {
            return RestResponse.ok();
        }
        macAdd = macAdd.replaceAll(":", "");
        DownloadProductStockInfoResult result = new DownloadProductStockInfoResult();
        String url = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/productStockInfo/";
        if (CollectionUtils.isEmpty(list)) {
            //全部下载
            //_user.json
            result.setUser(url + macAdd + "_user.json");
            //_plan.json
            result.setPlan(url + macAdd + "_plan.json");
            //_subPlan.json
            result.setSubPlan(url + macAdd + "_subPlan.json");
            //_trainRecord.json
            result.setTrainRecord(url + macAdd + "_trainRecord.json");
            //_trainData.json
            result.setTrainData(url + macAdd + "_trainData.json");
            //_evaluateRecord.json
            result.setEvaluateRecord(url + macAdd + "_evaluateRecord.json");
        } else {

            for (MacAddDownloadType type : list) {
                String downloadType = type.getDownloadType();
                if (downloadType.equals("_user.json")) {
                    result.setUser(url + macAdd + "_user.json");
                } else if (downloadType.equals("_plan.json")) {
                    result.setPlan(url + macAdd + "_plan.json");
                } else if (downloadType.equals("_subPlan.json")) {
                    result.setSubPlan(url + macAdd + "_subPlan.json");
                } else if (downloadType.equals("_evaluateRecord.json")) {
                    result.setEvaluateRecord(url + macAdd + "_evaluateRecord.json");
                } else if (downloadType.equals("_trainData.json")) {
                    result.setTrainData(url + macAdd + "_trainData.json");
                } else if (downloadType.equals("_trainRecord.json")) {
                    result.setTrainRecord(url + macAdd + "_trainRecord.json");
                }
            }

        }


        return RestResponse.ok(result);
    }
}
