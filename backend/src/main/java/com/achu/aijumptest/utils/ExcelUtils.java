package com.achu.aijumptest.utils;

import com.achu.aijumptest.exception.BusinessException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.utils.ExcelUtils
 *
 * @author: achu_code
 * description: EasyExcel 导入导出封装工具类。
 */
@Component
public class ExcelUtils {

    /**
     * 通用 Excel 导入。
     *
     * @param file     前端上传的 Excel 文件
     * @param clazz    Excel 映射实体类字节码，例如 QuestionImportExcel.class
     * @param listener EasyExcel 监听器，用于处理每一行数据
     * @param <T>      Excel 映射实体类型
     */
    public static <T> void readExcel(MultipartFile file, Class<T> clazz, ReadListener<T> listener) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Excel 文件不能为空，请重新上传！");
        }

        try {
            EasyExcel.read(file.getInputStream(), clazz, listener)
                    // 默认读取第一个 sheet
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            throw new BusinessException("Excel 文件读取失败，请检查文件格式是否正确！");
        }
    }

    /**
     * 通用 Excel 导出。
     * 默认 sheet 名与文件名一致。
     *
     * @param response HTTP 响应对象
     * @param list     要导出的数据集合
     * @param fileName 下载文件名，不需要带 .xlsx 后缀
     * @param clazz    Excel 映射实体类字节码
     * @param <T>      Excel 映射实体类型
     */
    public static <T> void writeExcel(HttpServletResponse response, List<T> list, String fileName, Class<T> clazz) {
        writeExcel(response, list, fileName, fileName, clazz);
    }

    /**
     * 通用 Excel 导出。
     *
     * @param response  HTTP 响应对象
     * @param list      要导出的数据集合
     * @param fileName  下载文件名，不需要带 .xlsx 后缀
     * @param sheetName Excel 工作表名称
     * @param clazz     Excel 映射实体类字节码
     * @param <T>       Excel 映射实体类型
     */
    public static <T> void writeExcel(HttpServletResponse response,
                                      List<T> list,
                                      String fileName,
                                      String sheetName,
                                      Class<T> clazz) {
        try {
            // 1. 设置 Excel 文件响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            // 2. 处理中文文件名，避免浏览器下载后乱码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader(
                    "Content-disposition",
                    "attachment;filename=" + encodedFileName + ".xlsx"
            );

            // 3. 写出 Excel 到浏览器响应流
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .doWrite(list);
        } catch (IOException e) {
            throw new BusinessException("Excel 导出失败，系统响应异常！");
        }
    }
}
