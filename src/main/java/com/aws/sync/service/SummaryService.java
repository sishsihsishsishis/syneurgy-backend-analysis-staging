package com.aws.sync.service;


import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.SummaryTable;
import com.baomidou.mybatisplus.extension.service.IService;



public interface SummaryService extends IService<SummaryTable> {

    RestResult getSummaryByMeetingId(Long meetingID);

    String getSummaryDataByMeetingId(Long meetingId);
}
