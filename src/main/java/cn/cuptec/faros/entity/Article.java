package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private int id;
    private Integer createUserId;
    private LocalDateTime createTime;
    private Integer deptId;
    private String video;
    private String content;
    private String picture;
    private int sort;
    //1 视频 2图片 3文字
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private int type;
    private String title;
    //是否显示 1 显示 2不显示

    private int showContent;
    //1:医院版 2：家庭版
    private Integer category;
}
