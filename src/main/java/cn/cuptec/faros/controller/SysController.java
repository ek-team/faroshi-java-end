package cn.cuptec.faros.controller;



import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.SysJob;
import cn.cuptec.faros.entity.SysJobLog;
import cn.cuptec.faros.service.SysJobLogService;
import cn.cuptec.faros.service.SysJobService;
import cn.cuptec.faros.util.TaskUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.cuptec.faros.entity.PigxQuartzEnum.*;


@RestController

@RequestMapping("/sys")
public class SysController {

    @GetMapping("/getTime")
    public RestResponse getTime() {
        return RestResponse.ok(System.currentTimeMillis());
    }
}
