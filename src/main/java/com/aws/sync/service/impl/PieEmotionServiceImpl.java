package com.aws.sync.service.impl;

import com.aws.sync.entity.PieEmotion;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.mapper.PieEmotionMapper;
import com.aws.sync.mapper.PieSpeakerMapper;
import com.aws.sync.service.PieEmotionService;
import com.aws.sync.service.PieSpeakerService;
import com.aws.sync.vo.PieEmotionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PieEmotionServiceImpl extends ServiceImpl<PieEmotionMapper, PieEmotion> implements PieEmotionService {
    @Resource
    PieEmotionMapper pieEmotionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPie(List<PieEmotion> pieEmotions) {
        return pieEmotionMapper.addBatch(pieEmotions);
    }

    @Override
    public List<PieEmotionVO> findEmotion(Long meetingID) {
        return pieEmotionMapper.findEmotion(meetingID);
    }
}
