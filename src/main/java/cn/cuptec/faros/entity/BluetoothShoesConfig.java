package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bluetooth_shoes_config")
public class BluetoothShoesConfig {
    @TableId(type = IdType.AUTO)
    private int id;
    private int size;
}
