package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.enums.DictTypeEnum;
import cn.cuptec.faros.entity.Dict;
import cn.cuptec.faros.entity.DictItem;
import cn.cuptec.faros.mapper.DictItemMapper;
import cn.cuptec.faros.mapper.DictMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典表
 */
@Service
@AllArgsConstructor
public class DictService extends ServiceImpl<DictMapper, Dict> {
	private final DictItemMapper dictItemMapper;

	/**
	 * 根据ID 删除字典
	 *
	 * @param id 字典ID
	 * @return
	 */
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public RestResponse removeDict(Integer id) {
		Dict dict = this.getById(id);
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType().equals(dict.getSystem())) {
			return RestResponse.failed("系统内置字典不能删除");
		}

		baseMapper.deleteById(id);
		dictItemMapper.delete(Wrappers.<DictItem>lambdaQuery()
				.eq(DictItem::getDictId, id));
		return RestResponse.ok();
	}

	/**
	 * 更新字典
	 *
	 * @param dict 字典
	 * @return
	 */
	public RestResponse updateDict(Dict dict) {
		Dict tenantDict = this.getById(dict.getId());
		// 系统内置
		if (DictTypeEnum.SYSTEM.getType().equals(tenantDict.getSystem())) {
			return RestResponse.failed("系统内置字典不能修改");
		}
		return RestResponse.ok(this.updateById(dict));
	}
}
