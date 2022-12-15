package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自己的服务信息
 */
@Data
public class UserServicePackageInfo extends Model<UserServicePackageInfo> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer servicePackageInfoId;//服务id
    @TableField(exist = false)
    private ServicePackageInfo servicePackageInfo;
    private Integer useCount = 0;//已使用次数
    private LocalDateTime createTime;
    private Integer orderId;
    private Integer userId;
}
