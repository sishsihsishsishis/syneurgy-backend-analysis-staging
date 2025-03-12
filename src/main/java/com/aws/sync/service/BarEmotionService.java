package com.aws.sync.service;

import com.aws.sync.entity.BarEmotion;
import com.aws.sync.entity.PieEmotion;
import com.aws.sync.vo.BarEmotionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BarEmotionService extends IService<BarEmotion> {
    int insertBar(List<BarEmotion> barEmotions);

    List<BarEmotionVO> findEmotion (Long meetingID);
}
