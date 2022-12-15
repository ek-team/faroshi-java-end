package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TbUserTrainRecordBackup {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer oldId;
    private String userId;//用户唯一id 静态方法获取唯一id编号
    private Long keyId;                 //唯一ID
    private Integer successTime;//成功次数
    private Integer warningTime;//警告次数
    private Integer trainTime;//训练时间
    private Integer score;//得分
    private Integer painLevel;//疼痛等级
    private String adverseReactions;//不良反应
    private Integer targetLoad;//目标负重


    private Long createDate;
    private Integer frequency;//每天次数
    private String diagnostic;//患病类型
    private String str;//保留
    private Long planId;
    private Integer classId;
    private Integer isUpload;                //上传状态  0-未上传  1-上传到局域网 2-上传到云端
    private String dateStr;
    private Integer backVersion = 1; //备份版本号
}
