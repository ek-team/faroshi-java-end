package cn.cuptec.faros.entity;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@TableName(value = "im_msg")
public class ImMsg implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "msg_text")
    @JSONField(serialize = false)
    private String msgText;

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

    @TableField(value = "room_id")
    private Integer roomId;

    @TableField(value = "msg_key")
    private String msgKey;

    @TableField(value = "msg_content")
    @JSONField(serializeUsing = ConverObjetcSerializer.class)
    private String msgContent;



    private static final long serialVersionUID = 1L;


    static public class ConverObjetcSerializer implements ObjectSerializer {



        @Override
        public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
            JSONArray jsonArray = new JSONArray();

            if (o != null) jsonArray = JSONArray.parseArray(o.toString());
            jsonSerializer.write(jsonArray);
        }
    }
}