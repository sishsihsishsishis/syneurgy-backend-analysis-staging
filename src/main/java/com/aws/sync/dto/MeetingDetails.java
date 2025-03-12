package com.aws.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDetails {
    private Long meeting_id;
    private Long team_id;
    private Integer is_handle;
    private String video_url;
    private String img_url;
    private Long duration;
    private String thumbnail;
    private Long meeting_start_time;
    private Double total_score;
    private Integer cv_handle;
    private Integer nlp_handle;
    private Long video_create_time;
    private String meeting_type;
    private String meeting_name;
    private Double threshold;
}
