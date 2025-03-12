package com.aws.sync.service;

import com.aws.sync.entity.PieEmotion;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.PieEmotionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PieEmotionService extends IService<PieEmotion> {
    int insertPie(List<PieEmotion> pieEmotions);

    List<PieEmotionVO> findEmotion(Long meetingID);
}
