package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class PlanUserExcel {
    @ColumnWidth(0)
    @ExcelProperty(value = "姓名", index = 0)
    private String name;

    @ColumnWidth(10)
    @ExcelProperty(value = "手术名称", index = 1)
    private String diagnosis;
    @ColumnWidth(10)
    @ExcelProperty(value = "体重", index = 2)
    private String weight;
    @ColumnWidth(10)
    @ExcelProperty(value = "手术时间", index = 3)
    private String date;
    @ColumnWidth(10)
    @ExcelProperty(value = "是否测试账号", index = 4)
    private String isTestAccount;
    @ColumnWidth(10)
    @ExcelProperty(value = "年龄", index = 5)
    private String age;
    @ColumnWidth(10)
    @ExcelProperty(value = "身份证", index = 6)
    private String idCard;
    @ColumnWidth(10)
    @ExcelProperty(value = "医院名称", index = 7)
    private String hospitalName;
    @ColumnWidth(10)
    @ExcelProperty(value = "联系方式", index = 8)
    private String telePhone;
    @ColumnWidth(10)
    @ExcelProperty(value = "地址", index = 9)
    private String address;

}
