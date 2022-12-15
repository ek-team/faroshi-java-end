package cn.cuptec.faros.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * Created by zhanglun on 2021/5/20
 * Describe:
 */
@Data
public class TbTrainData  {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long userId;//用户唯一id 静态方法获取唯一id编号
    private Long keyId;                 //唯一ID
    private Long createDate;
    private Integer frequency;//当天第几次
    private Integer targetLoad;//目标负重
    private Integer realLoad;//实际负重
    private Long planId;
    private Integer classId;
    private Integer isUpload;

    private String dateStr;
    private Integer recordId;

}
