package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.entity.MacAddDownloadType;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.service.MacAddDownloadTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 选择设备下载数据信息
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/macAddDownloadType")
public class MacAddDownloadTypeController {
    @Resource
    private MacAddDownloadTypeService macAddDownloadTypeService;

    @PostMapping("/save")
    public RestResponse save(@RequestBody MacAddDownloadType macAddDownloadType) {
        MacAddDownloadType one = macAddDownloadTypeService.getOne(new QueryWrapper<MacAddDownloadType>().lambda().eq(MacAddDownloadType::getMacAdd, macAddDownloadType.getMacAdd()).eq(MacAddDownloadType::getDownloadType, macAddDownloadType.getDownloadType()));
        if (one != null) {
            return RestResponse.failed("请勿重复添加");
        }
        return RestResponse.ok(macAddDownloadTypeService.save(macAddDownloadType));
    }

    @GetMapping("/delete")
    public RestResponse delete(@RequestParam("id") Integer id) {

        return RestResponse.ok(macAddDownloadTypeService.removeById(id));
    }

    @GetMapping("/list")
    public RestResponse list(@RequestParam("macAdd") String macAdd) {

        return RestResponse.ok(macAddDownloadTypeService.list(new QueryWrapper<MacAddDownloadType>().lambda().eq(MacAddDownloadType::getMacAdd, macAdd)));
    }
}
