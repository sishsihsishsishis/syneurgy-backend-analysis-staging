package com.aws.sync.vo;

import lombok.Data;

@Data
public class IndividualAllVO {
    private String users;
    private Double time_ms;
    private Double individual_sync;
    private Double individual_distance;
    private Double distance;
    private Integer label;
}
