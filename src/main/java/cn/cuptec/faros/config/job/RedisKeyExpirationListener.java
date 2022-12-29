package cn.cuptec.faros.config.job;

import cn.cuptec.faros.config.properties.RedisConfigProperties;
import cn.cuptec.faros.service.ChatUserService;
import cn.cuptec.faros.service.UserOrdertService;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * redis过期监听
 * 1、自动取消订单
 * 2、自动收货
 */
@Slf4j
@Component
public class RedisKeyExpirationListener implements MessageListener {

    private RedisTemplate<String, String> redisTemplate;
    private RedisConfigProperties redisConfigProperties;
    private UserOrdertService userOrdertService;
    private ChatUserService chatUserService;

    public static final String URL = "/pages/orderConfirm/orderConfirm?id=";

    public RedisKeyExpirationListener(RedisTemplate<String, String> redisTemplate,
                                      RedisConfigProperties redisConfigProperties,
                                      UserOrdertService userOrdertService,
                                      ChatUserService chatUserService
    ) {
        this.redisTemplate = redisTemplate;
        this.redisConfigProperties = redisConfigProperties;
        this.userOrdertService = userOrdertService;
        this.chatUserService = chatUserService;

    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        RedisSerializer<?> serializer = redisTemplate.getValueSerializer();
        String channel = String.valueOf(serializer.deserialize(message.getChannel()));
        String body = String.valueOf(serializer.deserialize(message.getBody()));
        log.info("key过期监听进入//////////////////////////////////////////////" + body);
        //key过期监听
        if (StrUtil.format("__keyevent@{}__:expired", redisConfigProperties.getDatabase()).equals(channel)) {
            //订单自动取消
            if (body.contains("mall:order:is_pay_0:")) {
                String[] str = body.split(":");
                String wxOrderId = str[3];
                log.info("redis过期监听：：=============" + wxOrderId);

            }

        }
    }
}
