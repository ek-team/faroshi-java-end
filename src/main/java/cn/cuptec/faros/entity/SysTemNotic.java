package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/***
 * 系统通知 发送通知APp
 */
@Data
public class SysTemNotic {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer type;//1记录异常通知 2-计划修改医生确认通知
    private LocalDateTime createTime;
    private Integer doctorId;
    private Integer teamId;
    private Integer readStatus;//1-未读 0-已读
    private String patientUserId;//用户id
    private String stockUserId;//设备用户id
    private String keyId;
    private Integer chatUserId;
    private Integer checkStatus=1;//1待审核 2审核通过
    @TableField(exist = false)
    private TbTrainUser tbTrainUser;
}
