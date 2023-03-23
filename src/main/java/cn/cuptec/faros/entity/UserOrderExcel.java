package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class UserOrderExcel {
    @ColumnWidth(0)
    @ExcelProperty(value = "订单号", index = 0)
    private String orderNo;
    @ColumnWidth(10)
    @ExcelProperty(value = "状态", index = 1)
    private String status;
    @ColumnWidth(10)
    @ExcelProperty(value = "价格", index = 2)
    private String payment;//
    @ColumnWidth(10)
    @ExcelProperty(value = "订单创建时间", index = 3)
    private String createTime;//
    @ColumnWidth(10)
    @ExcelProperty(value = "服务包名称", index = 4)
    private String servicePackName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "买家", index = 5)
    private String userName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "支付时间", index = 6)
    private String payTime;//
    @ColumnWidth(10)
    @ExcelProperty(value = "就诊人", index = 7)
    private String patientUserName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "期望发货时间", index = 8)
    private String deliveryDate;//
    @ColumnWidth(10)
    @ExcelProperty(value = "医院", index = 9)
    private String hospitalName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "团队", index = 10)
    private String doctorTeamName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "身份证号", index = 11)
    private String patientUserIdCard;//
    @ColumnWidth(10)
    @ExcelProperty(value = "收货人姓名", index = 12)
    private String receiverName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "收货人地址", index = 13)
    private String receiverDetailAddress;//
    @ColumnWidth(10)
    @ExcelProperty(value = "规格", index = 14)
    private String spec;//
    @ColumnWidth(10)
    @ExcelProperty(value = "收货人手机号", index = 15)
    private String phone;//
}
