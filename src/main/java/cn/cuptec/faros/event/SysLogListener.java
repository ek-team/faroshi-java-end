package cn.cuptec.faros.event;

import cn.cuptec.faros.entity.Log;
import cn.cuptec.faros.service.LogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步监听日志事件
 */
@Slf4j
@AllArgsConstructor
@Component
public class SysLogListener {
	private final LogService logService;

	@Async
	@Order
	@EventListener(SysLogEvent.class)
	public void saveSysLog(SysLogEvent event) {
		Log tenantLog = event.getLog();
		logService.save(tenantLog);
	}
}
