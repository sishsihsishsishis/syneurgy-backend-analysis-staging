package com.aws.sync.vo.score;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamScoreVO {
    private Long meetingId;
    private String meetingName;
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
    private Long video_create_time;

    public TeamScoreVO(Long meetingId, Long teamId, Double brain_score, Double body_score, Double behavior_score, Double total_score, Double nlp_speaker_time, Double nlp_equal_participation) {
        this.meetingId = meetingId;
        this.teamId = teamId;
        this.brain_score = brain_score;
        this.body_score = body_score;
        this.behavior_score = behavior_score;
        this.total_score = total_score;
        this.nlp_speaker_time = nlp_speaker_time;
        this.nlp_equal_participation = nlp_equal_participation;
    }
}
