package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 病种
 */
@Data
public class Diseases extends Model<Diseases> {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer status=0; //0-待审核 1-审核通过 2-审核不通过
    private Integer createUserId;
    private LocalDateTime createTime;
    private String name;
    private Integer deptId;
}
