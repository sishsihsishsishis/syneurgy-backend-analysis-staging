package com.aws.sync.vo;

import lombok.Data;

import java.util.Map;

@Data
public class MeetingScoreVO {
    private Long meeting_id;
    private Long team_id;
    private Long upload_time;
    private String meeting_name;
    private Long duration;
    private Integer is_handle;
    private String video_url;
    private String thumbnail;
    private Double body_score;
    private Double brain_score;
    private Double behavior_score;
    private Double total_score;
    private Integer cv_handle;
    private Integer nlp_handle;
    private Long video_create_time;
    private String meeting_type;
    private MeetingSummaryVO status;
    private Map<String, Double> dimension;
}
