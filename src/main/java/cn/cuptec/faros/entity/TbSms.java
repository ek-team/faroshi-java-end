package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class TbSms {

    @Queryable(queryLogical = QueryLogical.EQUAL)
    @TableId(type = IdType.AUTO)
    private String id;

    private String userPhone;

    private String userName;

    private String smsText;

    private int saleId;


    private String saleName;

    private String productName;
    private int productId;



    private Date createDate;


}
