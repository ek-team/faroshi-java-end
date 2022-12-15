package cn.cuptec.faros.config.job;

import cn.cuptec.faros.config.event.SysJobEvent;
import cn.cuptec.faros.entity.SysJob;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.Trigger;
import org.springframework.context.ApplicationEventPublisher;

@Aspect
@Slf4j
@AllArgsConstructor
public class PigxQuartzInvokeFactory {

    private final ApplicationEventPublisher publisher;

    @SneakyThrows
    void init(SysJob sysJob, Trigger trigger) {
        publisher.publishEvent(new SysJobEvent(sysJob, trigger));
    }
}

