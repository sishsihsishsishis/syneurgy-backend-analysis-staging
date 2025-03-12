package com.aws.sync.vo;


import lombok.Data;


@Data
public class VideoVO {
    private Long meeting_id;
    private Long team_id;
    private Integer is_handle;
    private String video_url;
    private String img_url;
    private Integer is_update;
    private Long duration;
    private String thumbnail;
    private Long upload_time;
    private Long meeting_start_time;
    private Double total_score;
    private Integer cv_handle;
    private Integer nlp_handle;
    private Long video_create_time;
    private String meeting_type;
    private String meeting_name;
    private Double threshold;
    private Double body_score;
    private Double brain_score;
    private Double behavior_score;
}
