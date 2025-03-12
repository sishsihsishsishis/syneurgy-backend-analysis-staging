package com.aws.sync.vo;

import lombok.Data;

@Data
public class TeamPerformanceVO {
    private Long meeting_id;
    private Long create_time;
    private Double brain_score;
    private Double body_score;
    private Double behavior_score;
    private String meeting_name;

}
