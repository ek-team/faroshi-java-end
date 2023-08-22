package cn.cuptec.faros.entity;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * 训练记录
 */
@Data
public class TbUserTrainRecord implements Comparable<TbUserTrainRecord> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userId;//用户唯一id 静态方法获取唯一id编号
    private Long keyId;                 //唯一ID
    private Integer successTime;//成功次数
    private Integer warningTime;//警告次数
    private String macAddress;
    private Integer totalTrainStep;
    private String bleMacAddress;
    private String source;
    private String bleName;
    private Integer trainTime;//训练时间
    private Integer score;//得分
    private Integer painLevel;//疼痛等级
    private String adverseReactions;//不良反应
    private int targetLoad;//目标负重
    private String productSn;
    private Long createDate;
    private Integer frequency;//每天次数
    private String diagnostic;//患病类型
    private String str;//保留
    private Long planId;
    private Integer classId;
    private Integer isUpload;                //上传状态  0-未上传  1-上传到局域网 2-上传到云端
    private String dateStr;
    private LocalDateTime updateTime;//上传时间
    @TableField(exist = false)
    private List<TbTrainData> trainDataList;

    @TableField(exist = false)
    private Integer count;
    @TableField(exist = false)
    private String userName;//设备用户名字

    @Override
    public int compareTo(TbUserTrainRecord o) {
        if (o.getUpdateTime() != null && this.updateTime!=null) {
            return o.getUpdateTime().compareTo(this.updateTime);//根据时间降序
        } else {
            return o.getDateStr().compareTo(this.dateStr);//根据时间降序
        }

    }

}

