package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("agent_phone")
public class AgentPhone {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String phone;
}
