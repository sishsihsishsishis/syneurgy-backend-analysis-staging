package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BehaviourEngineCountVO {
    private Long meetingId;
    private String speakers;
    private String type;
    private String subType;
    private Long count;
}
