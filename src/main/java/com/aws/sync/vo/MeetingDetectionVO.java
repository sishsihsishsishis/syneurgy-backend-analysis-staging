package com.aws.sync.vo;

import com.aws.sync.vo.detection.DetectionVO;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MeetingDetectionVO {

    private String userId;
    private Long meetingId;
    private String speaker;
    private String username;
    private Map<String, List<DetectionVO>> results = new HashMap<>();

    public void addResult(String type, List<DetectionVO> result) {
        this.results.put(type, result);
    }

    public Map<String, List<DetectionVO>> getResults() {
        return this.results;
    }
}