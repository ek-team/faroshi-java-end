package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_stock_relation_qr_code")
public class ProductStockRelationQrCode {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productStockId;
    private String liveQrCodeId;
}
