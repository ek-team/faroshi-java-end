package cn.cuptec.faros.config.job;

import cn.cuptec.faros.entity.PigxQuartzEnum;
import cn.cuptec.faros.entity.SysJob;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 动态任务工厂
 */
@Slf4j
@DisallowConcurrentExecution
public class PigxQuartzFactory implements Job {

    @Autowired
    private PigxQuartzInvokeFactory pigxQuartzInvokeFactory;


    @Override
    @SneakyThrows
    public void execute(JobExecutionContext jobExecutionContext) {
        SysJob sysJob = (SysJob) jobExecutionContext.getMergedJobDataMap().get(PigxQuartzEnum.SCHEDULE_JOB_KEY.getType());
        pigxQuartzInvokeFactory.init(sysJob, jobExecutionContext.getTrigger());
    }
}
