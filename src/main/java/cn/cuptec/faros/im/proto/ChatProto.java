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
    public static final String INQUIRY="INQUIRY";//问诊单
    public static final String PRESCRIPTION_INFO="PRESCRIPTION_INFO";//处方单
    //产品介绍的消息类型
    public static final String MESSAGE_PRODUCT_INFO = "MESSAGE_PRODUCT_INFO";
    public static final String MESSAGE_NEWRECIPE = "MESSAGE_NEWRECIPE";
    public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";


    //提示类消息
    public static final String TIP_NEWMESSAGE = "TIP_NEWMESSAGE"; //新聊天消息提醒 系统-》用户
    public static final String TIP_GROUP_NEWMESSAGE = "TIP_GROUP_NEWMESSAGE"; //新聊天消息提醒 系统-》用户
    public static final String TIP_ERROR = "TIP_ERROR"; //错误消息 系统-》用户
    public static final String AUTH_RESULT = "AUTH_RESULT"; //认证结果
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";


    //音频消息
    public static final String VIDEO = "VIDEO";
    //红包
    public static final String RED_ENVELOPE = "RED_ENVELOPE";
    //文章
    public static final String ARTICLE = "ARTICLE";
    //转发消息
    public static final String FORWARD = "FORWARD";
    //音频已读
    public static final String REQUEST_VIDEO_READ = "REQUEST_VIDEO_READ";
    public static final String RESPONSE_VIDEO_READ = "RESPONSE_VIDEO_READ"; //聊天已读



}

