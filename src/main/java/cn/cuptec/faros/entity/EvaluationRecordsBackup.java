package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
@Data
public class EvaluationRecordsBackup extends Model<EvaluationRecordsBackup> implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private long keyId;                 //唯一ID
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private long userId;//用户唯一id 静态方法获取唯一id编号
    private int evaluateResult;
    private long createDate;
    private long updateDate;
    private int vas;//耐受等级
    private float firstValue;
    private float fourthValue;
    private float fifthValue;
    private float secondValue;
    private float thirdValue;
    private int isUpload;                //上传状态  0-未上传  1-上传到局域网 2-上传到云端
    private Integer backVersion = 1; //备份版本号
}
