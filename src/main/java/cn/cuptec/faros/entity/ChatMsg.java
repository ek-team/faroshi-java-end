package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@ApiModel("聊天消息")
@Data
public class ChatMsg extends Model<ChatMsg> {

    @TableId(type = IdType.AUTO)
    private String id;
    private Integer chatUserId;//群聊消息id
    private String readUserIds;//已读人用户id
    /**
     * 消息类型
     * 数据字典MsgType TEXT-文本  IMG-图片
     * AUDIO-音频 ORDER-订单通知  VIDEO-音频 RED_ENVELOPE-红包
     * ACCOMPANYING_ORDER-陪诊单 REFERRAL-转诊单
     * INQUIRY-问诊单
     */
    private String msgType;

    /**
     * 发送者id
     */
    private Integer fromUid;

    /**
     * 接收者id
     */
    private Integer toUid;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 消息创建时间
     */
    private Date createTime;

    /**
     * 是否已撤回
     */
    private int canceled;

    /**
     * 是否已推送
     */
    private int pushed;

    /**
     * 是否已读
     * 0未读 1-已读
     */
    private int readStatus;
    //图片
    private String url;

    private String str1;
    private String str2;
    private String str3;
    private String str4;
    private String videoDuration;//音频时长
    /**
     * 语音是否已读 1-未读 2-已读
     */
    private Integer videoRead = 1;
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private PatientOtherOrder patientOtherOrder;//图文咨询
    @TableField(exist = false)
    private FollowUpPlanNotice followUpPlanNotice;//随访计划
}

