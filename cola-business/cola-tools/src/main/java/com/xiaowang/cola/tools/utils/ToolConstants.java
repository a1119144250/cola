package com.xiaowang.cola.tools.utils;

/**
 * 工具常量
 *
 * @author cola
 */
public class ToolConstants {

    /**
     * 工具状态
     */
    public static class Status {
        /**
         * 禁用
         */
        public static final int DISABLED = 0;
        
        /**
         * 启用
         */
        public static final int ENABLED = 1;
    }

    /**
     * 工具类型
     */
    public static class Type {
        /**
         * 系统工具
         */
        public static final String SYSTEM = "SYSTEM";
        
        /**
         * 开发工具
         */
        public static final String DEVELOPMENT = "DEVELOPMENT";
        
        /**
         * 业务工具
         */
        public static final String BUSINESS = "BUSINESS";
        
        /**
         * 数据工具
         */
        public static final String DATA = "DATA";
    }

    /**
     * JSON 格式化类型
     */
    public static class JsonFormatType {
        /**
         * 美化格式（带缩进和换行）
         */
        public static final String PRETTY = "PRETTY";
        
        /**
         * 紧凑格式（保留基本结构）
         */
        public static final String COMPACT = "COMPACT";
        
        /**
         * 压缩格式（最小化体积）
         */
        public static final String MINIFY = "MINIFY";
    }
}
