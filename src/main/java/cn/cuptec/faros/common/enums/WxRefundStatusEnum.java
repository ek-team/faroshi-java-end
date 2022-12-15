package cn.cuptec.faros.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WxRefundStatusEnum {
    PENDING(1,"PENDING","待处理"),
    SUCCESS(2,"SUCCESS","退款成功"),
    CHANGE(3,"CHANGE","退款异常"),
    REFUNDCLOSE(4,"REFUNDCLOSE","退款关闭");



    private Integer code ;
    private String desc;
    private String str;



    public static WxRefundStatusEnum getEnumByDesc(String desc){

        for (WxRefundStatusEnum e: WxRefundStatusEnum.values()  ) {
            if(e.getDesc().equals(desc)) return e;
        }
        return null;
    }
}
