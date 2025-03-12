package com.aws.sync.dto;

import lombok.Data;

@Data
public class SearchDTO {
    private String meetingName;
    private String meetingType;
    private Long start;
    private Long end;
}
