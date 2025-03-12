package com.aws.sync.vo.detection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectionVO {
    private Long meetingId;
    private String users;
    private double starts;
    private double ends;
    private String keyword;
    private String frequencyMap;
    private String unknown;
    private String substances;
    private String similarities;
}
