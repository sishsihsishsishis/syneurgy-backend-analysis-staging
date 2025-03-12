package com.aws.sync.vo;

import lombok.Data;

@Data
public class GptResultVO {
    private Long meetingId;
    private Long teamId;
    private String result;
}
