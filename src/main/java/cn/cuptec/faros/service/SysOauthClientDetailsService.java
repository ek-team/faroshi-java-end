package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.entity.SysOauthClientDetails;
import cn.cuptec.faros.mapper.SysOauthClientDetailsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class SysOauthClientDetailsService extends ServiceImpl<SysOauthClientDetailsMapper, SysOauthClientDetails> {

	/**
	 * 通过ID删除客户端
	 *
	 * @param id
	 * @return
	 */
	@CacheEvict(value = CacheConstants.CLIENT_DETAILS_KEY, key = "#id")
	public Boolean removeClientDetailsById(String id) {
		return this.removeById(id);
	}

	/**
	 * 根据客户端信息
	 *
	 * @param clientDetails
	 * @return
	 */
	@CacheEvict(value = CacheConstants.CLIENT_DETAILS_KEY, key = "#clientDetails.clientId")
	public Boolean updateClientDetailsById(SysOauthClientDetails clientDetails) {
		return this.updateById(clientDetails);
	}
}
