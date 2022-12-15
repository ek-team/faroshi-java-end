package cn.cuptec.faros.controller;


import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dict;
import cn.cuptec.faros.entity.DictItem;
import cn.cuptec.faros.service.DictItemService;
import cn.cuptec.faros.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dict")
@Api(value = "dict", tags = "字典管理模块")
public class DictController extends AbstractBaseController<DictService, Dict> {
	private final DictItemService dictItemService;

	@ApiOperation(value = "通过ID查询字典信息")
	@GetMapping("/{id}")
	public RestResponse<Dict> getById(@PathVariable Integer id) {
		return RestResponse.ok(service.getById(id));
	}

	@ApiOperation(value = "分页查询字典信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageSize", value = "分页大小", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "pageNum", value = "页码", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "ascs", value = "升序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "descs", value = "降序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "id", value = "字典id", dataTypeClass = Integer.class, type = "query"),
			@ApiImplicitParam(name = "type", value = "字典类型", dataTypeClass = String.class, type = "query"),
			@ApiImplicitParam(name = "createTimeBegin", value = "创建时间查询参数-开始时间", dataTypeClass = Date.class, type = "query"),
			@ApiImplicitParam(name = "createTimeEnd", value = "创建时间查询参数-结束时间", dataTypeClass = Date.class, type = "query"),
	})
	@GetMapping("/page")
	public RestResponse<IPage> getDictPage() {
		Page<Dict> page = getPage();
		QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
		if (page != null)
			return RestResponse.ok(service.page(page, queryWrapper));
		else
			return RestResponse.ok();
	}

	/**
	 * 通过字典类型查找字典
	 *
	 * @param type 类型
	 * @return 同类型字典
	 */
	@GetMapping("/type/{type}")
	@Cacheable(value = CacheConstants.DICT_DETAILS, key = "#type", unless = "#result == null")
	public RestResponse getDictByType(@PathVariable String type) {
		return RestResponse.ok(dictItemService.list(Wrappers
				.<DictItem>query().lambda()
				.eq(DictItem::getType, type)));
	}

	@ApiOperation(value = "添加字典")
	@SysLog("添加字典")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_dict_add')")
	public RestResponse<Boolean> save(@Valid @RequestBody Dict sysDict) {
		return RestResponse.ok(service.save(sysDict));
	}

	@ApiOperation(value = "删除字典，并且清除字典缓存")
	@SysLog("删除字典")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_dict_del')")
	public RestResponse removeById(@PathVariable Integer id) {
		return service.removeDict(id);
	}

	@ApiOperation(value = "修改字典")
	@PutMapping
	@SysLog("修改字典")
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	public RestResponse updateById(@Valid @RequestBody Dict sysDict) {
		return service.updateDict(sysDict);
	}

	@ApiOperation(value = "字典项分页查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageSize", value = "分页大小", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "pageNum", value = "页码", dataTypeClass = Integer.class, type = "query", required = true),
			@ApiImplicitParam(name = "ascs", value = "升序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "descs", value = "降序排列字段", dataTypeClass = String[].class, type = "query"),
			@ApiImplicitParam(name = "id", value = "字典项id", dataTypeClass = Integer.class, type = "query"),
			@ApiImplicitParam(name = "dictId", value = "字典id", dataTypeClass = Integer.class, type = "query"),
			@ApiImplicitParam(name = "value", value = "字典项值(模糊查询)", dataTypeClass = String.class, type = "query"),
			@ApiImplicitParam(name = "label", value = "标签值(模糊查询)", dataTypeClass = String.class, type = "query"),
			@ApiImplicitParam(name = "type", value = "类型", dataTypeClass = String.class, type = "query"),
			@ApiImplicitParam(name = "updateTimeBegin", value = "更新时间查询参数-开始时间", dataTypeClass = Date.class, type = "query"),
			@ApiImplicitParam(name = "updateTimeEnd", value = "更新时间查询参数-结束时间", dataTypeClass = Date.class, type = "query"),
	})
	@GetMapping("/item/page")
	public RestResponse<IPage<DictItem>> getSysDictItemPage() {
		Page page = getPage(new Page<DictItem>());
		QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
		return RestResponse.ok(dictItemService.page(page, queryWrapper));
	}

	@ApiOperation(value = "通过id查询字典项")
	@GetMapping("/item/{id}")
	public RestResponse<DictItem> getDictItemById(@PathVariable("id") Integer id) {
		return RestResponse.ok(dictItemService.getById(id));
	}

	@ApiOperation(value = "新增字典项")
	@SysLog("新增字典项")
	@PostMapping("/item")
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public RestResponse save(@RequestBody DictItem sysDictItem) {
		return RestResponse.ok(dictItemService.save(sysDictItem));
	}

	@ApiOperation(value = "修改字典项")
	@SysLog("修改字典项")
	@PutMapping("/item")
	public RestResponse updateById(@RequestBody DictItem sysDictItem) {
		return dictItemService.updateDictItem(sysDictItem);
	}

	@ApiOperation(value = "通过id删除字典项")
	@SysLog("删除字典项")
	@DeleteMapping("/item/{id}")
	public RestResponse removeDictItemById(@PathVariable Integer id) {
		return dictItemService.removeDictItem(id);
	}

	@Override
	protected Class<Dict> getEntityClass() {
		return Dict.class;
	}
}
