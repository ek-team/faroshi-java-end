package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class DoctorUpdateSubPlanRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String userId;
    private String doctorName;
    //修改之前的版本
    private int beforeVersion;
    //修改之后的版本
    private int afterVersion;
    private Date createTime;
    //设备序列号
    private String productSn;
    //设备地址
    private String macAdd;
}
