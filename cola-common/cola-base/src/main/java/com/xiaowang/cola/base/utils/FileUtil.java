package com.xiaowang.cola.base.utils;

import com.xiaowang.cola.base.constant.Constant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;

public class FileUtil {

    public static boolean isExcel(File file) {
        return file.getName().endsWith(Constant.XLS_EXCEL_FILE_TYPE)
                || file.getName().endsWith(Constant.XLSX_EXCEL_FILE_TYPE);
    }

    public static boolean notExcel(MultipartFile file){
        return !Objects.requireNonNull(file.getOriginalFilename()).endsWith(Constant.XLS_EXCEL_FILE_TYPE)
                && !file.getOriginalFilename().endsWith(Constant.XLSX_EXCEL_FILE_TYPE);
    }


    public static File newFileToTempDir(String fileName) {
        File tmpDirectory = FileUtils.getTempDirectory();
        return FileUtils.getFile(tmpDirectory, fileName);
    }

    /**
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        return FileUtils.deleteQuietly(file);
    }

    private final static String EXCEL_FILE_EXTENSION = "[xls][xlsx]";
    public final static String EXTENSION_SEPARATOR = ".";

    public static boolean excelTypeError(String path) {
        if (StringUtils.isBlank(path)) {
            return true;
        }
        String lowercasePath = path.toLowerCase();
        return !EXCEL_FILE_EXTENSION.contains("[" + lowercasePath.substring(lowercasePath.lastIndexOf(EXTENSION_SEPARATOR) + 1) + "]");
    }
}