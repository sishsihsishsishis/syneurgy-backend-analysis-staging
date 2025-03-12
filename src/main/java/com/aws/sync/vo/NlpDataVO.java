package com.aws.sync.vo;

import lombok.Data;

import javax.persistence.Column;

@Data
public class NlpDataVO {
    private String speaker;

    private Double starts;

    private Double ends;

    private String sentence;

    private String emotion;
    private String dialogue;

}
