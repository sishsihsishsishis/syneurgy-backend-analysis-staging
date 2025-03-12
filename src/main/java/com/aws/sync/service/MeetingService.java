package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.MeetingSearchDTO;
import com.aws.sync.dto.SearchDTO;
import com.aws.sync.dto.TeamMeetingDTO;
import com.aws.sync.vo.MeetingSummaryVO;
import com.aws.sync.vo.TeamSyncVO;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.vo.VideoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface MeetingService extends IService<MeetingTable> {
    List<VideoVO> findVideo();

    RestResult checkHandle(Long meetingID);

    RestResult updateHandle(Long meetingID);

    RestResult updateScore(Score score);

    Score getScore(Long meetingID);

    String findFileName(Long meetingID);


    void updateMeetingStartTime(Long meetingID, Long date);

    Long saveMeeting(MeetingTable meetingTable);

    RestResult<Void> updateMeetingName(Long meetingId, String meetingName);

    Boolean checkNlpHandle(Long meetingID);

    void updateNlpHandle(Long meetingID);

    Boolean checkCVHandle(Long meetingID);

    boolean checkMatchHandle(Long meetingID);

    void updateCVHandle(Long meetingID);

    void updateDataHandle(Long meetingID);

    RestResult getLatestFiveScore(Long teamID);

//    RestResult<List<VideoVO>> findTeamMeetingByTeamID(Long teamID, long currentPage, long pageCount, String meetingName, String meetingType);

     RestResult getEMailNotSend();

    RestResult updateEmailSend(Long meetingID);

    boolean checkMatch(Long meeting_id);

    RestResult updateMatch(Long meeting_id);

    RestResult deleteMatch(Long meetingID);

    RestResult<Void> updateMeetingType(Long meetingID, String meetingType);

    void updateDuration(Long meetingID, Double time_ms);

    void updateNlpFile(Long meetingID);

    RestResult removeCvDataByMeetingId(Long meetingID);

    RestResult removeNlpDataByMeetingId(Long meetingID);

    RestResult searchMeeting(SearchDTO searchDTO);

    TeamSyncVO findTeamSync(Long meetingID);

    void updateUserMerge(Long meetingID);

    boolean checkUserMerge(Long meetingID);

    void processRadar(Long meetingId);

    RestResult<List<VideoVO>> findTeamMeetingByTeamID(Long teamID, MeetingSearchDTO meetingSearchDTO);

    List<MeetingTable> findMeetingsByTeamId(Long teamId, TeamMeetingDTO teamMeetingDTO);

    List<MeetingTable> getLatestFiveMeetingByTeam(Long teamId, String date);

    List<MeetingTable> getLatestNMeetingByTeam(Long teamId, String date, int count);

    RestResult modLatestMeetingTypeByTeamId(Long teamId, String meetingType, int modCount);

    List<MeetingTable> findLatestMeetingTypeByTeamId(Long teamId, String meetingType);

    List<MeetingTable> findMeetingInfoByTeamId(Long teamId, String meetingType);

    Page<Long> selectDistinctTeamIds(Integer pageNum, Integer pageSize);

    void handleMatch(Long meetingId) throws IOException;

    void handleNewMatch(Long meetingId) throws IOException;

    MeetingTable getByMeetingId(Long meetingID);

    List<MeetingTable> getMeetingByTeamId(Long teamId);

    List<MeetingTable> getAllMeetingByTeamId(Long teamId);

    void handleDetection(Long meeting_id, String name, int type) throws IOException;

    void handlePostureDetection(Long meeting_id, String name, int type) throws IOException;

    void handleBrainScore(Long meeting_id) throws IOException;

    void removeMeetingDataByMeetingId(Long meetingId);

    RestResult findTeamMeetingProgress(Long teamID, int progress);

    HashMap<String, Double> getAverageLatestFiveScore(Long teamId);

    MeetingSummaryVO getGlobalTeamMetricsInfo(Long teamId);

    MeetingSummaryVO computeTeamMeetingSummary(List<MeetingTable> meetingTables);
}
