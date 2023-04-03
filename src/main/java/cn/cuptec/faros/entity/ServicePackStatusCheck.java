package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 服务包审核
 */
@Data
public class ServicePackStatusCheck extends Model<ServicePackStatusCheck> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer servicePackId;
    private Integer status;//0-启用 停-启用 2-待审核
}
