package com.aws.sync.dto;

import lombok.Data;

@Data
public class VideoDTO {
    private Long teamId;
    private String meetingUrl;
    private String meetingType;
    private String meetingName;
    private Long videoCreationTime;
}
