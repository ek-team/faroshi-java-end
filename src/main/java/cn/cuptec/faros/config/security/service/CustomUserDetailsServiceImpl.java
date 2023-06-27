package cn.cuptec.faros.config.security.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详细信息
 */
@Slf4j
@Service
@AllArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    private final UserService userService;

    private final CacheManager cacheManager;

    /**
     * 用户密码登录
     *
     * @param phoneNo 用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String phoneNo) {
//		Cache cache = cacheManager.getCache(CacheConstants.USER_DETAILS);
//		if (cache != null && cache.get(phone) != null) {
//			return (CustomUser) cache.get(phone).get();
//		}
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phoneNo));
        if (user != null) {
            user = userService.refactByUser(user);
        }
        UserDetails userDetails = getUserDetails(user);
        //cache.put(phone, userDetails);
        return userDetails;
    }

    /**
     * 根据社交登录code 登录
     *
     * @param inStr TYPE@CODE
     * @return UserDetails
     * @throws UsernameNotFoundException
     */

    @Override
    @SneakyThrows
    public UserDetails loadUserBySocial(String inStr) {
        log.info("登录===================" + inStr + "====" + inStr);
        User user = userService.getBySocialParam(inStr);
        UserDetails userDetails = getUserDetails(user);
        log.info("登录返回===================" + inStr + "======" + userDetails.toString());
        return userDetails;
    }

    /**
     * 构建userdetails
     */
    private UserDetails getUserDetails(User user) {
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        Set<String> dbAuthsSet = new HashSet<>();
        if (ArrayUtil.isNotEmpty(user.getRoles())) {
            // 获取角色
            Arrays.stream(user.getRoles()).forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role.getId()));
            // 获取资源
            dbAuthsSet.addAll(Arrays.stream(user.getPermissions()).map(permission -> permission.getPermission()).filter(permission -> StringUtils.isNotEmpty(permission)).collect(Collectors.toList()));
        }
        Collection<? extends GrantedAuthority> authorities
                = AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
        log.info("构建userdetails...");
        log.info(JSON.toJSONString(user));
        boolean enabled = StrUtil.equals(user.getLockFlag(), CommonConstants.STATUS_NORMAL);
        // 构造security用户
        //imService.updateImState(user);
        return new CustomUser(user.getId(), user.getDeptId(), user.getNickname(), user.getAvatar(), user.getPhone(), SecurityConstants.BCRYPT + user.getPassword(), enabled,
                true, true, !CommonConstants.STATUS_LOCK.equals(user.getLockFlag()), authorities);
    }

}
