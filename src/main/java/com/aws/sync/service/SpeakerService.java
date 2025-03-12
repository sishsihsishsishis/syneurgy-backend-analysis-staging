package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.UpdateSpeakerNameDTO;
import com.aws.sync.entity.match.Speaker;
import com.aws.sync.entity.match.SpeakerUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SpeakerService extends IService<Speaker> {

    List<Speaker> getSpeakerList(Long meeting_id);

    void updateSpeaker(Long meetingID, int count);
}
