package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.enums.DictTypeEnum;
import cn.cuptec.faros.entity.Dict;
import cn.cuptec.faros.entity.DictItem;
import cn.cuptec.faros.mapper.DictItemMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Service
@AllArgsConstructor
public class DictItemService extends ServiceImpl<DictItemMapper, DictItem> {
	private final DictService dictService;

	/**
	 * 删除字典项
	 *
	 * @param id 字典项ID
	 * @return
	 */
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public RestResponse removeDictItem(Integer id) {
		//根据ID查询字典ID
		DictItem dictItem = this.getById(id);
		Dict dict = dictService.getById(dictItem.getDictId());
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType().equals(dict.getSystem())) {
			return RestResponse.failed("系统内置字典项目不能删除");
		}
		return RestResponse.ok(this.removeById(id));
	}

	/**
	 * 更新字典项
	 *
	 * @param item 字典项
	 * @return
	 */
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public RestResponse updateDictItem(DictItem item) {
		//查询字典
		Dict dict = dictService.getById(item.getDictId());
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType().equals(dict.getSystem())) {
			return RestResponse.failed("系统内置字典项目不能删除");
		}
		return RestResponse.ok(this.updateById(item));
	}
}
