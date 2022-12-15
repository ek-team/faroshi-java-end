package cn.cuptec.faros.im.bean;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ChatUserVO implements Comparable<ChatUserVO> {

    /**
     * 用户id
     */
    private Integer targetUid;

    /**
     * 昵称
     */
    private String nickname;
    private LocalDateTime clearTime;
    /**
     * 头像
     */
    private String avatar;

    /**
     * 最后通讯时间
     */
    private Date lastChatTime;

    /**
     * 最后一条消息
     */
    private String lastMsg;
    //备注
    private String remark;
    /**
     * 对话是否已关闭
     */
    private int isClosed;

    /**
     * 是否在线
     */
    private int online;

    /**
     * 是否有新消息
     */
    private int hasNewMsg;

    /**
     * 未读消息数量
     */
    private int noReadCount;

    @Override
    public int compareTo(ChatUserVO o) {
        return o.lastChatTime.compareTo(this.lastChatTime);//根据时间降序
    }
}


