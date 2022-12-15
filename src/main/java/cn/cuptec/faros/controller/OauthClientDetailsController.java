package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.SysOauthClientDetails;
import cn.cuptec.faros.service.SysOauthClientDetailsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/client")
@Api(value = "client", tags = "客户端管理模块")
public class OauthClientDetailsController extends AbstractBaseController<SysOauthClientDetailsService, SysOauthClientDetails> {

	@ApiOperation(value = "根据ID查询客户端")
	@GetMapping("/{id}")
	public RestResponse getById(@PathVariable Integer id) {
		return RestResponse.ok(service.getById(id));
	}


	@ApiOperation(value = "分页查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageSize", value = "分页大小", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "pageNum", value = "页码", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "ascs", value = "升序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "descs", value = "降序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "descs", value = "降序排列字段", dataTypeClass = String[].class, type = "query"),
	})
	@GetMapping("/page")
	public RestResponse<IPage<SysOauthClientDetails>> getOauthClientDetailsPage() {
		Page<SysOauthClientDetails> page = getPage();
		QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
		if (page != null)
			return RestResponse.ok(service.page(page, queryWrapper));
		else
			return RestResponse.ok(emptyPage());
	}

	@ApiOperation(value = "新增客户端")
	@SysLog("添加终端")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('client_add')")
	public RestResponse<Boolean> add(@Valid @RequestBody SysOauthClientDetails sysOauthClientDetails) {
		return RestResponse.ok(service.save(sysOauthClientDetails));
	}

	@ApiOperation(value = "根据ID删除客户端")
	@SysLog("删除终端")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('client_del')")
	public RestResponse<Boolean> removeById(@PathVariable String id) {
		return RestResponse.ok(service.removeClientDetailsById(id));
	}

	@ApiOperation(value = "编辑客户端")
	@SysLog("编辑终端")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('client_edit')")
	public RestResponse<Boolean> update(@Valid @RequestBody SysOauthClientDetails sysOauthClientDetails) {
		return RestResponse.ok(service.updateClientDetailsById(sysOauthClientDetails));
	}

	@Override
	protected Class<SysOauthClientDetails> getEntityClass() {
		return SysOauthClientDetails.class;
	}
}
