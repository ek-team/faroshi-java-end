package cn.cuptec.faros.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "im_chat_room")
public class ImChatRoom implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;



    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;





    /**
     * 1.单聊
     */
    @TableField(value = "type")
    private Integer type;


    private Integer lastMsgId;

    private static final long serialVersionUID = 1L;


}