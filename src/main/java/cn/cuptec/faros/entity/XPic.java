package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDate;

@Data
public class XPic extends Model<XPic> {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String url;
    private LocalDate createTime;
    private String idCard;
}
