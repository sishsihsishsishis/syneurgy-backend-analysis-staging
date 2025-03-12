package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.AnalysisInfo;
import com.aws.sync.entity.VideoAnalysisTable;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;

public interface AnalysisService extends IService<AnalysisInfo> {
    RestResult saveAnalysisInfo(Long meetingId, HashMap<String, List<Long>> time);

    void judgeIfAnalysisAsync(VideoAnalysisTable videoAnalysisTable, String currentFile) throws Exception;
}
