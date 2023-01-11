package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.entity.AppVersion;
import cn.cuptec.faros.service.AppVersionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/appVersion")
public class AppVersionController {
    @Resource
    private AppVersionService appVersionServiceImpl;

    @ApiOperation(value = "app自动更新")
    @GetMapping("/update")
    public RestResponse checkUpdate(@RequestParam("version") String version,@RequestParam("model") String model) {
        Integer integer = Integer.valueOf(version.replaceAll("\\.", ""));
        AppVersion appVersion = appVersionServiceImpl.getOne(new QueryWrapper<AppVersion>().lambda()
                .eq(AppVersion::getModel, model));


        System.out.println("version:" + version);
        System.out.println("miniVersion:" + appVersion.getApkVersion());
        System.out.println("apkVersion:" + appVersion.getWgtVersion());

        if (integer < appVersion.getApkVersion()) {
//            如果传递过来的版本号小于当前最新版本号，说明有更新
            return RestResponse.ok(appVersion);
        }
        return RestResponse.ok();
    }
}
