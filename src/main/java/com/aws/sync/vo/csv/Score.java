package com.aws.sync.vo.csv;

import lombok.Data;

@Data
public class Score {
    private Long meeting_id;
    private Double body_score;
    private Double behavior_score;
    private Double total_score;
    private Double nlp_speaker_time;
    private Double nlp_equal_participation;
    private Long duration;
    private Long meeting_start_time;
    private Double brain_score;
}
