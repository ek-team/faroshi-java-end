package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class ImportDoctor extends BaseRowModel{
    @ColumnWidth(0)
    @ExcelProperty(value = "省", index = 0)
    private String province;

    @ColumnWidth(10)
    @ExcelProperty(value = "市", index = 1)
    private String city;
    @ColumnWidth(10)
    @ExcelProperty(value = "县（区）", index = 2)
    private String area;
    @ColumnWidth(10)
    @ExcelProperty(value = "医院", index = 3)
    private String hospital;//
    @ColumnWidth(10)
    @ExcelProperty(value = "科室", index = 4)
    private String department;//
    @ColumnWidth(10)
    @ExcelProperty(value = "级别", index = 5)
    private String level;//
    @ColumnWidth(10)
    @ExcelProperty(value = "服务包名", index = 6)
    private String servicePackName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "成员姓名", index = 7)
    private String userName;//
    @ColumnWidth(10)
    @ExcelProperty(value = "角色", index = 8)
    private String role;//
    @ColumnWidth(10)
    @ExcelProperty(value = "手机号", index = 9)
    private String mobile;//
}
