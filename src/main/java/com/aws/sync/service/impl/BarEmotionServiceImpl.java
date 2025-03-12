package com.aws.sync.service.impl;

import com.aws.sync.entity.BarEmotion;
import com.aws.sync.entity.PieEmotion;
import com.aws.sync.mapper.BarEmotionMapper;
import com.aws.sync.mapper.PieEmotionMapper;
import com.aws.sync.service.BarEmotionService;
import com.aws.sync.service.PieEmotionService;
import com.aws.sync.vo.BarEmotionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BarEmotionServiceImpl extends ServiceImpl<BarEmotionMapper, BarEmotion> implements BarEmotionService {
    @Resource
    BarEmotionMapper barEmotionMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBar(List<BarEmotion> barEmotions) {
        return barEmotionMapper.addBatch(barEmotions);
    }

    @Override
    public List<BarEmotionVO> findEmotion(Long meetingID) {
        return barEmotionMapper.findEmotion(meetingID);
    }
}
