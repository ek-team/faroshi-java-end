package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.config.web.httpMessageConverter.OAuth2AccessTokenMessageConverter;
import cn.cuptec.faros.controller.TokenController;
//import cn.cuptec.faros.entity.Token;
//import cn.cuptec.faros.mapper.TokenMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Resource
    private  RedisTemplate redisTemplate;


    public void delTokenByUserId(Integer userId){
        String key = String.format("%s*", TokenController.OAUTH_ACCESS);


        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());

        Set<String> keys = redisTemplate.keys(key);

        if(CollUtil.isEmpty(keys)) return;
        List<OAuth2AccessToken> tokenInfos = redisTemplate.opsForValue().multiGet(keys);

        List<String> oauthKeyList = tokenInfos.stream().filter(accessToken -> userId.equals(accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_USER_ID)))
                .map(accessToken -> TokenController.OAUTH + accessToken.getValue())
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(oauthKeyList)) redisTemplate.delete(oauthKeyList);
    }

}