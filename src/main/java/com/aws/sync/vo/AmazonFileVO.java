package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 云存储文件模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonFileVO {
    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String url;

    /**
     * 云存储中的路径
     */
    private String filePath;

    /**
     *  文件类型
     */
    private String fileType;
}

