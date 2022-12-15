package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Log;
import cn.cuptec.faros.service.LogService;
import cn.cuptec.faros.vo.PreLogVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/log")
@Api(value = "log", tags = "日志管理模块")
public class LogController extends AbstractBaseController<LogService, Log> {

	private final LogService logService;

	/**
	 * 简单分页查询
	 * @return
	 */
	@GetMapping("/page")
	public RestResponse getLogPage() {
        Page<Log> page = getPage();
        if (page == null){
            return RestResponse.failed("请提供分页查询参数");
        }
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        return RestResponse.ok(logService.page(page, queryWrapper));

	}

	/**
	 * 删除日志
	 * @param id ID
	 * @return success/false
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('log_del')")
	public RestResponse removeById(@PathVariable Long id) {
		return RestResponse.ok(logService.removeById(id));
	}

	/**
	 * 批量插入前端异常日志
	 * @param preLogVoList 日志实体
	 * @return success/false
	 */
	@PostMapping("/logs")
	public RestResponse saveBatchLogs(@RequestBody List<PreLogVo> preLogVoList) {
		return RestResponse.ok(logService.saveBatchLogs(preLogVoList));
	}

    @Override
    protected Class<Log> getEntityClass() {
        return Log.class;
    }
}
