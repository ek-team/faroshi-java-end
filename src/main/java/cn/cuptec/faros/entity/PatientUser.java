package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 就诊人管理
 */
@Data
public class PatientUser {
    @TableId(type = IdType.AUTO)
    private String id;
    private String name;
    private String idCard;
    private String age;
    private Integer sex; //0-男 1-女
    private String phone;
    private Integer userId;
}
