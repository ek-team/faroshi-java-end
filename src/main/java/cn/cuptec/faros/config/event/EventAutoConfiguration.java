package cn.cuptec.faros.config.event;

import cn.cuptec.faros.config.job.PigxQuartzInvokeFactory;
import cn.cuptec.faros.service.SysJobLogService;
import cn.cuptec.faros.util.TaskInvokUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 多线程自动配置
 */
@EnableAsync
@Configuration
@ConditionalOnWebApplication
public class EventAutoConfiguration {
    @Autowired
    private TaskInvokUtil taskInvokUtil;
    @Autowired
    private SysJobLogService sysJobLogService;

    @Bean
    public SysJobListener sysJobListener() {
        return new SysJobListener(taskInvokUtil);
    }

    @Bean
    public PigxQuartzInvokeFactory pigxQuartzInvokeFactory(ApplicationEventPublisher publisher) {
        return new PigxQuartzInvokeFactory(publisher);
    }

    @Bean
    public SysJobLogListener sysJobLogListener() {
        return new SysJobLogListener(sysJobLogService);
    }

    @Bean
    public TaskInvokUtil taskInvokUtil(ApplicationEventPublisher publisher) {
        return new TaskInvokUtil(publisher);
    }

}
