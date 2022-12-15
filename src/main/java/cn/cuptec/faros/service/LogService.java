package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.entity.Log;
import cn.cuptec.faros.mapper.LogMapper;
import cn.cuptec.faros.vo.PreLogVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 */
@Service
public class LogService extends ServiceImpl<LogMapper, Log> {

	/**
	 * 批量插入前端错误日志
	 *
	 * @param preLogVoList 日志信息
	 * @return true/false
	 */
	public Boolean saveBatchLogs(List<PreLogVo> preLogVoList) {
		List<Log> tenantLogs = preLogVoList.stream()
			.map(pre -> {
				Log log = new Log();
				log.setType(CommonConstants.STATUS_LOCK);
				log.setTitle(pre.getInfo());
				log.setException(pre.getStack());
				log.setParams(pre.getMessage());
				log.setCreateTime(LocalDateTime.now());
				log.setRequestUri(pre.getUrl());
				log.setCreateBy(pre.getUser());
				return log;
			})
			.collect(Collectors.toList());
		return this.saveBatch(tenantLogs);
	}
}
