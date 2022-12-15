package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("na_li_user_info")
public class NaLiUserInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private LocalDateTime createTime;
    private String orderId;
    private String idCard;
    private String productSn;

}
