package com.aws.sync.vo;

import lombok.Data;

@Data
public class MeetingSummaryVO {
    //Engagement:  radar chart 的 ”absorption or task engagement”
    //Alignment: sync score?
    //Agency: 5 radar average
    //Stress: the whole meeting HRV -> stress
    //Burnout: (engagement + stress) / 2 ?
    private Long meetingId;
    private Double engagement;
    private Double alignment;
    private Double agency;
    private Double stress;
    private Double burnout;
    private Double score;
}
