package cn.cuptec.faros.config.event;

import cn.cuptec.faros.entity.SysJob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.quartz.Trigger;

/**
 * 定时任务多线程事件
 */
@Getter
@AllArgsConstructor
public class SysJobEvent {

    private final SysJob sysJob;

    private final Trigger trigger;
}