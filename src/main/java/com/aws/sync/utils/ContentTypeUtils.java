package com.aws.sync.utils;

import java.util.HashMap;
import java.util.Map;

public class ContentTypeUtils {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        // 更多类型可以在这里添加
    }

    public static String getContentType(String fileExtension) {
        return MIME_TYPES.getOrDefault(fileExtension.toLowerCase(), "application/octet-stream"); // 默认为二进制流
    }
}
