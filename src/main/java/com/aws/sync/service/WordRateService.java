package com.aws.sync.service;

import com.aws.sync.entity.WordRate;
import com.aws.sync.vo.WordRateVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface WordRateService extends IService<WordRate> {
    Map<Integer, List<WordRateVO>> queryDataByMeetingID(Long meetingID);
}
