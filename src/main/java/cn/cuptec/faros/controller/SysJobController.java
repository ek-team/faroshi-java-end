package cn.cuptec.faros.controller;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/6 10:01
 */

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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.quartz.Scheduler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.PostConstruct;

import static cn.cuptec.faros.entity.PigxQuartzEnum.*;

/**
 * 定时任务管理
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-job")
@Api(value = "sys-job", tags = "定时任务")
public class SysJobController {
    private final SysJobService sysJobService;
    private final SysJobLogService sysJobLogService;
    private final TaskUtil taskUtil;
    private final Scheduler scheduler;

    /**
     * 定时任务分页查询
     *
     * @param page   分页对象
     * @param sysJob 定时任务调度表
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页定时业务查询")
    public RestResponse getSysJobPage(Page page, SysJob sysJob) {
        return RestResponse.ok(sysJobService.page(page, Wrappers.query(sysJob)));
    }


    /**
     * 通过id查询定时任务
     *
     * @param id id
     * @return RestResponse
     */
    @GetMapping("/getbyid")
    @ApiOperation(value = "唯一标识查询定时任务")
    public RestResponse getById(@RequestParam("id") Integer id) {
        return RestResponse.ok(sysJobService.getById(id));
    }

    /**
     * 新增定时任务
     *
     * @param sysJob 定时任务调度表
     * @return RestResponse
     */
    @SysLog("新增定时任务")
    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('job_sys_job_add')")
    @ApiOperation(value = "新增定时任务")
    public RestResponse save(@RequestBody SysJob sysJob) {
        sysJob.setJobStatus(JOB_STATUS_RELEASE.getType());
        sysJob.setCreateBy(SecurityUtils.getUser().getUsername());
        return RestResponse.ok(sysJobService.save(sysJob));
    }

    /**
     * 修改定时任务
     *
     * @param sysJob 定时任务调度表
     * @return RestResponse
     */
    @SysLog("修改定时任务")
    @PutMapping("/updateById")
    @PreAuthorize("@pms.hasPermission('job_sys_job_edit')")
    @ApiOperation(value = "修改定时任务")
    public RestResponse updateById(@RequestBody SysJob sysJob) {
        sysJob.setUpdateBy(SecurityUtils.getUser().getUsername());
        SysJob querySysJob = this.sysJobService.getById(sysJob.getJobId());
        if (JOB_STATUS_NOT_RUNNING.getType().equals(querySysJob.getJobStatus())) {
            this.taskUtil.addOrUpateJob(sysJob, scheduler);
            sysJobService.updateById(sysJob);
        } else if (JOB_STATUS_RELEASE.getType().equals(querySysJob.getJobStatus())) {
            sysJobService.updateById(sysJob);
        }
        return RestResponse.ok();
    }

    /**
     * 通过id删除定时任务
     *
     * @param id id
     * @return RestResponse
     */
    @SysLog("删除定时任务")
    @DeleteMapping("/removeById")
    @PreAuthorize("@pms.hasPermission('job_sys_job_del')")
    @ApiOperation(value = "唯一标识查询定时任务，暂停任务才能删除")
    public RestResponse removeById(@RequestParam Integer id) {
        SysJob querySysJob = this.sysJobService.getById(id);
        if (JOB_STATUS_NOT_RUNNING.getType().equals(querySysJob.getJobStatus())) {
            this.taskUtil.removeJob(querySysJob, scheduler);
            this.sysJobService.removeById(id);
        } else if (JOB_STATUS_RELEASE.getType().equals(querySysJob.getJobStatus())) {
            this.sysJobService.removeById(id);
        }
        return RestResponse.ok();
    }

    /**
     * 暂停全部定时任务
     *
     * @return
     */
    @SysLog("暂停全部定时任务")
    @PostMapping("/shutdown-jobs")
    @PreAuthorize("@pms.hasPermission('job_sys_job_shutdown_job')")
    @ApiOperation(value = "暂停全部定时任务")
    public RestResponse shutdownJobs() {
        taskUtil.pauseJobs(scheduler);
        int count = this.sysJobService.count(new LambdaQueryWrapper<SysJob>()
                .eq(SysJob::getJobStatus, JOB_STATUS_RUNNING.getType()));
        if (count <= 0) {
            return RestResponse.ok("无正在运行定时任务");
        } else {
            //更新定时任务状态条件，运行状态2更新为暂停状态2
            this.sysJobService.update(SysJob.builder()
                    .jobStatus(JOB_STATUS_NOT_RUNNING.getType()).build(), new UpdateWrapper<SysJob>()
                    .lambda().eq(SysJob::getJobStatus, JOB_STATUS_RUNNING.getType()));
            return RestResponse.ok("暂停成功");
        }
    }

    /**
     * 启动全部定时任务
     *
     * @return
     */
    @SysLog("启动全部定时任务")
    @PostMapping("/start-jobs")
    @PreAuthorize("@pms.hasPermission('job_sys_job_start_job')")
    @ApiOperation(value = "启动全部定时任务")
    public RestResponse startJobs() {
        //更新定时任务状态条件，暂停状态3更新为运行状态2
        this.sysJobService.update(SysJob.builder().jobStatus(JOB_STATUS_RUNNING
                .getType()).build(), new UpdateWrapper<SysJob>().lambda()
                .eq(SysJob::getJobStatus, JOB_STATUS_NOT_RUNNING.getType()));
        taskUtil.startJobs(scheduler);
        return RestResponse.ok();
    }

    /**
     * 刷新全部定时任务
     *
     * @return
     */
    @SysLog("刷新全部定时任务")
    @PostMapping("/refresh-jobs")
    @PreAuthorize("@pms.hasPermission('job_sys_job_refresh_job')")
    @ApiOperation(value = "刷新全部定时任务")
    public RestResponse refreshJobs() {
        sysJobService.list().forEach((sysjob) -> {
            if (JOB_STATUS_RELEASE.getType().equals(sysjob.getJobStatus())
                    || JOB_STATUS_DEL.getType().equals(sysjob.getJobStatus())) {
                taskUtil.removeJob(sysjob, scheduler);
            } else if (JOB_STATUS_RUNNING.getType().equals(sysjob.getJobStatus())
                    || JOB_STATUS_NOT_RUNNING.getType().equals(sysjob.getJobStatus())) {
                taskUtil.addOrUpateJob(sysjob, scheduler);
            } else {
                taskUtil.removeJob(sysjob, scheduler);
            }
        });
        return RestResponse.ok();
    }

    /**
     * 启动定时任务
     *
     * @param jobId
     * @return
     */
    @SysLog("启动定时任务")
    @PostMapping("/start-job/{id}")
    @PreAuthorize("@pms.hasPermission('job_sys_job_start_job')")
    @ApiOperation(value = "启动定时任务")
    public RestResponse startJob(@PathVariable("id") Integer jobId) {
        SysJob querySysJob = this.sysJobService.getById(jobId);
        if (querySysJob != null && JOB_LOG_STATUS_FAIL.getType()
                .equals(querySysJob.getJobStatus())) {
            taskUtil.addOrUpateJob(querySysJob, scheduler);
        } else {
            taskUtil.resumeJob(querySysJob, scheduler);
        }
        //更新定时任务状态条件，暂停状态3更新为运行状态2
        this.sysJobService.updateById(SysJob.builder().jobId(jobId)
                .jobStatus(JOB_STATUS_RUNNING.getType()).build());
        return RestResponse.ok();
    }

    /**
     * 暂停定时任务
     *
     * @return
     */
    @SysLog("暂停定时任务")
    @PostMapping("/shutdown-job/{id}")
    @PreAuthorize("@pms.hasPermission('job_sys_job_shutdown_job')")
    @ApiOperation(value = "暂停定时任务")
    public RestResponse shutdownJob(@PathVariable("id") Integer id) {
        SysJob querySysJob = this.sysJobService.getById(id);
        //更新定时任务状态条件，运行状态2更新为暂停状态3
        this.sysJobService.updateById(SysJob.builder().jobId(querySysJob.getJobId())
                .jobStatus(JOB_STATUS_NOT_RUNNING.getType()).build());
        taskUtil.pauseJob(querySysJob, scheduler);
        return RestResponse.ok();
    }

    /**
     * 唯一标识查询定时执行日志
     *
     * @return
     */
    @GetMapping("/job-log")
    @ApiOperation(value = "唯一标识查询定时执行日志")
    public RestResponse getJobLog(Page page, SysJobLog sysJobLog) {
        return RestResponse.ok(sysJobLogService.page(page, Wrappers.query(sysJobLog)));
    }

    /**
     * 检验任务名称和任务组联合是否唯一
     *
     * @return
     */
    @GetMapping("/is-valid-task-name")
    @ApiOperation(value = "检验任务名称和任务组联合是否唯一")
    public RestResponse isValidTaskName(@RequestParam String jobName, @RequestParam String jobGroup) {
        LambdaQueryWrapper<SysJob> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SysJob::getJobName,jobName);
        queryWrapper.eq(SysJob::getJobGroup,jobGroup);
        return this.sysJobService
                .count(queryWrapper) > 0
                ? RestResponse.failed() : RestResponse.ok();
    }
}
