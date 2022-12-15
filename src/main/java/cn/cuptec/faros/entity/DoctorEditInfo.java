package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/17 14:28
 */
@Data
public class DoctorEditInfo {
    @TableId(type = IdType.AUTO)
    private String id;

    //姓名
    private String name;
    //手机号
    private String mobile;
    //简介
    private String introduction;
    //医院
    private Integer hospitalId;
    private String hospital;
    //创建人
    private int creatId;
    private int deptId;
    private String deptName;
    //二维码
    private String liveQrCodeId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;


}
