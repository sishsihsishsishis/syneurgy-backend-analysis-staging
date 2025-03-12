package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.WordRate;
import com.aws.sync.vo.NlpVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NlpService extends IService<NlpTable> {
    int insertNlp(List<NlpTable> nlpTables);

    List<NlpVO> findNlp(Long meetingID);

    RestResult handleNlp(Long meetingID) throws IOException;

    RestResult getNlpDataByMeetingID(Long meetingID);

    RestResult handlePartNlp(Long meetingID, Integer part) throws IOException;

    RestResult handleNplSummary(Long meetingId) throws IOException;

    RestResult nlpSummary(Long meetingId) throws IOException;

    RestResult nlpClassification(Long meetingId) throws IOException;

    void handleDetectionNlp(Long meeting_id) throws IOException;

    CompletableFuture<String> sendPostRequestAsync(String url, Long meetingId);

    List<WordRate> processWordRate(List<String[]> data, Long meetingId);

    RestResult handleWordInfo(Long meetingId) throws IOException;
}
