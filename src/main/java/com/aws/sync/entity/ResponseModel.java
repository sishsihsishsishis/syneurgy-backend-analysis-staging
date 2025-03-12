package com.aws.sync.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseModel {
    private String id;
    private String object;
    private Long created;
    private String model;
    private Map<String, Integer> usage;
    private List<Map<String, Object>> choices;

    // getters and setters
}
