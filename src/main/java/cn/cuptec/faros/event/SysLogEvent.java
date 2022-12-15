package cn.cuptec.faros.event;

import cn.cuptec.faros.entity.Log;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统日志事件
 */
@Getter
@AllArgsConstructor
public class SysLogEvent {
	private final Log log;
}
