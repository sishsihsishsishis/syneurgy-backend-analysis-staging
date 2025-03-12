package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.AResult;
import com.aws.sync.entity.NlpWordCount;
import com.aws.sync.entity.match.Speaker;
import com.aws.sync.mapper.AResultMapper;
import com.aws.sync.mapper.NlpWordCountMapper;
import com.aws.sync.service.NlpWordCountService;
import com.aws.sync.service.SpeakerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NlpWordCountServiceImpl extends ServiceImpl<NlpWordCountMapper, NlpWordCount> implements NlpWordCountService {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    NlpWordCountMapper nlpWordCountMapper;

    @Autowired
    SpeakerService speakerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(Map<String, Map<Integer, Integer>> wordCount, Long meetingID) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        NlpWordCountMapper nlpWordCountMapper = session.getMapper(NlpWordCountMapper.class);
        for (Map.Entry<String, Map<Integer, Integer>> entry : wordCount.entrySet()) {
            Map<Integer, Integer> speakerWordCount = entry.getValue();
            for (Map.Entry<Integer, Integer> timeSlotEntry : speakerWordCount.entrySet()) {
//                System.out.println("  " + timeSlotEntry.getKey() + "s: " + timeSlotEntry.getValue());
                nlpWordCountMapper.addOneByOne(new NlpWordCount(meetingID, entry.getKey(), timeSlotEntry.getKey(), timeSlotEntry.getValue()));
            }
        }
        session.commit();
        session.close();
    }

    @Override
    public RestResult getWordCountByMeetingID(Long meetingID) {
        List<Speaker> speakerList = speakerService.getSpeakerList(meetingID);
        HashMap<String,List<List<Integer>>> ans = new HashMap<>();
        for (Speaker speaker : speakerList) {
            LambdaQueryWrapper<NlpWordCount> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(NlpWordCount::getMeeting_id,meetingID).eq(NlpWordCount::getSpeaker,speaker.getSpeaker_name()).orderByAsc(NlpWordCount::getTime_ms);
            List<NlpWordCount> nlpWordCounts = nlpWordCountMapper.selectList(lambdaQueryWrapper);
            List<List<Integer>> count = new ArrayList<>();
            for (NlpWordCount nlpWordCount : nlpWordCounts) {
                count.add(Arrays.asList(nlpWordCount.getTime_ms(),nlpWordCount.getCount()));
            }
            ans.put(speaker.getSpeaker_name(),count);
        }
        return RestResult.success().data(ans);
    }
}
