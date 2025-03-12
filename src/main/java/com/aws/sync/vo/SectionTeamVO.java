package com.aws.sync.vo;

import lombok.Data;

@Data
public class SectionTeamVO {
    private Double starts;
    private Double ends;
    private Integer label;
    private Double attention;
    private Double sentiment ;
    private Double participation;
    private Double hrv;
    private Double stress;
    private String insight;
    private String highlight_statement;
}
