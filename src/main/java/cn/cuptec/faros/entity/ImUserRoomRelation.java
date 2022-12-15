package cn.cuptec.faros.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "im_user_room_relation")
@Builder
public class ImUserRoomRelation implements Serializable {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Integer userId;

    @TableId(value = "room_id", type = IdType.INPUT)
    private Integer roomId;

    /**
     * 是否展示 0,显示 1,。不展示
     */

    @JSONField(deserialize = false, serialize = false)
    private Integer isShow;



    private Integer unreadNum;


    @TableField(value = "create_time")
    private Date createTime;

    private Integer roomType;

    private static final long serialVersionUID = 1L;
}