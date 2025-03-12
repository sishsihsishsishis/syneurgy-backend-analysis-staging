package com.aws.sync.vo;

import lombok.Data;

@Data
public class SynchronyMomentVO {
    private Double starts;
    private Double ends;
    private Integer label;
    private String sentence;
    private String insight;
    private Double attention;
    private Double sentiment ;
    private Double participation;
    private Double hrv;
    private Double stress;
}
