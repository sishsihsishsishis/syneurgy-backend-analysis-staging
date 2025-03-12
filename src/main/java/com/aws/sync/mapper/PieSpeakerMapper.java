package com.aws.sync.mapper;


import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.BarSpeakerVO;
import com.aws.sync.vo.NlpRateVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PieSpeakerMapper extends BaseMapper<com.aws.sync.entity.PieSpeaker> {
    int addBatch(@Param("PieSpeaker") List<PieSpeaker> pieSpeakers);

    List<PieSpeakerVO> findSpeaker(Long meetingID);

    List<BarSpeakerVO> findBarSpeaker(Long meetingID);

    List<NlpRateVO> findSpeakerRate(Long meetingID);
}
