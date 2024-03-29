package cn.cuptec.faros.util;

import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.entity.*;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Font;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.TableStyle;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.merge.LoopMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUtil {
    /**
     * 导出 Excel ：一个 sheet，带表头.
     *
     * @param response HttpServletResponse
     * @param data     数据 list，每个元素为一个 BaseRowModel
     * @param fileName 导出的文件名
     * @param model    映射实体类，Excel 模型
     * @throws Exception 异常
     */

    public static void writeExcel(HttpServletResponse response, List<? extends Object> data,List<? extends Object> data1,List<? extends Object> data2, String fileName, String sheetName, Class model) throws Exception {

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();

        //这里 需要指定写用哪个class去写
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "基本信息").head(PlanUserExcelBO.class).build();
        excelWriter.write(data, writeSheet);
        writeSheet = EasyExcel.writerSheet(1, "训练记录").head(PlanExcelBO.class).build();
        excelWriter.write(data1, writeSheet);
        writeSheet = EasyExcel.writerSheet(2, "评估记录").head(EvaluationRecordsExcelBo.class).build();
        excelWriter.write(data2, writeSheet);
        //千万别忘记finish 会帮忙关闭流
        excelWriter.finish();

    }

    public static void writeDeliveryMoBanExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class model) throws Exception {

        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 颜色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        // 字体
        headWriteCellStyle.setWriteFont(headWriteFont);
        headWriteCellStyle.setWrapped(true);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠中对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        List<LoopMergeStrategy> loopMergeStrategies = new ArrayList<>();


        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        ExcelWriterBuilder write = EasyExcel.write(getOutputStream(fileName, response), model);
        loopMergeStrategies.forEach(m -> {
            write.registerWriteHandler(m);
        });
        write.excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy)
                //最大长度自适应 目前没有对应算法优化 建议注释掉不用 会出bug
//                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(data);

    }

    public static void writeUserOrderExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class model) throws Exception {

        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 颜色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        // 字体
        headWriteCellStyle.setWriteFont(headWriteFont);
        headWriteCellStyle.setWrapped(true);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠中对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        List<LoopMergeStrategy> loopMergeStrategies = new ArrayList<>();

//        for (int i = 0; i < planExcelBOS.size(); i++) {
//            if (planExcelBOS.get(i).getSize() != null) {
//                List<LoopMergeStrategy> merge = merge(planExcelBOS.get(i).getSize()+1);
//                loopMergeStrategies.addAll(merge);
//
//            }
//        }

        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        ExcelWriterBuilder write = EasyExcel.write(getOutputStream(fileName, response), model);
        loopMergeStrategies.forEach(m -> {
            write.registerWriteHandler(m);
        });
        write.excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy)
                //最大长度自适应 目前没有对应算法优化 建议注释掉不用 会出bug
//                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(data);

    }

    private static List<LoopMergeStrategy> merge(int index) {
        List<LoopMergeStrategy> loopMergeStrategies = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            LoopMergeStrategy loopMergeStrategy = new LoopMergeStrategy(index, 1, i);

            loopMergeStrategies.add(loopMergeStrategy);
        }
        return loopMergeStrategies;
    }

    /**
     * 导出文件时为Writer生成OutputStream.
     *
     * @param fileName 文件名
     * @param response response
     * @return ""
     */
    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setCharacterEncoding("utf8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-store");
            response.addHeader("Cache-Control", "max-age=0");
            return response.getOutputStream();
        } catch (IOException e) {
            throw new Exception("导出excel表格失败!", e);
        }
    }

    public static void writefFormExcel(HttpServletResponse response, List<List<Object>> dataList, String fileName, String sheetName, List<List<String>> headList) throws Exception {

        EasyExcel.write(getOutputStream(fileName, response)).head(headList).sheet("题目数据").doWrite(dataList);
    }
}