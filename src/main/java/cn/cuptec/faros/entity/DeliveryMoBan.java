package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class DeliveryMoBan extends BaseRowModel {

    @ColumnWidth(0)
    @ExcelProperty(value = "id", index = 0)
    private String id;

    @ColumnWidth(10)
    @ExcelProperty(value = "订单号", index = 1)
    private String orderNo;
    @ColumnWidth(10)
    @ExcelProperty(value = "快递公司", index = 2)
    private String name;
    @ColumnWidth(10)
    @ExcelProperty(value = "快递单号", index = 3)
    private String deliverySn;//
}
