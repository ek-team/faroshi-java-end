//package cn.cuptec.faros.entity;
//
//import cn.cuptec.faros.config.mybatis.JsonTypeHandler;
//import cn.hutool.json.JSONObject;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.extension.activerecord.Model;
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import org.apache.ibatis.type.JdbcType;
//
//import javax.validation.constraints.NotNull;
//import java.time.LocalDateTime;
//
//@Data
//@TableName("pay_apply_form")
//@EqualsAndHashCode(callSuper = true)
//@ApiModel(description = "支付进件申请单")
//public class PayApplyForm extends Model<PayApplyForm> {
//    private static final long serialVersionUID=1L;
//
//    /**
//     * PK
//     */
//    @NotNull(message = "PK不能为空")
//    @ApiModelProperty(value = "PK")
//    private String id;
//
//    /**
//     * 店铺Id
//     */
//    @NotNull(message = "店铺Id不能为空")
//    @ApiModelProperty(value = "店铺Id")
//    private String depId;
//    /**
//     * 排序
//     */
//    @ApiModelProperty(value = "排序")
//    private Integer sort;
//    /**
//     * 创建时间
//     */
//    @ApiModelProperty(value = "创建时间")
//    private LocalDateTime createTime;
//    /**
//     * 最后更新时间
//     */
//    @ApiModelProperty(value = "最后更新时间")
//    private LocalDateTime updateTime;
//    /**
//     * 逻辑删除标记（0：显示；1：隐藏）
//     */
//    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
//    private String delFlag;
//    /**
//     * 超级管理员信息
//     */
//    @NotNull(message = "超级管理员信息不能为空")
//    @ApiModelProperty(value = "超级管理员信息")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject contactInfo;
//    /**
//     * 主体资料
//     */
//    @NotNull(message = "主体资料不能为空")
//    @ApiModelProperty(value = "主体资料")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject subjectInfo;
//    /**
//     * 经营资料
//     */
//    @NotNull(message = "经营资料不能为空")
//    @ApiModelProperty(value = "经营资料")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject businessInfo;
//    /**
//     * 结算规则
//     */
//    @NotNull(message = "结算规则不能为空")
//    @ApiModelProperty(value = "结算规则")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject settlementInfo;
//    /**
//     * 结算银行账户
//     */
//    @NotNull(message = "结算银行账户不能为空")
//    @ApiModelProperty(value = "结算银行账户")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject bankAccountInfo;
//    /**
//     * 补充材料
//     */
//    @NotNull(message = "补充材料不能为空")
//    @ApiModelProperty(value = "补充材料")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject additionInfo;
//    /**
//     * 微信支付申请单号
//     */
//    @ApiModelProperty(value = "微信支付申请单号")
//    private String applymentId;
//    /**
//     * 特约商户号
//     */
//    @ApiModelProperty(value = "特约商户号")
//    private String subMchid;
//    /**
//     * 超级管理员签约链接
//     */
//    @ApiModelProperty(value = "超级管理员签约链接")
//    private String signUrl;
//    /**
//     * 申请单状态
//     */
//    @ApiModelProperty(value = "申请单状态")
//    private String applymentState;
//    /**
//     * 申请状态描述
//     */
//    @ApiModelProperty(value = "申请状态描述")
//    private String applymentStateMsg;
//    /**
//     * 驳回原因详情
//     */
//    @ApiModelProperty(value = "驳回原因详情")
//    @TableField(typeHandler = JsonTypeHandler.class, jdbcType= JdbcType.VARCHAR)
//    private JSONObject auditDetail;
//
//}
