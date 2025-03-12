package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BehaviourEngineCvVO {
    private Long meeting_id;
    //private Long team_id;
    private String users;
    private double time;
    private String keyword;
}
