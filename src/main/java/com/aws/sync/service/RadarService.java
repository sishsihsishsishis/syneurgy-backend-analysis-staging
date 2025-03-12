package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.Radar;
import com.aws.sync.vo.MeetingSummaryVO;
import com.aws.sync.vo.NlpVO;
import com.aws.sync.vo.RadarVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RadarService extends IService<Radar> {
    int insertRadar(List<Radar> radars);

    List<RadarVO> findKV(Long meetingID);

    RestResult getLatestFiveRadar(Long teamID);

    void setEngagementAndAgency(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO);
}
