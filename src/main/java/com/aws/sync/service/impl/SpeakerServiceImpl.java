package com.aws.sync.service.impl;

import com.aws.sync.entity.match.Speaker;

import com.aws.sync.mapper.SpeakerMapper;
import com.aws.sync.service.SpeakerService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakerServiceImpl extends ServiceImpl<SpeakerMapper, Speaker> implements SpeakerService {
    @Autowired
    SpeakerMapper speakerMapper;

    @Override
    public List<Speaker> getSpeakerList(Long meeting_id) {
        LambdaQueryWrapper<Speaker> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Speaker::getMeeting_id, meeting_id);
        List<Speaker> speakerList = speakerMapper.selectList(lambdaQueryWrapper);
        return speakerList;
    }

    @Override
    public void updateSpeaker(Long meetingID, int count) {
        for (int i = 0; i < count; i++) {
            if(i < 10){
                save(new Speaker(meetingID,"speaker0" + i));
            }else {
                save(new Speaker(meetingID,"speaker" + i));
            }
        }
    }

}
