package com.aws.sync.vo;

import lombok.Data;

@Data
public class NlpVO {
    private String speaker;
    private Double starts;
    private Double ends;
    private String sentence;
    private String emotion;
    private String dialogue;

}
