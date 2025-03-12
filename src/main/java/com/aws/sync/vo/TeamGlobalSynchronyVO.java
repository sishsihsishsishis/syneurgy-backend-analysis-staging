package com.aws.sync.vo;

import lombok.Data;

@Data
public class TeamGlobalSynchronyVO {
    private Long meeting_id;
    private Long create_time;
    private Double total_score;
    private String meeting_name;
}
