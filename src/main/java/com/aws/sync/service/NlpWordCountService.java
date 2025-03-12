package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.AResult;
import com.aws.sync.entity.NlpWordCount;
import com.aws.sync.vo.DataAVO;
import com.aws.sync.vo.csv.AMean;
import com.aws.sync.vo.csv.AllUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface NlpWordCountService extends IService<NlpWordCount> {

    void addOneByOne(Map<String, Map<Integer, Integer>> wordCount, Long meetingID);

    RestResult getWordCountByMeetingID(Long meetingID);
}
