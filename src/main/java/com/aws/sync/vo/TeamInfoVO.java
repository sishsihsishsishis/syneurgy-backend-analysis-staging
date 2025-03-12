package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamInfoVO {
    private Long teamId;
    private Double global;
    private Long duration;
    private List<MeetingScoreVO> score;
    private HashMap<String, Double> status;
    private HashMap<String, Double> performance;
    private HashMap<String, Double> sentiment;
    private HashMap<String, Double> dimension;

}
