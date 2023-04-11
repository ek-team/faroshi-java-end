package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
@HeadRowHeight(40)
public class EvaluationRecordsExcelBo extends BaseRowModel {

    @ColumnWidth(0)
    @ExcelProperty(value = "评估结果", index = 0)
    private int evaluateResult;
    @ColumnWidth(10)
    @ExcelProperty(value = "创建时间", index = 1)
    private long createDate;
    @ColumnWidth(10)
    @ExcelProperty(value = "修改时间", index = 2)
    private long updateDate;
    @ColumnWidth(10)
    @ExcelProperty(value = "耐受等级", index = 3)
    private int vas;//耐受等级
    @ColumnWidth(10)
    @ExcelProperty(value = "第一个值", index = 4)
    private float firstValue;
    @ColumnWidth(10)
    @ExcelProperty(value = "第四值", index = 5)
    private float fourthValue;
    @ColumnWidth(10)
    @ExcelProperty(value = "第五值", index = 6)
    private float fifthValue;
    @ColumnWidth(10)
    @ExcelProperty(value = "第二值", index = 7)
    private float secondValue;
    @ColumnWidth(10)
    @ExcelProperty(value = "第三值", index = 8)
    private float thirdValue;
}
