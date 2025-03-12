package com.aws.sync.service.impl;

import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.mapper.NlpMapper;
import com.aws.sync.mapper.PieSpeakerMapper;
import com.aws.sync.service.NlpService;
import com.aws.sync.service.PieSpeakerService;
import com.aws.sync.vo.BarSpeakerVO;
import com.aws.sync.vo.NlpRateVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PieSpeakerServiceImpl extends ServiceImpl<PieSpeakerMapper, PieSpeaker> implements PieSpeakerService {
    @Resource
    PieSpeakerMapper pieSpeakerMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPie(List<PieSpeaker> pieSpeakers) {
        return pieSpeakerMapper.addBatch(pieSpeakers);
    }

    @Override
    public List<PieSpeakerVO> findSpeaker(Long meetingID) {
        return pieSpeakerMapper.findSpeaker(meetingID);
    }

    @Override
    public List<BarSpeakerVO> findBarSpeaker(Long meetingID) {
        return pieSpeakerMapper.findBarSpeaker(meetingID);
    }

    @Override
    public List<NlpRateVO> findSpeakerRate(Long meetingID) {
        return pieSpeakerMapper.findSpeakerRate(meetingID);
    }
}
