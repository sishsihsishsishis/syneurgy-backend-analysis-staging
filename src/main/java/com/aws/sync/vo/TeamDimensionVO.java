package com.aws.sync.vo;

import lombok.Data;

import java.util.List;

@Data
public class TeamDimensionVO {
    private Long meeting_id;
    private Long create_time;
    private String meeting_name;
    private List<RadarVO> dimension;
}
