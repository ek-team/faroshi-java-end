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
    private String userIds;//群聊用户id
    private Integer groupType = 0;//0-单聊 1-群聊
    private Integer teamId;//团队id
    /**
     * 聊天对象用户id
     */
    //@TableId(type = IdType.INPUT)
    @TableField
    private Integer targetUid;

    /**
     * 对话是否已关闭
     */
    private int isClosed;

    /**
     * 最后一条消息
     */
    private String lastMsg;

    /**
     * 最后聊天时间
     */
    private Date lastChatTime;

    private String remark;

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

    @Override
    public int compareTo(ChatUser o) {
        return o.lastChatTime.compareTo(this.lastChatTime);//根据时间降序
    }
}
