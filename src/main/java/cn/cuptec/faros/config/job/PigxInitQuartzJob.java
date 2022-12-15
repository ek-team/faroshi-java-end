package cn.cuptec.faros.config.job;

import cn.cuptec.faros.entity.PigxQuartzEnum;
import cn.cuptec.faros.service.SysJobService;
import cn.cuptec.faros.util.TaskUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化加载定时任务
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class PigxInitQuartzJob {
    private final SysJobService sysJobService;
    private final TaskUtil taskUtil;
    private final Scheduler scheduler;

    @Bean
    public void customize() {
        sysJobService.list().forEach(sysjob -> {
            if (PigxQuartzEnum.JOB_STATUS_RELEASE.getType().equals(sysjob.getJobStatus())) {
                taskUtil.removeJob(sysjob, scheduler);
            } else if (PigxQuartzEnum.JOB_STATUS_RUNNING.getType().equals(sysjob.getJobStatus())) {
                taskUtil.resumeJob(sysjob, scheduler);
            } else if (PigxQuartzEnum.JOB_STATUS_NOT_RUNNING.getType().equals(sysjob.getJobStatus())) {
                taskUtil.pauseJob(sysjob, scheduler);
            } else {
                taskUtil.removeJob(sysjob, scheduler);
            }
        });
    }
}