package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

@Data
public class Express {

    //快递编码
    @TableId(value = "express_code", type = IdType.INPUT)
    private String expressCode;

    //快递名称
    private String expressName;

}
