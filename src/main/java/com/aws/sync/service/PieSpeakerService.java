package com.aws.sync.service;

import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.PieSpeaker;
import com.aws.sync.vo.BarSpeakerVO;
import com.aws.sync.vo.NlpRateVO;
import com.aws.sync.vo.PieSpeakerVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PieSpeakerService extends IService<PieSpeaker> {
    int insertPie(List<PieSpeaker> pieSpeakers);

    List<PieSpeakerVO> findSpeaker(Long meetingID);

    List<BarSpeakerVO> findBarSpeaker(Long meetingID);

    List<NlpRateVO> findSpeakerRate(Long meetingID);
}
