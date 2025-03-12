package com.aws.sync.vo;

import lombok.Data;

@Data
public class BarSpeakerVO {
    private String speaker;
    private Double negative;
    private Double neutral;
    private Double positive;
}
