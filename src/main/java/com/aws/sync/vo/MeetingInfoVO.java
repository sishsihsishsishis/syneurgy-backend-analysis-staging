package com.aws.sync.vo;

import lombok.Data;

@Data
public class MeetingInfoVO {
    private Long meeting_id;
    private Long team_id;
    private Long upload_time;
    private Long meeting_start_time;
    private String meeting_name;
    private Integer is_update;
    private Long duration;
    private Integer is_handle;
    private String video_url;
    private String thumbnail;
    private String img_url;
    private Double body_score;
    private Double brain_score;
    private Double behavior_score;
    private Double total_score;
    private Integer cv_handle;
    private Integer nlp_handle;
    private Long video_create_time;
    private String meeting_type;
    private Integer nlp_file;
    private Integer is_match;
    private Double team_distance;
    private Integer is_merge;
//    private Double a_positive_rate;
//    private Double v_positive_rate;
//    private Double v_negative_rate;
//    private Double a_negative_rate;
}
