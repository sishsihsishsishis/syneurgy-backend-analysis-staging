package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.GptSummary;
import com.aws.sync.entity.NlpSummary;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.vo.GptRequestInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface GptService extends IService<GptSummary> {

    List<NlpSummary> summaryNlp(List<String[]> nlp_data, Long meetingID, HashMap<String, Object> deleteMap);

    void summaryNlpText(List<String[]> nlp_data, List<NlpTable> nlpTables, Long meetingID, HashMap<String, Object> deleteMap);

    List<String> processNLPData(List<String[]> nlpData, Long meetingID);

    double processRadarTrust(Long meetingID) throws IOException;

    void processAllHighlight(List<String[]> nlpData, Long meetingID);

    String sendMessageToGpt(String content, String systemPrompt);

    String sendMessageToGpt4(String content, String systemPrompt);

    RestResult queryByTeamIdAndLabel(Long teamId, int label);

    void processTeamAndMeetingGptData(GptRequestInfoVO gptRequestInfoVO);

    RestResult queryByMeetingIdAndLabel(Long meetingId, int label);
}
