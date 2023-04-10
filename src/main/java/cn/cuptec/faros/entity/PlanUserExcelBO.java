package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import java.util.Date;

@Data
@HeadRowHeight(40)
public class PlanUserExcelBO extends BaseRowModel {

    @ColumnWidth(0)
    @ExcelProperty(value = "数据id", index = 0)
    private Integer id;

    @ColumnWidth(10)
    @ExcelProperty(value = "用户昵称", index = 1)
    private String name;


    @ColumnWidth(10)
    @ExcelProperty(value = "身份证号", index = 2)
    private String idCard;
    @ColumnWidth(10)
    @ExcelProperty(value = "身高", index = 3)
    private String height="0";//身高
    @ColumnWidth(10)
    @ExcelProperty(value = "文化程度", index = 4)
    private String educationLevel;//文化程度
    @ColumnWidth(10)
    @ExcelProperty(value = "发病时间", index = 5)
    private Date onsetTime;//发病时间
    @ColumnWidth(10)
    @ExcelProperty(value = "发病诊断", index = 6)
    private String onsetDiagnosis;//发病诊断
    @ColumnWidth(10)
    @ExcelProperty(value = "疾病诊断", index = 7)
    private String diseaseDiagnosis;//疾病诊断
}
