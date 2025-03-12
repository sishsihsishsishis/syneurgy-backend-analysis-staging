package com.aws.sync.vo;


import lombok.Data;

@Data
public class WordRateVO {
    private Long meetingId;
    private String name;
    private Double rate;
}
