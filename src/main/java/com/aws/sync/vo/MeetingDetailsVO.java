package com.aws.sync.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class MeetingDetailsVO {
    private Long teamId;
    private List<Long> meetingIds;
    private List<Double> global;
    private HashMap<String, List<Double>> status;
    private HashMap<String, List<Double>> performance;
    private HashMap<String, List<Double>> sentiment;
    private HashMap<String, List<Double>> dimension;

    public MeetingDetailsVO(Long teamId) {
        this.teamId = teamId;
    }
}
