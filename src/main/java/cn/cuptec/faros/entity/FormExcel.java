package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class FormExcel {
    @ColumnWidth(0)
    @ExcelProperty(value = "表单名称", index = 0)
    private String formName;
    @ColumnWidth(10)
    @ExcelProperty(value = "用户姓名", index = 1)
    private String userName;
    @ColumnWidth(10)
    @ExcelProperty(value = "分数", index = 2)
    private String scope;
    @ColumnWidth(10)
    @ExcelProperty(value = "医生姓名", index = 3)
    private String doctorName;
}
