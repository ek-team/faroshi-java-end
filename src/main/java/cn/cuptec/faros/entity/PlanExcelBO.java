package cn.cuptec.faros.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;


@Data
@HeadRowHeight(40)
public class PlanExcelBO extends BaseRowModel {

    @ColumnWidth(0)
    @ExcelProperty(value = "数据id", index = 0)
    private Integer id;
    @ColumnWidth(10)
    @ExcelProperty(value = "用户昵称", index = 1)
    private String name;//用户唯一id 静态方法获取唯一id编号
    @ColumnWidth(10)
    @ExcelProperty(value = "成功次数", index = 2)
    private Integer successTime;//成功次数
    @ColumnWidth(10)
    @ExcelProperty(value = "警告次数", index = 3)
    private Integer warningTime;//警告次数
    @ColumnWidth(10)
    @ExcelProperty(value = "训练时间", index = 4)
    private Integer trainTime;//训练时间
    @ColumnWidth(10)
    @ExcelProperty(value = "得分", index =5)
    private Integer score;//得分
    @ColumnWidth(10)
    @ExcelProperty(value = "疼痛等级", index = 6)
    private Integer painLevel;//疼痛等级
    @ColumnWidth(10)
    @ExcelProperty(value = "不良反应", index = 7)
    private String adverseReactions;//不良反应
    @ColumnWidth(10)
    @ExcelProperty(value = "目标负重", index = 8)
    private Integer targetLoad;//目标负重
    @ColumnWidth(10)
    @ExcelProperty(value = "每天次数", index = 9)
    private Integer frequency;//每天次数
    @ColumnWidth(10)
    @ExcelProperty(value = "患病类型", index = 10)
    private String diagnostic;//患病类型
    @ColumnWidth(10)
    @ExcelProperty(value = "时间", index = 11)
    private String dateStr;
    @ColumnWidth(10)
    @ExcelProperty(value = "总次数", index = 12)
    private Integer size;
    //详情信息
    @ColumnWidth(10)
    @ExcelProperty(value = "当天第几次", index = 13)
    private Integer frequencyDetail;//当天第几次
    @ColumnWidth(10)
    @ExcelProperty(value = "目标负重", index = 14)
    private Integer targetLoadDetail;//目标负重
    @ColumnWidth(10)
    @ExcelProperty(value = "实际负重", index = 15)
    private Integer realLoad;//实际负重

}
