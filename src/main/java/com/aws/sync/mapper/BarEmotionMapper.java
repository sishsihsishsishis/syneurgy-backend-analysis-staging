package com.aws.sync.mapper;


import com.aws.sync.entity.BarEmotion;
import com.aws.sync.entity.PieEmotion;
import com.aws.sync.vo.BarEmotionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BarEmotionMapper extends BaseMapper<BarEmotion> {
    int addBatch(@Param("BarEmotion") List<BarEmotion> barEmotions);

    List<BarEmotionVO> findEmotion(Long meetingID);
}
