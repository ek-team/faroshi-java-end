package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 代理商快递设置
 */
@Data
public class DeliverySetting {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer status=0;//0关闭 1开启
    private String phone;
    private String name;
    private String address;
    private Integer deptId;
}
