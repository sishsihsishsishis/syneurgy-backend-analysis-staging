package com.aws.sync.dto;

import lombok.Data;

@Data
public class MeetingSearchDTO {
    private long currentPage;
    private long pageCount;
    private String meetingName;
    private String meetingType;
    private Long start;
    private Long end;
    private Integer progress;
    private Integer sortType;
}
