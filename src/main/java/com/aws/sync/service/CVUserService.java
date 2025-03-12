package com.aws.sync.service;




import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.MatchDTO;
import com.aws.sync.entity.match.CVUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CVUserService extends IService<CVUser> {

    List<CVUser> getUserList(Long meeting_id);

    int findUserCount(Long meetingID);

    RestResult matchLoginUser(Long meetingID, MatchDTO matchDTO);

    RestResult getMatchResult(Long meetingID);

    RestResult updateMatch(Long meetingID, String user);

    RestResult updateEmailSend(Long meetingID, String user);
    List<CVUser> getByUserId(String userId);

    List<CVUser> getByUserIdAndTeamId(String userId, Long teamId);

    void handleUserMatch(String userId, Long username, Long meetingId);

    List<CVUser> getMeetingAndUserMapByUserId(String userId);
}
