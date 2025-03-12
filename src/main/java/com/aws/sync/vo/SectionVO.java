package com.aws.sync.vo;

import lombok.Data;



@Data
public class SectionVO {
    private Double starts;
    private Double ends;
    private String user;
    private String description;

    private int label;
    private Double attention;
    private Double sentiment ;
    private Double participation;
    private Double hrv;
    private Double stress;
}
