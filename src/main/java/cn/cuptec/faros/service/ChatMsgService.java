package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.mapper.ChatMsgMapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class ChatMsgService extends ServiceImpl<ChatMsgMapper, ChatMsg> {
    public boolean setReaded(Integer uid, Integer targetUid) {

        return update(Wrappers.<ChatMsg>lambdaUpdate()
                .set(ChatMsg::getReadStatus, 1)
                .eq(ChatMsg::getToUid, uid)
                .eq(ChatMsg::getFromUid, targetUid)
                .eq(ChatMsg::getReadStatus, 0)
        );
    }
}
