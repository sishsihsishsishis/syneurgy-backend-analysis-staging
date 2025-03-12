package com.aws.sync.dto.video;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CompleteUploadDTO {
    private MultipartFile thumbnail;
    private String uploadId;
    private String partETagsJson;
    private Long lastModified;
}
