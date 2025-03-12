package com.aws.sync.vo;

import com.aws.sync.vo.detection.DetectionVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MeetingRadarVO {
    private String userId;
    private Long meetingId;
    private String username;
    private List<DetectionVO> results = new ArrayList<>();

}