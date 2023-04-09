package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.DeviceLog;
import cn.cuptec.faros.entity.SysJob;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.DeptService;
import cn.cuptec.faros.service.DeviceLogService;
import cn.cuptec.faros.util.AliOssUtils;
import cn.cuptec.faros.util.UploadFileUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 设备日志管理
 */
@RestController
@AllArgsConstructor
@RequestMapping("/devicelog")
public class DeviceLogController extends AbstractBaseController<DeviceLogService, DeviceLog> {
    private final OssProperties ossProperties;

    @PostMapping("/add")
    public RestResponse<Boolean> save(@RequestBody DeviceLog deviceLog) {
        deviceLog.setCreateTime(LocalDateTime.now());
        deviceLog.setLogUrl(stringReplace(deviceLog.getLogUrl()));
        return RestResponse.ok(service.save(deviceLog));
    }

    public static void main(String[] args) {
        String url = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/log/1.xlog";
        String dir = url.split("/")[3] + "/" + url.split("/")[4];
        System.out.println(dir);
    }

    public static String stringReplace(String str) {

        return str.replace("\"", "");

    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public RestResponse getSysJobPage() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<DeviceLog> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    @DeleteMapping("/delete")
    public RestResponse<Boolean> removeById(@RequestParam("id") Integer id) {
        DeviceLog deviceLog = service.getById(id);
        if (deviceLog != null) {
            String logUrl = deviceLog.getLogUrl();

            String dir = logUrl.split("/")[3] + "/" + logUrl.split("/")[4];
            try {
                UploadFileUtils.deleteFile(dir, ossProperties);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.ok(service.removeById(id));
    }

    @PutMapping("/update")
    public RestResponse update(@RequestBody DeviceLog deviceLog) {
        return RestResponse.ok(service.updateById(deviceLog));
    }

    @Override
    protected Class<DeviceLog> getEntityClass() {
        return DeviceLog.class;
    }
}
