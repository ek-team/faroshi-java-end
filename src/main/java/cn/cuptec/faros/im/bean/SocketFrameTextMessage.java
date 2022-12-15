package cn.cuptec.faros.im.bean;

import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.im.proto.ChatProto;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class SocketFrameTextMessage {

    //后置表单id
    private String formId;
    private String videoDuration;//音频时长
    //备注
    private String remark;

    private String str1;
    private String str2;
    //1-医生 2-患者
    private Integer type;

    //图片
    private String url;
    //用户信息
    private String userInfo;
    private String cId;
    /**
     * 消息类型 对应ChatProto
     */
    private String msgType;

    /**
     * 返回的数据
     */
    private Object data;

    /**
     * 消息
     */
    private String msg;
    /**
     * 消息id
     */
    private String msgId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /**
     * 目标用户id
     */
    private Integer targetUid;

    /**
     * 发送者id
     */
    private Integer fromUid;

    /**
     * 发送者用户名
     */
    private String fromUname;

    /**
     * 发送者头像
     */
    private String fromAvatar;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 分页大小
     */
    private Integer pageSize;
    /**
     * 未读消息数量
     */
    private Integer noReadCount;

    /**
     * 清空记录时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clearTime;
    /**
     * uid
     */
    private Integer myUserId;

    /**
     * 消息时间戳
     */
    private Long msgTimeStamp;


    /**
     * 校验通信参数
     *
     * @return
     */
    public boolean validateP2P() {
        if (StringUtils.isEmpty(msg) || targetUid == null) {
            return false;
        }
        return true;
    }

    public static SocketFrameTextMessage message(String messageType, Object data, Integer targerUid, Integer fromUid, String fromUname, String fromAvatar) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = messageType;
        message.data = data;
        message.targetUid = targerUid;
        message.fromUid = fromUid;
        message.fromUname = fromUname;
        message.fromAvatar = fromAvatar;
        return message;
    }
    public static SocketFrameTextMessage responseSetVideoReaded(String msgId) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.RESPONSE_VIDEO_READ;
        message.msgId = msgId;
        return message;
    }
    public static SocketFrameTextMessage responseSetReaded(Long msgTimeStamp) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.RESPONSE_READ;
        message.time = new Date();
        message.msgTimeStamp = msgTimeStamp;
        return message;
    }
    public static SocketFrameTextMessage newMessageTip(Integer fromUid, String fromUname, String fromAvatar, Date messageTime, String msgType, String msg) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.TIP_NEWMESSAGE;
        message.fromUid = fromUid;
        message.fromUname = fromUname;
        message.fromAvatar = fromAvatar;
        message.msgTimeStamp = messageTime.getTime();


        message.msg = msg;
        return message;
    }

    public static SocketFrameTextMessage error(String msg) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.TIP_ERROR;
        message.msg = msg;
        message.time = new Date();
        return message;
    }


    public static SocketFrameTextMessage error(String msg, Object data) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.TIP_ERROR;
        message.msg = msg;
        message.time = new Date();
        message.data = data;
        return message;
    }

    public static SocketFrameTextMessage request(String messageType, String msg, Object data) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = messageType;
        message.msg = msg;
        message.data = data;
        return message;
    }

    public static SocketFrameTextMessage ping() {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.BASE_PING;
        message.time = new Date();
        return message;
    }

    public static SocketFrameTextMessage pong(int noReadCount) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.BASE_PONG;
        message.time = new Date();
        message.setNoReadCount(noReadCount);
        return message;
    }


    public static SocketFrameTextMessage authRequired() {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.AUTH_REQUIRED;
        message.time = new Date();
        return message;
    }

    public static SocketFrameTextMessage authResult(boolean authed) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.AUTH_RESULT;
        message.time = new Date();
        message.data = authed;
        return message;
    }

    public static SocketFrameTextMessage responseMessage(ChatMsg chatMsg) {
        SocketFrameTextMessage message = new SocketFrameTextMessage();
        message.msgType = ChatProto.RESPONSE_MESSAGE;
        message.time = new Date();
        message.data = chatMsg;
        return message;
    }




}
