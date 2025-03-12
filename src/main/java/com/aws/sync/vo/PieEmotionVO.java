package com.aws.sync.vo;

import lombok.Data;

@Data
public class PieEmotionVO {
    private String emotion;
    private Double emotion_time;
    private Double emotion_time_rate;
}
