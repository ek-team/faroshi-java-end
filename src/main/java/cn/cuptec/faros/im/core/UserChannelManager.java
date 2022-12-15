package cn.cuptec.faros.im.core;

import cn.cuptec.faros.config.properties.WsProperties;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.util.NettyUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Channel的管理器以及user管理工具类
 */
@Slf4j
@Component
public class UserChannelManager {

    private static WsProperties staticWsProperties;
    @Resource
    private WsProperties wsProperties;

    private static final Logger logger = LoggerFactory.getLogger(UserChannelManager.class);

    public static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    public static ConcurrentMap<Channel, SocketUser> userChannelMap = new ConcurrentHashMap<>();

    private static AtomicInteger userCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        staticWsProperties = wsProperties;
    }

    public static void addChannel(Channel channel) {
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        System.out.println("addChannel:" + remoteAddr);
        if (!channel.isActive()) {
            logger.error("channel is not active, address: {}", remoteAddr);
        }
        SocketUser userInfo = new SocketUser();
        userInfo.setAddr(remoteAddr);
        userInfo.setTime(System.currentTimeMillis());
        userChannelMap.put(channel, userInfo);
    }



    public static boolean saveUser(Channel channel, SocketFrameTextMessage message) {
        SocketUser userInfo = userChannelMap.get(channel);
        if (userInfo == null) {
            return false;
        }
        if (!channel.isActive()) {
            logger.error("channel is not active, address: {}, token: {}", userInfo.getAddr(), message.getUserInfo());
            return false;
        }

        User tenantUser = JSONObject.parseObject(message.getUserInfo(), User.class);
        logger.info("认证用户信息:{}" , tenantUser);
        // 增加一个认证用户
        //如果用户之前已连接，下线
        Channel preChannel = UserChannelManager.getUserChannel(tenantUser.getId());
        if (preChannel != null) {
            logger.info("用户之前已连接，下线" + tenantUser.getNickname());

            removeChannel(preChannel);
        }
        userCount.incrementAndGet();
        userInfo.setAuth(true);
        userInfo.setTime(System.currentTimeMillis());
        userInfo.setUserInfo(tenantUser);
        return true;
    }

    /**
     * 从缓存中移除Channel，并且关闭Channel
     *
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        try {
            SocketUser socketUser = getUserInfo(channel);

            //加上读写锁保证移除channel时，避免channel关闭时，还有别的线程对其操作，造成错误
            rwLock.writeLock().lock();
            channel.close();
            SocketUser userInfo = userChannelMap.get(channel);
            if (userInfo != null) {
                if (userInfo.isAuth()) {
                    // 减去一个认证用户
                    userCount.decrementAndGet();
                }
                userChannelMap.remove(channel);
            }
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    /**
     * 广播系统消息
     */
    public static void broadCastInfo(String code, ChatProto msg) {
        try {
            rwLock.readLock().lock();
            Set<Channel> keySet = userChannelMap.keySet();
            for (Channel ch : keySet) {
                SocketUser socketUser = userChannelMap.get(ch);
                if (socketUser == null || !socketUser.isAuth()) {
                    continue;
                }
                ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 广播ping消息
     */
    public static void broadCastPing() {
        try {
            rwLock.readLock().lock();
            //logger.info("broadCastPing userCount: {},{},{}", userCount.intValue(), "------", userChannelMap.size());
            Set<Channel> keySet = userChannelMap.keySet();
            StringBuffer name = new StringBuffer();
            for (Channel ch : keySet) {
                SocketUser socketUser = userChannelMap.get(ch);
                //如果channel是没有用户信息或者没有授权的用户则跳过
                if (socketUser == null || !socketUser.isAuth()) {
                    continue;
                }
                ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.ping())));
                name.append(socketUser.getUserInfo().getNickname() + "==");
            }
            logger.info("在线人昵称: {}", name);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 扫描并关闭失效的Channel
     */
    public static void scanNotActiveChannel() {
        Set<Channel> keySet = userChannelMap.keySet();
        for (Channel ch : keySet) {
            SocketUser socketUser = userChannelMap.get(ch);
            if (socketUser == null) {
                continue;
            }
            //如果channel没有打开或者激活或者验证用户信息时间超过10s就认为这是一个无效channel，应该移除
            if (!ch.isOpen() || !ch.isActive() || (!socketUser.isAuth() &&
                    (System.currentTimeMillis() - socketUser.getTime()) > 10000)) {
                logger.info("如果channel没有打开或者激活或者验证用户信息时间超过10s就认为这是一个无效channel，应该移除");
                removeChannel(ch);
            }
        }



    }

    /**
     * 获取用户信息
     *
     * @param channel
     * @return
     */
    public static SocketUser getUserInfo(Channel channel) {
        if (userChannelMap.containsKey(channel))
            return userChannelMap.get(channel);
        return null;
    }

    /**
     * 根据用户id获取用户通道
     *
     * @param uid
     * @return
     */
    public static Channel getUserChannel(Integer uid) {
        for (Channel channel : userChannelMap.keySet()) {
            SocketUser socketUser = userChannelMap.get(channel);
            if (socketUser.isAuth() && socketUser.getUserInfo().getId().equals(uid)) {
                return channel;
            }
        }
        return null;
    }

    /**
     * 获取已连接的用户数量
     *
     * @return
     */
    public static int getAuthUserCount() {
        return userCount.get();
    }

    /**
     * 更新用户最后活动时间
     *
     * @param channel
     */
    public static void updateUserTime(Channel channel) {
        SocketUser userInfo = getUserInfo(channel);
        if (userInfo != null) {
            userInfo.setTime(System.currentTimeMillis());
        }
    }


}
