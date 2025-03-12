package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BehaviourEngineVO {
    private HashMap<String, List<List<Double>>> info;
    private Long meetingId;
    private String type;
    private String subType;
}
