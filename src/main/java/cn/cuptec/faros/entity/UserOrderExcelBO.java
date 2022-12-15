package cn.cuptec.faros.entity;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class UserOrderExcelBO extends BaseRowModel {

    @ColumnWidth(0)
    @ExcelProperty(value = "数据id", index = 0)
    private String id;
    @ColumnWidth(10)
    @ExcelProperty(value = "纳里订单编号", index = 1)
    private String naLiOrderId;
    @ColumnWidth(10)
    @ExcelProperty(value = "业务员手机号", index = 2)
    private String salesmanPhone;
    @ColumnWidth(10)
    @ExcelProperty(value = "快递单号", index = 3)
    private String deliverySn;
    @ColumnWidth(10)
    @ExcelProperty(value = "医院", index = 4)
    private String hospitalName;
    @ColumnWidth(10)
    @ExcelProperty(value = "实际付款", index =5)
    private String payment;//得分
    @ColumnWidth(10)
    @ExcelProperty(value = "收货人", index = 6)
    private String receiverName;
    @ColumnWidth(10)
    @ExcelProperty(value = "收货电话", index = 7)
    private String receiverPhone;
    @ColumnWidth(10)
    @ExcelProperty(value = "收货地区", index = 8)
    private String receiverRegion;
    @ColumnWidth(10)
    @ExcelProperty(value = "订单状态", index = 9)
    private String status;
    @ColumnWidth(10)
    @ExcelProperty(value = "订单状态", index = 10)
    private String productName;
}
