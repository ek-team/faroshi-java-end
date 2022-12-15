package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 用户备份
 */
@Data
public class TbTrainUserBackup {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userId;                 //用户唯一ID
    private Long keyId;                 //唯一ID
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String name;            //姓名
    private String caseHistoryNo;   //病历号
    private Integer age;                //年龄
    private Date date;            //手术时间
    private Integer sex;                //性别  男-1  女-0
    private String diagnosis;       //诊断结果
    private String photo;
    private String doctor;           //医生
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String hospitalName;           //医院名称
    private String hospitalAddress;           //医院地址
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
    private String weight;      // 体重
    private String evaluateWeight;      // 评估负重
    private String account;      // 用户名
    private String password;      // 密码
    private String str;      // 保留
    private Integer xtUserId; //系统用户id
    private Integer endService; //是否结束服务 1否 2是
    private Integer backVersion = 1; //备份版本号


}
