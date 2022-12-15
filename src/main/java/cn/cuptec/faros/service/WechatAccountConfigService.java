package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.entity.WechatAccountConfig;
import cn.cuptec.faros.mapper.UserOrderMapper;
import cn.cuptec.faros.mapper.WechatAccountConfigMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/17 10:20
 */
@Service
public class WechatAccountConfigService extends ServiceImpl<WechatAccountConfigMapper, WechatAccountConfig> {
    @Resource
    private UserService userService;

    public List<Integer> selectByDepId(int depId){
        return   baseMapper.selectByDepId(depId);
    }

   public WechatAccountConfig getByUid(int uid){
       //查询服务号配置
       WechatAccountConfig wechatAccountConfig = getOne(Wrappers.<WechatAccountConfig>lambdaQuery().eq(WechatAccountConfig::getUid, uid));
       if (wechatAccountConfig == null) {
           //查询上级
           Integer depId = userService.getById(uid).getDeptId();
           //查询该部门下的一级业务员
           List<Integer> uIds = selectByDepId(depId);
           if (!CollectionUtils.isEmpty(uIds)) {
               Integer depUid = uIds.get(0);
               wechatAccountConfig = getOne(Wrappers.<WechatAccountConfig>lambdaQuery().eq(WechatAccountConfig::getUid, depUid));

           }
       }
       return wechatAccountConfig;
    }

    public WechatAccountConfig getByMchId(String mchId){
        //查询服务号配置

        return    this.getOne(Wrappers.<WechatAccountConfig>lambdaQuery().eq(WechatAccountConfig::getMchId, mchId).last("limit 1"));
    }
}
