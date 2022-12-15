package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TbPlanBackup implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long userId;//用户唯一id 静态方法获取唯一id编号
    private Long keyId;                 //唯一ID
    private Long planId;//计划id 静态方法获取唯一id编号
    private Integer planType;//训练计划类型 短期0，中期1，长期2
    private Date createDate;//创建时间
    private Date updateDate;//更新时间
    private String weight;//患者体重
    private Integer planTotalDay;//训练周期 （天）
    private Integer classId;//阶段Id （1到3）3个阶段
    private Integer planStatus;//计划状态 0未开始，1进行中，2完成
    private Date startDate;//开始时间wx8d3cacbced9f7d0a
    private Date endDate;//结束时间
    private Integer timeOfDay;//每天训练次数
    private Integer countOfTime;//每次训练步数
    @TableField(value = "`load`")
    private Integer load;//负重
    private Integer trainTime;//训练时间
    private Integer trainType;//训练方式 0按步数，1按时间
    private String remark;//备注
    private int trainStep;//训练步数
    private int modifyStatus;//修改状态
    private Integer backVersion = 1; //备份版本号
}
