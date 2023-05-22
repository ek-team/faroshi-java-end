package cn.cuptec.faros.im.bean;

import cn.cuptec.faros.entity.DoctorTeamPeople;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ChatUserVO implements Comparable<ChatUserVO> {

    /**
     * 用户id
     */
    private Integer targetUid;
    private String chatDesc;
    private Integer chatUserId;//群聊id
    private Integer groupType = 0;//0-单聊 1-群聊
    private String patientId;
    /**
     * 昵称
     */
    private String nickname;
    private String patientName;
    private LocalDateTime clearTime;
    /**
     * 头像
     */
    private String avatar;
    private String patientAvatar;
    private List<DoctorTeamPeople> doctorTeamPeopleList;
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
    private LocalDateTime serviceStartTime;//服务开始时间
    private LocalDateTime serviceEndTime;//服务结束时间
    /**
     * 是否在线
     */
    private int online;

    /**
     * 是否有新消息
     */
    private int hasNewMsg;

    @Override
    public int compareTo(ChatUserVO o) {
        return o.lastChatTime.compareTo(this.lastChatTime);//根据时间降序
    }
}


