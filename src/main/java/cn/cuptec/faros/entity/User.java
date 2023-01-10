package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 用户表
 */
@TableName("`user`")
@Data
public class User extends Model<User> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String phone;
    private String cid;//APP消息推送 cid
    @JSONField(serialize = false)
    private String password;

    @JSONField(deserialize = false)
    private String salt;

    @JSONField(deserialize = false)
    private String maOpenId;

    @JSONField(deserialize = false)
    private String mpOpenId;

    //唯一键
    @JSONField(deserialize = false)
    private String unionId;

    @Queryable(queryLogical = QueryLogical.LIKE)
    private String nickname;

    private String gender;

    private String language;
    private int type = 0;//0-系统用户 1-微信用户
    private String city;

    private String province;
    private String diseasesName;//病种名称
    private String remarks;

    private String realName;

    private String avatar;
    private String hospitalId;//医院id

    @JSONField(deserialize = false)
    private Boolean isSubscribe;

    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer deptId;

    @JSONField(deserialize = false)
    @Queryable(queryLogical = QueryLogical.QUANTUM)
    private LocalDateTime createTime;

    @JSONField(deserialize = false)
    private LocalDateTime updateTime;

    //锁定标记
    private String lockFlag;

    //删除标记
    @TableLogic
    @JSONField(deserialize = false)
    private String delFlag;

    /**
     * 权限集合
     */
    @TableField(exist = false)
    private Menu[] permissions;

    /**
     * 角色集合
     */
    @TableField(exist = false)
    private Role[] roles;

    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private HospitalInfo hospitalInfo;
    @TableField(exist = false)
    private String hospitalName;
    @TableField(exist = false)
    private String doctorTeamName;//所属团队名称
    @TableField(exist = false)
    private List<FollowUpPlanNotice> followUpPlanNoticeList;//患者随访计划
    @TableField(exist = false)
    private ElectronicCase electronicCase;//电子病例
}
