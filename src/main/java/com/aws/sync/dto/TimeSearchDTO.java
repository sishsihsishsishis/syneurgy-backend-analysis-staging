package com.aws.sync.dto;

import lombok.Data;

@Data
public class TimeSearchDTO {
    private Long teamID;
    private String userID;
    private Long start;
    private Long end;

}
