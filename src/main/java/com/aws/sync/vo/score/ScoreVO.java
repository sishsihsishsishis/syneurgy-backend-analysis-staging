package com.aws.sync.vo.score;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreVO {
    private Long meetingId;
    private Long teamId;
    private Double brain_score;
    private Double body_score;
    private Double behavior_score;
    private Double total_score;
    private Double nlp_speaker_time;
    private Double nlp_equal_participation;
    private Double coefficient_body;
    private Double coefficient_behaviour;
    private Double coefficient_brain;
    private Double coefficient_total;
    private Double weight_brain;
    private Double weight_body;
    private Double weight_behaviour;
    private Double weight_nlp_speak_time;
    private Double weight_nlp_equal_participation;


}
