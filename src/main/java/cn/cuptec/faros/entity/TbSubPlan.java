package cn.cuptec.faros.entity;


import cn.cuptec.faros.im.bean.ChatUserVO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * Created by zhanglun on 2021/5/7
 * Describe:
 */
@Data
public class TbSubPlan implements Comparable<TbSubPlan>{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long userId;//用户唯一id 静态方法获取唯一id编号
    private Integer doctorId;
    private Long keyId;                 //唯一ID
    private Long planId;//计划id 静态方法获取唯一id编号
    private Integer planStatus;//计划状态 0未开始，1进行中，2完成
    private Integer classId;//阶段Id （1到3）3个阶段
    @TableField(value = "`load`")
    private Integer load;//负重
    @TableField(exist = false)
    private Integer endLoad;//负重
    @TableField(exist = false)
    private String weight;//患者体重
    @TableField(exist = false)
    private String startDateStr;
    @TableField(exist = false)
    private String endDateStr;
    private Integer weekNum;//第几周
    private Integer dayNum;//第几天
    private Date startDate;
    private Date endDate;
    private Date createDate;//创建时间
    private Date updateDate;//更新时间
    //版本记录
    private Integer version;
    private Integer planInvalid=0;// 0合法 1-不合法
    private Integer tbPlanId;
    private Integer trainTime;//训练时间
    private Integer trainStep;//训练步数 踩踏次数
    private int modifyStatus;//修改状态  0未修改，1云端修改，2设备端修改
    private int initStart;//是否是初使计划 1-是 2-不是
    @TableField(exist = false)
    private Integer newStatus;
    @TableField(exist = false)
    private Integer updateStatus;//同步到老平台的状态

    @Override
    public int compareTo(TbSubPlan o) {
        return this.startDate.compareTo(o.startDate);//根据时间降序
    }
 }
