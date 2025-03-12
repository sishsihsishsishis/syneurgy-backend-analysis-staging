package com.aws.sync.service.impl;

import com.aws.sync.config.common.SynchronyMomentInsightEnum;
import com.aws.sync.entity.IndividualSync;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.SynchronyMoment;
import com.aws.sync.mapper.MeetingMapper;
import com.aws.sync.mapper.NlpMapper;
import com.aws.sync.mapper.SynchronyMomentMapper;
import com.aws.sync.service.*;
import com.aws.sync.utils.NlpUtil;
import com.aws.sync.vo.SynchronyMomentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class SynchronyMomentServiceImpl extends ServiceImpl<SynchronyMomentMapper,SynchronyMoment> implements SynchronyMomentService {
    private String MOMENT_PROMPT = "Role\n" +
            "You, as a highly skilled AI expert trained in language understanding and text finding, are able to comprehensively find, read and understand, and accurately analyze long segments of conversational text.\n" +
            "\n" +
            "Objective\n" +
            "After fully reading and understanding the dialog text, find these sections in the text and make a statement for each section based on the START TIME and END TIME I give you.\n" +
            "\n" +
            "Style\n" +
            "Must be accurate and robust, no matter how many times you output it, the result will be the same.\n" +
            "\n" +
            "Tone of voice\n" +
            "Accurate and persuasive\n" +
            "\n" +
            "Audience\n" +
            "People who are interested in communicating online as a team\n" +
            "\n" +
            "Response\n" +
            "Include only start time, end time, and clip description\n" +
            "The template is as follows\n" +
            "Start time:25.23\n" +
            "End time:55.16\n" +
            "DESCRIPTION:The segment describes the product's current popularity in the marketplace and highlights future development strategies.";

    @Autowired
    IndividualSyncService individualSyncService;

    @Resource
    NlpMapper nlpMapper;

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    GptService gptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSmallest3(Long meetingID) throws IOException {
        log.info("[SynchronyMomentServiceImpl][saveSmallest3] meetingId:{}", meetingID);
        //TODO: 添加判断是否已经添加
        LambdaQueryWrapper<IndividualSync> lambdaQueryWrapper = new LambdaQueryWrapper<IndividualSync>()
                .eq(IndividualSync::getMeeting_id, meetingID);

        List<IndividualSync> individualSyncList = individualSyncService.list(lambdaQueryWrapper);

        Map<Double, Double> timeScoreMap = new HashMap<>();
            System.out.println("timeScoreMap");
        for (IndividualSync individualSync : individualSyncList) {
            timeScoreMap.put(individualSync.getTime_ms(), timeScoreMap.getOrDefault(individualSync.getTime_ms(),0.0d) + individualSync.getDistance());
        }
        List<Map.Entry<Double, Double>> list = new ArrayList<>(timeScoreMap.entrySet());
        list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));


        List<String[]> nlpData = amazonUploadService.readNlpLine("nlp_result.txt", meetingID.toString());
        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingID, nlpData);


        HashMap<Long, Integer> smallest3Label= new HashMap<>();
        for (int i = 0; i < 3; i++) {
            if(list.size() > i) {
                Long time = list.get(i).getKey().longValue();
                if (time != null) smallest3Label.put(time, i);
            }
        }


        List<SynchronyMoment> synchronyMomentList = new ArrayList<>();
        //找到在这个时间段内的nlp文本，发送给GPT进行总结。
        for (Map.Entry<Long, Integer> entry : smallest3Label.entrySet()) {

            Long start = entry.getKey() / 1000;
            Integer label = entry.getValue();
            Long end = start + 30;
            StringBuilder sentence = new StringBuilder();
            sentence.append("Speaker\tStart\tEnd\tSentence\n");
            boolean hasData = false;
            for (NlpTable nlpTable : nlpTables) {
                if ((nlpTable.getStarts() >= start && end >= nlpTable.getStarts())
                    || (nlpTable.getEnds() >= start && end >= nlpTable.getEnds())){
                    sentence.append(nlpTable.getSpeaker() + "\t")
                            .append(nlpTable.getStarts() + "\t")
                            .append(nlpTable.getEnds() + "\t")
                            .append(nlpTable.getSentence() + "\n");
                    hasData = true;
                }
            }
            //synchronyMomentList.add(new SynchronyMoment(meetingID, start * 1000.0, end * 1000.0, label, sentence.toString()));
            //如果这个时间范围内有数据
            if (hasData) {
                String message = gptService.sendMessageToGpt(sentence.toString(), MOMENT_PROMPT);
                System.out.println(message);
                SynchronyMoment synchronyMoment = parseGptMoment(message, meetingID, label);
                synchronyMomentList.add(synchronyMoment);
            }
        }
        for (SynchronyMoment synchronyMoment : synchronyMomentList) {
            System.out.println(synchronyMoment.toString());
        }
        //批量添加
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id", meetingID);
        removeByMap(deleteMap);
        saveBatch(synchronyMomentList);
    }

    private SynchronyMoment parseGptMoment(String content, Long meetingId, int label) {
        log.info("[SynchronyMomentServiceImpl][parseGptMoment] meetingId:{}, content:{}", meetingId, content);
        String[] split = content.split("\n");
        Double start = Double.valueOf(split[0].split(":")[1]);
        Double end = Double.valueOf(split[1].split(":")[1]);
        String description = split[2].split(":")[1];
        SynchronyMoment synchronyMoment = new SynchronyMoment(meetingId, start * 1000.0, end * 1000.0, label, description);
        return synchronyMoment;
    }
    @Override
    public List<SynchronyMomentVO> getSynchronyMomentVOByMeetingId(Long meetingID) {
        LambdaQueryWrapper<SynchronyMoment> synchronyMomentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        synchronyMomentLambdaQueryWrapper.eq(SynchronyMoment::getMeeting_id, meetingID);
        List<SynchronyMoment> synchronyMoments = list(synchronyMomentLambdaQueryWrapper);
        List<SynchronyMomentVO> data = new ArrayList<>();
        for (SynchronyMoment synchronyMoment : synchronyMoments) {
            SynchronyMomentVO synchronyMomentVO = new SynchronyMomentVO();
            BeanUtils.copyProperties(synchronyMoment, synchronyMomentVO);
            synchronyMomentVO.setInsight(SynchronyMomentInsightEnum.getSentenceFromLabel(synchronyMoment.getLabel()));
            data.add(synchronyMomentVO);
        }
        return data;
    }
}
