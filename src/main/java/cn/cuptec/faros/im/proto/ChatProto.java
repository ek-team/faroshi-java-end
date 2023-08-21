package cn.cuptec.faros.im.proto;

/**
 * 聊天的协议
 */
public class ChatProto {

    //双向消息类型
    public static final String BASE_PING = "PING"; //ping消息
    public static final String BASE_PONG = "PONG"; //pong消息

    //系统请求
    public static final String REQUEST_AUTH = "REQUEST_AUTH"; //认证消息 用户-》系统


    public static final String REQUEST_READ = "REQUEST_READ"; //聊天已读
    public static final String RESPONSE_READ = "RESPONSE_READ"; //聊天已读



    //用户通信
    public static final String MESSAGE_PIC = "MESSAGE_PIC";
    public static final String MESSAGE_TEXT = "MESSAGE_TEXT";
    public static final String MESSAGE_FILE = "MESSAGE_FILE";

    public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";

    public static final String REMOVE_CHANNEL = "REMOVE_CHANNEL"; //关闭会话
    public static final String RESPONSE_CLOSECHAT = "RESPONSE_CLOSECHAT"; //关闭会话
    //提示类消息
    public static final String TIP_NEWMESSAGE = "TIP_NEWMESSAGE"; //新聊天消息提醒 系统-》用户
    public static final String TIP_GROUP_NEWMESSAGE = "TIP_GROUP_NEWMESSAGE"; //新聊天消息提醒 系统-》用户
    public static final String TIP_ERROR = "TIP_ERROR"; //错误消息 系统-》用户
    public static final String AUTH_RESULT = "AUTH_RESULT"; //认证结果
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";


    //音频消息
    public static final String VIDEO = "VIDEO";
    //视频频消息
    public static final String VIDEO_URL = "VIDEO_URL";
    //转发消息
    public static final String FORWARD = "FORWARFD";
    //音频已读
    public static final String REQUEST_VIDEO_READ = "REQUEST_VIDEO_READ";
    public static final String RESPONSE_VIDEO_READ = "RESPONSE_VIDEO_READ"; //聊天已读

    public static final String FOLLOW_UP_PLAN = "FOLLOW_UP_PLAN"; //随访计划消息类型
    public static final String PIC_CONSULTATION = "PIC_CONSULTATION"; //图文咨询消息类型
    public static final String CONFIRM_STATUS = "CONFIRM_STATUS"; //咨询消息医生确认消息
    public static final String FORM = "FORM"; //表单消息
    public static final String ARTICLE = "ARTICLE"; //表单消息
    public static final String SYSTEM_NOTIC = "SYSTEM_NOTIC"; //系统消息

    public static final String ADD_ORDER_COUNT = "ADD_ORDER_COUNT"; //订单数加1
    public static final String PRODUCT_STOCK_USER_COUNT = "PRODUCT_STOCK_USER_COUNT"; //设备注册人数加1
    public static final String PRODUCT_STOCK_TRAIN_RECORD_COUNT = "PRODUCT_STOCK_TRAIN_RECORD_COUNT"; //训练记录人数加1
}

