package com.aws.sync.dto.video;

import lombok.Data;

@Data
public class VideoUploadDTO {
    private Long meeting_id;
    private Integer is_handle;
    private String video_url;
    private String img_url;
    private Integer is_update;
    private String thumbnail;
    private Long upload_time;
    private String meeting_name;
    private Long meeting_start_time;
    private String meeting_type;
    private Long video_create_time;
}
