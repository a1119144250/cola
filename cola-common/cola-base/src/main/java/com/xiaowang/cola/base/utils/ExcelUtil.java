package com.xiaowang.cola.base.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    /**
     * 用EasyExcel读取Excel的第一行（表头）
     * @param file MultipartFile对象
     * @return 表头数组
     */
    public static String[] readExcelHeaders(MultipartFile file) {
        List<String> headers = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            // 只读取第一行
            EasyExcel.read(is, new PageReadListener<List<String>>(dataList -> {
                if (!dataList.isEmpty()) {
                    List<String> firstRow = dataList.get(0);
                    headers.addAll(firstRow);
                }
            }))
            .headRowNumber(0) // 不指定模型，直接读取原始数据
            .sheet()
            .doRead();
        } catch (Exception e) {
            throw new RuntimeException("读取Excel表头失败", e);
        }
        return headers.toArray(new String[0]);
    }
}