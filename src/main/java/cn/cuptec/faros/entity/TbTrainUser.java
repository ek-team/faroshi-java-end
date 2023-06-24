package cn.cuptec.faros.entity;


import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by zhanglun on 2020/6/3
 * Describe:
 */
@Data
public class TbTrainUser {
    private static final long serialVersionUID = 1L;

    private String doctorTeam;
    private Integer doctorTeamId;
    private Integer planCheckStatus=1;//1待审核 2审核通过
    private String birthday;
    private Integer cardType; //1-身份证 2-其他
    private String treatmentMethodId;//治疗方法Id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String height = "0";//身高
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String educationLevel;//文化程度
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Date onsetTime;//发病时间
    private String onsetDiagnosis;//发病诊断
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String diseaseDiagnosis;//疾病诊断
    private String userId;                 //用户唯一ID
    private Long keyId;                 //唯一ID
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String name;            //姓名
    private String registerProductSn;//注册设备序列号
    private String useProductSn;//当前使用设备序列号
    private int trainRecordTag; //训练记录标识 0-未设置 1-已设置
    private int evaluationRecordTag;//评估记录标识 0-未设置 1-已设置
    private int recoveryPlanCleanTag;//康复计划清除标识 0-未设置 1-已设置
    private Integer category = 1; //1-下肢 2-气动
    private String caseHistoryNo;   //病历号
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer age;                //年龄
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Date date;            //手术时间
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer sex;                //性别  男-1  女-0
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String diagnosis;       //诊断结果
    private String photo;
    private String doctor;           //医生
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String hospitalName;           //医院名称
    private String hospitalId;           //医院名称
    private String hospitalAddress;           //医院地址
    private Integer userType=0;   //0骨科用户，1康复科用户
    private String department;//科室信息
    private String macAdd; //mac地址
    private String address;         //联系地址
    private String telePhone;       //联系方式
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String idCard;       //身份证
    private String linkMan;         //联系人
    private String pingYin;
    private Date createDate;      // 创建时间
    private Date updateDate;      // 更新时间
    private String remark;      // 备注
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private String weight;      // 体重
    private String evaluateWeight;      // 评估负重
    private String account;      // 用户名
    private String password;      // 密码
    private String str;      // 保留
    private LocalDateTime firstTrainTime;//第一次上传训练记录时间
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer xtUserId; //系统用户id
    private Integer endService; //是否结束服务 1否 2是
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer isTestAccount = 0;//是否是测试账号 0不是 1是
    @TableField(exist = false)
    private Integer unreadNum;
    @TableField(exist = false)
    private Date useTime;
    @TableField(exist = false)
    private Integer accountStatus;//身份证存在 判断是使用新账号还是旧账号 0新账号 1旧账号
}
