package cn.cuptec.faros.bo;

import cn.cuptec.faros.entity.ImChatRoom;

import cn.cuptec.faros.entity.ImMsg;
import cn.cuptec.faros.entity.ImUserRoomRelation;
import cn.cuptec.faros.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ImChatRoomBO implements Serializable {


    private static final long serialVersionUID = 5416524938354507909L;



    private ImChatRoom imChatRoomInfo;

    private List<User> joinUserInfo;



    private Integer unreadNum;

    private ImMsg lastImMsgInfo;

}
