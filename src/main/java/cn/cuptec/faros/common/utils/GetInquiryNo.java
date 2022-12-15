package cn.cuptec.faros.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @Description:  生成问诊单编码
 * @Author mby
 * @Date 2020/8/17 18:15
 */
public class GetInquiryNo  {
    public static String getOrderIdByUUId() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = format.format(date);
       // int hashCodeV = UUID.randomUUID().toString().hashCode();
        long hashCodeV=Math.round((Math.random()+1) * 1000);

        return time + String.format("%04d", hashCodeV);
    }

    /*public static void main(String[] args) {
        String orderIdByUUId = GetInquiryNo.getOrderIdByUUId();
        System.out.println(orderIdByUUId);
    }*/
}
