package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.UpdateSpeakerNameDTO;
import com.aws.sync.entity.match.SpeakerUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface SpeakerUserService extends IService<SpeakerUser> {
    RestResult updateSpeakerNames(UpdateSpeakerNameDTO updateSpeakerNameDTO) throws IOException;
    List<SpeakerUser> getSpeakerUsersByMeetingId(Long meeting_id);
    List<String> getAllSpeakerNames(Long meeting_id);

    RestResult getMatchUser(Long meetingID);

    HashMap<String,List<String>> getSpeakerMap(Long meetingID);

    void deleteByMeetingID(Long meetingID);

    SpeakerUser queryDataByMeetingIdAndName(Long meetingId, String username);
}
