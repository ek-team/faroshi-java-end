package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.web.httpMessageConverter.OAuth2AccessTokenMessageConverter;
import cn.cuptec.faros.service.TokenService;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 删除token端点
 */
@RestController
@AllArgsConstructor
@RequestMapping("/token")
public class TokenController {

	public static final String OAUTH_ACCESS = SecurityConstants.CUP_PREFIX + SecurityConstants.OAUTH_PREFIX + "auth_to_access:";
	public static final String OAUTH = SecurityConstants.CUP_PREFIX + SecurityConstants.OAUTH_PREFIX + "auth:";
	private final RedisTemplate redisTemplate;
	private final TokenStore tokenStore;
	private final CacheManager cacheManager;

	@GetMapping("user")
	public RestResponse<CustomUser> customUserInfo(){
		return RestResponse.ok(SecurityUtils.getUser());
	}

	@ApiOperation(value = "检查token是否有效")
	@ApiImplicitParam(name = "")
	@GetMapping("/checkToken")
	public RestResponse checkToken(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
		if (StrUtil.isBlank(authHeader)) {
			return RestResponse.failed("token 不能为空", Boolean.FALSE);
		}

		String tokenValue = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
		if (accessToken == null || StrUtil.isBlank(accessToken.getValue())) {
			return RestResponse.failed("token 无效");
		}
		if (accessToken.isExpired()){
			return RestResponse.failed("token已过期");
		}
		return RestResponse.ok();
	}

	/**
	 * 退出token
	 *
	 * @param authHeader Authorization
	 */
	@DeleteMapping("/logout")
	public RestResponse logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		if (StrUtil.isBlank(authHeader)) {
			return RestResponse.failed("退出失败，token 为空", Boolean.FALSE);
		}

		String tokenValue = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
		if (accessToken == null || StrUtil.isBlank(accessToken.getValue())) {
			return RestResponse.failed("退出失败，token 无效", Boolean.TRUE);
		}
		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(accessToken);
		// 清空用户信息
		cacheManager.getCache(CacheConstants.USER_DETAILS)
				.evict(auth2Authentication.getName());

		// 清空access token
		tokenStore.removeAccessToken(accessToken);

		// 清空 refresh token
		OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
		tokenStore.removeRefreshToken(refreshToken);
		return RestResponse.ok(Boolean.TRUE);
	}

	/**
	 * 令牌管理调用
	 *
	 * @param token token
	 * @return
	 */
    @PreAuthorize("@pms.hasPermission('token_delete')")
	@DeleteMapping("/{token}")
	public RestResponse<Boolean> delToken(@PathVariable("token") String token) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
		tokenStore.removeAccessToken(oAuth2AccessToken);
		return new RestResponse<>();
	}


	/**
	 * 查询token
	 */
    @PreAuthorize("@pms.hasPermission('token_manage')")
	@GetMapping("/page")
	public RestResponse<Page> tokenList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
		//根据分页参数获取对应数据
		String key = String.format("%s*", OAUTH_ACCESS);
		List<String> pages = findKeysForPage(key, pageNum, pageSize);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		Page result = new Page(pageNum, pageSize);

        List<OAuth2AccessToken> tokenInfos = redisTemplate.opsForValue().multiGet(pages);
        List<Map<String, Object>> resultTokens = tokenInfos.stream().map(accessToken -> {
            Map<String, Object> data = new HashMap<>(8);
            data.put(SecurityConstants.DETAILS_USER_ID, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_USER_ID));
            data.put(SecurityConstants.DETAILS_USERNAME, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_USERNAME));
            data.put(SecurityConstants.DETAILS_DEPT_ID, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_DEPT_ID));
            data.put(SecurityConstants.DETAILS_NICKNAME, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_NICKNAME));
            data.put(SecurityConstants.DETAILS_AVATAR, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_AVATAR));
            data.put(SecurityConstants.DETAILS_LICENSE, SecurityConstants.AUTHOR);
            data.put(OAuth2AccessToken.EXPIRES_IN, accessToken.getExpiresIn());
            data.put(OAuth2AccessToken.SCOPE,accessToken.getScope());
            data.put(OAuth2AccessToken.ACCESS_TOKEN, accessToken.getValue());
            data.put(OAuth2AccessToken.TOKEN_TYPE, accessToken.getTokenType());
            if (accessToken instanceof DefaultOAuth2AccessToken){
                data.put(OAuth2AccessTokenMessageConverter.EXPIRED, ((DefaultOAuth2AccessToken)accessToken).isExpired());
            }
            return data;
        }).collect(Collectors.toList());

        result.setRecords(resultTokens);
		result.setTotal(Long.valueOf(redisTemplate.keys(key).size()));

		return RestResponse.ok(result);
	}

	private List<String> findKeysForPage(String patternKey, int pageNum, int pageSize) {
		ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
//		RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();//默认JKDSerializer,会出错
        StringRedisSerializer redisSerializer = new StringRedisSerializer();
		Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
		List<String> result = new ArrayList<>();
		int tmpIndex = 0;
		int startIndex = (pageNum - 1) * pageSize;
		int end = pageNum * pageSize;

		assert cursor != null;
		while (cursor.hasNext()) {
			if (tmpIndex >= startIndex && tmpIndex < end) {
                Object next = cursor.next();
                result.add(next.toString());
				tmpIndex++;
				continue;
			}
			if (tmpIndex >= end) {
				break;
			}
			tmpIndex++;
			cursor.next();
		}
		return result;
	}

}
