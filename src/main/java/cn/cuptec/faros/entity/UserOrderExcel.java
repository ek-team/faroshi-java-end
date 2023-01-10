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
}
