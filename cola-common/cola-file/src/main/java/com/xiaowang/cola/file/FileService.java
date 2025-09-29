package com.xiaowang.cola.file;

import java.io.InputStream;

/**
 * 文件 服务
 *
 * @author cola
 */
public interface FileService {

    /**
     * 文件上传
     * @param path
     * @param fileStream
     * @return
     */
    public boolean upload(String path, InputStream fileStream);

}
