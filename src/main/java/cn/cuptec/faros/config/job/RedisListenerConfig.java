package cn.cuptec.faros.config.job;

import cn.cuptec.faros.config.properties.RedisConfigProperties;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.Resource;

/**
 * redis监听器配置
 */
@Configuration
@AllArgsConstructor
public class RedisListenerConfig {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisConfigProperties redisConfigProperties;
    private final UserOrdertService userOrdertService;
    private final ChatUserService chatUserService;
    private final FollowUpPlanNoticeService followUpPlanNoticeService;
    private final WxMpService wxMpService;
    private final UserService userService;
    private final HospitalInfoService hospitalInfoService;
    private final ChatMsgService chatMsgService;
    private final  FollowUpPlanNoticeCountService followUpPlanNoticeCountService;
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new RedisKeyExpirationListener(redisTemplate, redisConfigProperties, userOrdertService, chatUserService,followUpPlanNoticeService,wxMpService,userService,hospitalInfoService,chatMsgService,followUpPlanNoticeCountService), new PatternTopic(StrUtil.format("__keyevent@{}__:expired", redisConfigProperties.getDatabase())));
        return container;
    }
}