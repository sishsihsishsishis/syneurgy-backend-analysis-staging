package com.aws.sync.vo;

import lombok.Data;

@Data
public class IndividualVO {
    private String users;
    private Double time_ms;
    private Double individual_sync;
    private Double individual_distance;
    private Double individual_rate;
}
