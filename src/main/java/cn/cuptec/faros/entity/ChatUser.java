package cn.cuptec.faros.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 聊天列表(用户和那些人聊过天)
 */
@Data
public class ChatUser extends Model<ChatUser> implements Comparable<ChatUser> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户id
     */
    //@TableId(type = IdType.INPUT)

    private Integer uid;
    private String chatDesc;//咨询还是随访
    private String userIds;//群聊用户id
    private Integer groupType;//0-单聊 1-群聊
    private Integer teamId;//团队id
    private String msgId;
    private Integer receiverId;//图文咨询接收人id
    private Integer receiverStatus=0;//接收完之后是否回话 0-未回 1-已回
    /**
     * 聊天对象用户id
     */
    //@TableId(type = IdType.INPUT)
    @TableField
    private Integer targetUid;
    private String patientOtherOrderNo;//图文咨询订单id
    /**
     * 对话是否已关闭
     */
    private int isClosed;
    private String patientOtherOrderStatus;//图文咨询状态//0-待接收 1-接收 2-拒绝 3-结束
    /**
     * 最后一条消息
     */
    private String lastMsg;

    /**
     * 最后聊天时间
     */
    private Date lastChatTime;
    private Integer chatCount = 9;//医生未接受消息的时候 可以再发送9条消息
    private String remark;
    private LocalDateTime serviceStartTime;//服务开始时间
    private LocalDateTime serviceEndTime;//服务结束时间
    /**
     * 清空聊天记录时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clearTime;
    /**
     * 是否有新消息
     */
    @TableField(exist = false)
    private int hasNewMsg;
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private int status;//1-有效 2-无效

    @Override
    public int compareTo(ChatUser o) {
        return o.lastChatTime.compareTo(this.lastChatTime);//根据时间降序
    }
}
