package com.aws.sync.mapper;


import com.aws.sync.entity.PieEmotion;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.PieEmotionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PieEmotionMapper extends BaseMapper<PieEmotion> {
    int addBatch(@Param("PieEmotion") List<PieEmotion> pieEmotions);

    List<PieEmotionVO> findEmotion(Long meetingID);
}
