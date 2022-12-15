package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

//业务员收款、支付通道，暂时只做成上传个人收款码
@Data
public class SalesmanPayChannel {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //业务员用户id
    private Integer salesmanId;

    //收款码url
    private String revMoneyPicUrl;

    //支付类型 1个人收款码 2 服务号支付
    private Integer payType = 1;
}
