package com.aws.sync.vo;

import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.WordInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GptRequestInfoVO {
    private Long teamId;
    private Long meetingId;
    private String transcript;
    private Double synchronyScore;
//    private Double engagement;
//    private Double alignment;
//    private Double agency;
//    private Double burnout;
//    private Double stress;
    private MeetingSummaryVO meetingSummaryVO;
    private Double overallSynchrony;
    private Double brain;
    private Double body;
    private Double behavior;
    private List<Double> progressData;
    private MeetingTable meetingScore;
    private Double positive;
    private Double neutral;
    private Double negative;
    private Map<String, Double> dimensions;
    private Integer left;
    private Integer right;
    private Integer top;
    private Integer bottom;
    private List<WordInfo> wordInfoList;
}
