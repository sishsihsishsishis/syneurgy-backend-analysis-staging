package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.constants.MeetingConstants;
import com.aws.sync.dto.MeetingSearchDTO;
import com.aws.sync.dto.SearchDTO;
import com.aws.sync.dto.TeamMeetingDTO;
import com.aws.sync.entity.DetectionCV;
import com.aws.sync.entity.DetectionRadar;
import com.aws.sync.entity.match.SpeakerUser;
import com.aws.sync.entity.match.UserWithArea;
import com.aws.sync.entity.match.UserWithCenter;
import com.aws.sync.mapper.*;
import com.aws.sync.service.*;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.vo.*;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.entity.MeetingTable;

import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aws.sync.constants.CsvConstants.*;

@Service
@Slf4j
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, MeetingTable> implements MeetingService {
    @Resource
    MeetingMapper meetingMapper;

    @Autowired
    PieEmotionService pieEmotionService;

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    EmojiMapper emojiMapper;

    @Autowired
    CVUserMapper cvUserMapper;

    @Autowired
    AResultMapper aResultMapper;

    @Autowired
    VResultMapper vResultMapper;

    @Autowired
    RResultMapper rResultMapper;

    @Autowired
    IndividualScoreMapper individualScoreMapper;

    @Resource
    IndividualSyncMapper individualSyncMapper;

    @Resource
    ASyncMapper aSyncMapper;

    @Resource
    VSyncMapper vSyncMapper;

    @Resource
    RSyncMapper rSyncMapper;

    @Resource
    IndividualAMapper individualAMapper;

    @Resource
    IndividualVMapper individualVMapper;

    @Resource
    IndividualRMapper individualRMapper;

    @Resource
    AveSyncMapper aveSyncMapper;

    @Resource
    HeatmapMapper heatmapMapper;

    @Resource
    SectionMapper sectionMapper;

    @Resource
    NlpWordCountMapper nlpWordCountMapper;

    @Resource
    NlpMapper nlpMapper;

    @Resource
    PieSpeakerMapper pieSpeakerMapper;

    @Resource
    PieEmotionMapper pieEmotionMapper;

    @Resource
    PieActMapper pieActMapper;

    @Resource
    BarEmotionMapper barEmotionMapper;

    @Resource
    RadarMapper radarMapper;

    @Resource
    SpeakerMapper speakerMapper;

    @Resource
    SpeakerUserMapper speakerUserMapper;

    @Resource
    UserDistanceMapper userDistanceMapper;

    @Resource
    PosAndNegRateMapper posAndNegRateMapper;

    @Resource
    SynchronyMomentMapper synchronyMomentMapper;

    @Resource
    SummaryMapper summaryMapper;

    @Resource
    DetectionCVService detectionCVService;

    @Resource
    DetectionNLPService detectionNLPService;

    @Resource
    AnalysisMapper analysisMapper;

    @Resource
    DetectionCVMapper detectionCVMapper;

    @Resource
    DetectionNLPMapper detectionNLPMapper;

    @Resource
    UniverseGroupEmojiMapper universeGroupEmojiMapper;

    @Resource
    HighlightStatementMapper highlightStatementMapper;

    @Resource
    NlpSummaryMapper nlpSummaryMapper;

    @Resource
    UserAvatarMapper userAvatarMapper;

    @Resource
    UserContributionMapper userContributionMapper;

    @Resource
    VideoAnalysisMapper videoAnalysisMapper;

    @Resource
    DetectionRadarService detectionRadarService;

    @Autowired
    RadarService radarService;

    @Override
    public List<VideoVO> findVideo() {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .orderByDesc(MeetingTable::getUpload_time);
        List<MeetingTable> meetingTables = meetingMapper.selectList(lambdaQueryWrapper);
        List<VideoVO> voList = new ArrayList<>();
        for(MeetingTable m : meetingTables){
            VideoVO videoVO = new VideoVO();
            BeanUtils.copyProperties(m, videoVO);
            voList.add(videoVO);
        }
//        return meetingMapper.findVideo();
        return voList;
    }

    @Override
    public RestResult checkHandle(Long meetingID) {
        VideoVO handle = meetingMapper.findHandle(meetingID);
        if (handle == null){
            return RestResult.fail().message("Meeting does not exist");
        }else if(handle.getIs_handle()==1){
            return RestResult.fail().message("Meeting has been handled");
        }
        return RestResult.success().data(handle);
    }

    @Override
    public RestResult updateHandle(Long meetingID) {
        meetingMapper.updateHandle(meetingID);
        return RestResult.success();
    }

    @Override
    public RestResult updateScore(Score score) {
        meetingMapper.updateScore(score);
        return RestResult.success();
    }

    @Override
    public Score getScore(Long meetingID) {
        return meetingMapper.getScore(meetingID);
    }

    @Override
    public String findFileName(Long meetingID) {
        return meetingMapper.findFileName(meetingID);
    }

    @Override
    public void updateMeetingStartTime(Long meetingID, Long date) {
        meetingMapper.updateTime(meetingID,date);
    }

    @Override
    public Long saveMeeting(MeetingTable meetingTable) {
        return meetingMapper.savaMeeting(meetingTable);
    }

    /**
     * 根据会议ID更新会议名称的具体逻辑实现。
     *
     * @param meetingId 会议ID。
     * @param meetingName 新的会议名称。
     * @return RestResult 包含操作结果的对象，通常是操作成功的确认。
     */
    @Override
    public RestResult<Void> updateMeetingName(Long meetingId, String meetingName) {
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<MeetingTable>()
                .eq(MeetingTable::getMeeting_id, meetingId)
                .set(MeetingTable::getMeeting_name, meetingName);
        meetingMapper.update(null,updateWrapper);
        return RestResult.success().message("update meeting name success");
    }

    @Override
    public Boolean checkNlpHandle(Long meetingID) {
        MeetingTable meetingTable = meetingMapper.selectById(meetingID);
        return CsvConstants.NLP_HANDLE.equals(meetingTable.getNlp_handle());
    }

    @Override
    public void updateNlpHandle(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MeetingTable::getMeeting_id,meetingID).set(MeetingTable::getNlp_handle, CsvConstants.NLP_HANDLE);
        meetingMapper.update(null,updateWrapper);
    }

    @Override
    public Boolean checkCVHandle(Long meetingID) {
        MeetingTable meetingTable = meetingMapper.selectById(meetingID);
        return CsvConstants.CV_HANDLE.equals(meetingTable.getCv_handle());
    }

    @Override
    public boolean checkMatchHandle(Long meetingID) {
        MeetingTable meetingTable = meetingMapper.selectById(meetingID);
        if(meetingTable != null && meetingTable.getIs_match() == MATCH_HANDLE){
            return true;
        }
        return false;
    }

    @Override
    public void updateCVHandle(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MeetingTable::getMeeting_id,meetingID).set(MeetingTable::getCv_handle, CsvConstants.CV_HANDLE);
        meetingMapper.update(null,updateWrapper);
    }

    @Override
    public void updateDataHandle(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(MeetingTable::getMeeting_id,meetingID).set(MeetingTable::getIs_handle, DATA_HANDLE);
        meetingMapper.update(null,lambdaUpdateWrapper);
    }

    @Override
    public RestResult getLatestFiveScore(Long teamID) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<MeetingTable>()
                .eq(MeetingTable::getTeam_id, teamID)
                .eq(MeetingTable::getIs_handle, DATA_HANDLE)
                .orderByDesc(MeetingTable::getVideo_create_time).last("LIMIT 5");
        List<MeetingTable> latestFiveMeeting = meetingMapper.selectList(lambdaQueryWrapper);

        HashMap<String,Double> latestFiveScore = new HashMap<>();
        latestFiveScore.put("total_score", null);
        latestFiveScore.put("behavior_score", null);
        latestFiveScore.put("body_score", null);
        latestFiveScore.put("brain_score", null);
        latestFiveScore.put("negative_rate", null);
        latestFiveScore.put("neutral_rate", null);
        latestFiveScore.put("positive_rate", null);
        if (latestFiveMeeting == null || latestFiveMeeting.size() == 0) {
            return RestResult.success().data(latestFiveScore);
        }

        Double total_score = 0.0d;
        Double behavior_score = 0.0d;
        Double body_score = 0.0d;
        Double brain_score = 0.0d;
        Double negative_rate = 0.0d;
        Double neutral_rate = 0.0d;
        Double positive_rate = 0.0d;

        int cnt_total = 0;
        int cnt_behavior = 0;
        int cnt_body = 0;
        int cnt_brain = 0;
        int cnt_negative = 0;
        int cnt_neutral = 0;
        int cnt_positive = 0;

        for (MeetingTable m : latestFiveMeeting) {
            if (m.getTotal_score() != null) {
                total_score += m.getTotal_score();
                cnt_total++;
            }
            if (m.getBehavior_score() != null) {
                behavior_score += m.getBehavior_score();
                cnt_behavior++;
            }
            if (m.getBody_score() != null) {
                body_score += m.getBody_score();
                cnt_body++;
            }
            if (m.getBrain_score() != null) {
                brain_score += m.getBrain_score();
                cnt_brain++;
            }
            List<PieEmotionVO> emotion = pieEmotionService.findEmotion(m.getMeeting_id());
            for (PieEmotionVO p : emotion) {
                if("Negative".equalsIgnoreCase(p.getEmotion())){
                    negative_rate += p.getEmotion_time_rate();
                    cnt_negative ++;
                } else if("Neutral".equalsIgnoreCase(p.getEmotion())){
                    neutral_rate += p.getEmotion_time_rate();
                    cnt_neutral ++;
                }
                else if("Positive".equalsIgnoreCase(p.getEmotion())){
                    positive_rate += p.getEmotion_time_rate();
                    cnt_positive++;
                }
            }
        }


        if (cnt_total != 0) {
            latestFiveScore.put("total_score",total_score / cnt_total);
        }
        if (cnt_behavior != 0) {
            latestFiveScore.put("behavior_score",behavior_score / cnt_behavior);
        }
        if (cnt_body != 0) {
            latestFiveScore.put("body_score",body_score / cnt_body);
        }
        if (cnt_brain != 0) {
            latestFiveScore.put("brain_score",brain_score / cnt_brain);
        }
        if (cnt_negative != 0) {
            latestFiveScore.put("negative_rate",negative_rate / cnt_negative);
        }
        if (cnt_neutral != 0) {
            latestFiveScore.put("neutral_rate",neutral_rate / cnt_neutral);
        }
        if (cnt_positive != 0) {
            latestFiveScore.put("positive_rate",positive_rate / cnt_positive);

        }

        return RestResult.success().data(latestFiveScore);
    }

    @Override
    public RestResult findTeamMeetingByTeamID(Long teamID, MeetingSearchDTO meetingSearchDTO) {
        Page<MeetingTable> page= new Page<>(meetingSearchDTO.getCurrentPage(),meetingSearchDTO.getPageCount());
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(MeetingTable::getTeam_id,teamID);

        if(meetingSearchDTO.getMeetingName() != null && !meetingSearchDTO.getMeetingName().isEmpty()){
            lambdaQueryWrapper.apply("meeting_name ILIKE {0}", "%" + meetingSearchDTO.getMeetingName() + "%");
        }

        if(meetingSearchDTO.getMeetingType() != null && !meetingSearchDTO.getMeetingType().isEmpty()){
            lambdaQueryWrapper.eq(MeetingTable::getMeeting_type,meetingSearchDTO.getMeetingType());
        }

        Integer progress = meetingSearchDTO.getProgress();
        if (progress != null) {
            lambdaQueryWrapper.eq(MeetingTable::getIs_handle, progress);
        }

        if (meetingSearchDTO.getStart() != null && meetingSearchDTO.getEnd() != null) {
            lambdaQueryWrapper.between(MeetingTable::getUpload_time, meetingSearchDTO.getStart(), meetingSearchDTO.getEnd());
        }

        if (meetingSearchDTO.getSortType() != null && meetingSearchDTO.getSortType() == 1) {
            lambdaQueryWrapper.orderByDesc(MeetingTable::getVideo_create_time);
        } else {
            lambdaQueryWrapper.orderByDesc(MeetingTable::getUpload_time);
        }
        Page<MeetingTable> meetingTablePage = meetingMapper.selectPage(page, lambdaQueryWrapper);
        List<MeetingTable> records = meetingTablePage.getRecords();
        List<VideoVO> videoList = new ArrayList<>();
        for (MeetingTable m : records){
            VideoVO videoVO = new VideoVO();
            BeanUtils.copyProperties(m,videoVO);
            videoList.add(videoVO);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total",meetingTablePage.getTotal());
        map.put("list", videoList);
        return RestResult.success().data(map);
    }

    @Override
    public List<MeetingTable> findMeetingsByTeamId(Long teamId, TeamMeetingDTO teamMeetingDTO) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(MeetingTable::getTeam_id,teamId);

        if(teamMeetingDTO.getStart() != null && teamMeetingDTO.getEnd() != null){
            lambdaQueryWrapper.between(MeetingTable::getVideo_create_time,teamMeetingDTO.getStart(),teamMeetingDTO.getEnd());
        }

        lambdaQueryWrapper.orderByDesc(MeetingTable::getVideo_create_time);
        List<MeetingTable> meetingTableList = meetingMapper.selectList(lambdaQueryWrapper);
        return meetingTableList;
    }

    @Override
    public List<MeetingTable> getLatestFiveMeetingByTeam(Long teamId, String date) {
        LambdaQueryWrapper<MeetingTable> latestFiveMeetingQueryWrapper = new LambdaQueryWrapper<>();
        latestFiveMeetingQueryWrapper
                .eq(MeetingTable::getTeam_id, teamId);
        //若保留此过滤条件，则team下无满足调节的meeting时，会导致分页数据对不上
                //.eq(MeetingTable::getIs_handle, 1);
        // 根据date参数计算起始时间
        LocalDateTime startTime = LocalDateTime.now(); // 默认现在时间，适用于"AllDate"
        switch (date) {
            case "LastWeek":
                startTime = LocalDateTime.now().minusWeeks(1);
                break;
            case "LastMonth":
                startTime = LocalDateTime.now().minusMonths(1);
                break;
            case "Last3Months":
                startTime = LocalDateTime.now().minusMonths(3);
                break;
            case "Last6Months":
                startTime = LocalDateTime.now().minusMonths(6);
                break;
            case "Last12Months":
                startTime = LocalDateTime.now().minusMonths(12);
                break;
            case "AllDates":
                // 对于"AllDate"，不需要设置startTime
                startTime = null;
                break;
        }

        // 如果startTime不为null，添加时间过滤条件
        if (startTime != null) {
            long startTimeStamp = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            latestFiveMeetingQueryWrapper.ge(MeetingTable::getVideo_create_time, startTimeStamp);
        }
        // 添加排序和限制条件
        latestFiveMeetingQueryWrapper
                .orderByDesc(MeetingTable::getVideo_create_time)
                .last("LIMIT 5");
        return meetingMapper.selectList(latestFiveMeetingQueryWrapper);
    }

    @Override
    public List<MeetingTable> getLatestNMeetingByTeam(Long teamId, String date, int count) {
        LambdaQueryWrapper<MeetingTable> latestFiveMeetingQueryWrapper = new LambdaQueryWrapper<>();
        latestFiveMeetingQueryWrapper
                .eq(MeetingTable::getTeam_id, teamId);
        //若保留此过滤条件，则team下无满足调节的meeting时，会导致分页数据对不上
        //.eq(MeetingTable::getIs_handle, 1);
        // 根据date参数计算起始时间
        LocalDateTime startTime = LocalDateTime.now(); // 默认现在时间，适用于"AllDate"
        switch (date) {
            case "LastWeek":
                startTime = LocalDateTime.now().minusWeeks(1);
                break;
            case "LastMonth":
                startTime = LocalDateTime.now().minusMonths(1);
                break;
            case "Last3Months":
                startTime = LocalDateTime.now().minusMonths(3);
                break;
            case "Last6Months":
                startTime = LocalDateTime.now().minusMonths(6);
                break;
            case "Last12Months":
                startTime = LocalDateTime.now().minusMonths(12);
                break;
            case "AllDates":
                // 对于"AllDate"，不需要设置startTime
                startTime = null;
                break;
        }

        // 如果startTime不为null，添加时间过滤条件
        if (startTime != null) {
            long startTimeStamp = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            latestFiveMeetingQueryWrapper.ge(MeetingTable::getVideo_create_time, startTimeStamp);
        }
        // 添加排序和限制条件
        latestFiveMeetingQueryWrapper
                .orderByDesc(MeetingTable::getVideo_create_time)
                .last("LIMIT " + count);
        return meetingMapper.selectList(latestFiveMeetingQueryWrapper);
    }

    @Override
    public RestResult modLatestMeetingTypeByTeamId(Long teamId, String meetingType, int modCount) {
        List<MeetingTable> latestNMeetingByTeam = getLatestNMeetingByTeam(teamId, modCount);
        List<Long> meetingIds = new ArrayList<>();
        for (MeetingTable meetingTable : latestNMeetingByTeam) {
            meetingTable.setMeeting_type(meetingType);
            meetingIds.add(meetingTable.getMeeting_id());
        }
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(MeetingTable::getMeeting_id, meetingIds)
                    .set(MeetingTable::getMeeting_type, meetingType);
        meetingMapper.update(null, updateWrapper);

        return RestResult.success().data(latestNMeetingByTeam);
    }

    @Override
    public List<MeetingTable> findLatestMeetingTypeByTeamId(Long teamId, String meetingType) {
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, 1);

        // 如果会议类型不为空，则在查询时加入该条件
        if (meetingType != null && !meetingType.isEmpty()) {
            queryWrapper.eq(MeetingTable::getMeeting_type, meetingType);
        }

        // 在所有条件设置完毕后添加orderBy和LIMIT
        queryWrapper.orderByDesc(MeetingTable::getVideo_create_time)
                .last("LIMIT 5");

        // 执行查询
        List<MeetingTable> meetingTableList = meetingMapper.selectList(queryWrapper);
        return meetingTableList;
    }

    public List<MeetingTable> findMeetingInfoByTeamId(Long teamId, String meetingType) {
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, 1);
        if (meetingType != null && !meetingType.isEmpty()) {
            queryWrapper.eq(MeetingTable::getMeeting_type, meetingType);
        }
        queryWrapper
                .orderByDesc(MeetingTable::getVideo_create_time);
        List<MeetingTable> meetingTableList = meetingMapper.selectList(queryWrapper);
        return meetingTableList;
    }

    @Override
    public Page<Long> selectDistinctTeamIds(Integer pageNum, Integer pageSize) {
        Page<Long> page = new Page<>(pageNum, pageSize);
        return meetingMapper.selectDistinctTeamIds(page);
    }

    @Override
    public void handleDetection(Long meetingId, String name, int type) throws IOException {
        log.info("[MeetingServiceImpl][handleDetection] meetingId :{}, name :{}, type :{}", meetingId, name, type);
        //已跳过标题行
        List<String> anchorResultData = amazonUploadService.readCsvLine(CsvConstants.ANCHOR_RESULT, String.valueOf(meetingId));
        //1.处理Anchor Result
        Map<Long,List<UserWithCenter>> userLocations = new HashMap<>();
        List<Long> timeLine = new ArrayList<>();
        for(String data : anchorResultData) {
            String[] info = data.split(",", 2);
            long time = Long.parseLong(info[0]);
            List<UserWithCenter> userWithCenters = new ArrayList<>();
            Gson gson = new Gson();
            String json = info[1].replace("\"", "");
            Map<String,int[]> coordinatesMap = gson.fromJson(json, new TypeToken<Map<String, int[]>>() {}.getType());
            for (Map.Entry<String, int[]> entry : coordinatesMap.entrySet()){
                String username = entry.getKey();
                username = username.replaceAll("_",  "0").replaceAll( "\"", "");
                int[] userLoc = entry.getValue();
                if(userLoc.length == 4) {
                    userWithCenters.add(new UserWithCenter(username, userLoc[0], userLoc[1], userLoc[2], userLoc[3]));
                }
            }
            userLocations.put(time, userWithCenters);
            timeLine.add(time);
        }

        List<String> detections = amazonUploadService.readCsvLine(name, String.valueOf(meetingId));
        List<DetectionCV> detectionCVList = new ArrayList<>(detections.size());
        int timeLineInd = 0;
        for (String data : detections) {
            String[] info = data.split(",");
            Double start = Double.parseDouble(info[2]);
            Double end = Double.parseDouble(info[3]);
            //x1=info[1] y1=info[2] x2 =info[3] y2 = info[4]
            //TODO 看是weight还是height
            String keyword = String.valueOf(info[1]);
            int x1 = Double.valueOf(info[4]).intValue();
            int y1 = Double.valueOf(info[5]).intValue();
            int x2 = Double.valueOf(info[6]).intValue();
            int y2 = Double.valueOf(info[7]).intValue();
            UserWithCenter u = new UserWithCenter(start, x1, y1, x2, y2);
            while (timeLineInd + 1 < timeLine.size() && timeLine.get(timeLineInd + 1) < start * 1000) {
                timeLineInd++;
            }
            Long nowTime = timeLine.get(timeLineInd);
            List<UserWithCenter> userWithCenters = userLocations.get(nowTime);
            UserWithCenter nearestUser = findNearestUser(userWithCenters, u.getCenterX(), u.getCenterY());
            //TODO:
            DetectionCV detectionCV = new DetectionCV(meetingId, nearestUser.getUserName(), start, end, type, keyword);
            if (detectionCV != null) {
                detectionCVList.add(detectionCV);
            }
        }
        LambdaQueryWrapper<DetectionCV> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DetectionCV::getMeetingId, meetingId)
                        .eq(DetectionCV::getTypes, type);
        detectionCVService.remove(queryWrapper);
        detectionCVService.addDetectionOneByOne(detectionCVList);
    }

    @Override
    public void handlePostureDetection(Long meetingId, String name, int type) throws IOException {
        log.info("[MeetingServiceImpl][handlePostureDetection] meetingId :{}, name :{}, type :{}", meetingId, name, type);
        List<String> detections = amazonUploadService.readCsvLine(name, String.valueOf(meetingId));
        List<DetectionCV> detectionCVList = new ArrayList<>(detections.size());
        for (String data : detections) {
            String[] info = data.split(",");
            String userName = info[0].replace("_", "");
            int number = Integer.parseInt(userName.substring(7)) - 1;
            String newUserName = String.format("speaker%02d", number);
            Double start = Double.parseDouble(info[2]);
            Double end = Double.parseDouble(info[3]);
            //x1=info[1] y1=info[2] x2 =info[3] y2 = info[4]
            //TODO 看是weight还是height
            String keyword = String.valueOf(info[1]);

            //TODO:
            DetectionCV detectionCV = new DetectionCV(meetingId, newUserName, start, end, type, keyword);
            if (detectionCV != null) {
                detectionCVList.add(detectionCV);
            }
        }
        LambdaQueryWrapper<DetectionCV> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DetectionCV::getMeetingId, meetingId)
                .eq(DetectionCV::getTypes, type);
        detectionCVService.remove(queryWrapper);
        detectionCVService.addDetectionOneByOne(detectionCVList);

    }

    @Override
    public void handleBrainScore(Long meetingId) {
        log.info("[MeetingServiceImpl][handleBrainScore] meetingId :{}", meetingId);
        List<String[]> dataR = amazonUploadService.readBlinkData(CsvConstants.BLINK_RESULT, meetingId);
        List<String[]> syncBrain = CsvUtil.get_sync_brain(WINDOW_BRAIN_SCORE_LENGTH, dataR);
        Double brainScores = CsvUtil.getBrainScores(syncBrain);
        if (brainScores < 0 || brainScores > 100) {
            brainScores = 50.0;
        }
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(MeetingTable::getMeeting_id, meetingId)
                        .set(MeetingTable::getBrain_score, brainScores);
        meetingMapper.update(null, lambdaUpdateWrapper);
    }

    @Override
    public void removeMeetingDataByMeetingId(Long meetingId) {
        removeCvDataByMeetingId(meetingId);
        removeNlpDataByMeetingId(meetingId);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingId);
        analysisMapper.deleteByMap(deleteMap);
        videoAnalysisMapper.deleteByMap(deleteMap);
        meetingMapper.deleteByMap(deleteMap);
    }

    @Override
    public RestResult findTeamMeetingProgress(Long teamID, int progress) {
        return null;
    }

    @Override
    public HashMap<String, Double> getAverageLatestFiveScore(Long teamId) {
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<MeetingTable>()
                .eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, DATA_HANDLE)
                .orderByDesc(MeetingTable::getVideo_create_time).last("LIMIT 5");
        List<MeetingTable> latestFiveMeeting = meetingMapper.selectList(queryWrapper);
        double total = latestFiveMeeting.stream()
                .map(MeetingTable::getTotal_score)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(60.0);


        //1.performance
        //计算brain
        double brain = latestFiveMeeting.stream().filter(meetingTable -> meetingTable.getBrain_score() != null)
                .mapToDouble(MeetingTable::getBrain_score)
                .average()
                .orElse(60.0);
        //计算body
        double body = latestFiveMeeting.stream().filter(meetingTable -> meetingTable.getBody_score() != null)
                .mapToDouble(MeetingTable::getBody_score)
                .average()
                .orElse(60.0);
        //计算behaviour
        double behavior = latestFiveMeeting.stream().filter(meetingTable -> meetingTable.getBehavior_score() != null)
                .mapToDouble(MeetingTable::getBehavior_score)
                .average()
                .orElse(60.0);
        HashMap<String, Double> scoreMap = new HashMap<>();
        scoreMap.put("synchronyScore", total);
        scoreMap.put("brain", brain);
        scoreMap.put("body", body);
        scoreMap.put("behavior", behavior);
        return scoreMap;
    }

    @Override
    public MeetingSummaryVO getGlobalTeamMetricsInfo(Long teamId) {
        List<MeetingTable> meetingTables = getLatestFiveMeetingByTeam(teamId, "AllDates");
        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
        if(meetingTables.size() == 0){
            return meetingSummaryVO;
        }

        List<MeetingTable> meetingTableLast = new ArrayList<>();
        meetingTableLast.add(meetingTables.get(0));
        MeetingSummaryVO meetingSummaryLast = computeTeamMeetingSummary(meetingTableLast);

        return meetingSummaryLast;
    }

    @Override
    public void handleMatch(Long meetingId) throws IOException {
        log.info("[MeetingServiceImpl][handleMatch] meetingId :{}", meetingId);
        //已跳过标题行
        List<String> anchorResultData = amazonUploadService.readCsvLine(CsvConstants.ANCHOR_RESULT, String.valueOf(meetingId));
        //1.处理Anchor Result
        Map<Long,List<UserWithCenter>> userLocations = new HashMap<>();
        List<Long> timeLine = new ArrayList<>();
        for (String data : anchorResultData) {
            String[] info = data.split(",", 2);
            long time = Long.parseLong(info[0]);
            List<UserWithCenter> userWithCenters = new ArrayList<>();
            Gson gson = new Gson();
            String json = info[1].replace("\"", "");
            Map<String,int[]> coordinatesMap = gson.fromJson(json, new TypeToken<Map<String, int[]>>() {}.getType());
            for (Map.Entry<String, int[]> entry : coordinatesMap.entrySet()){
                String username = entry.getKey();
                username = username.replaceAll("_",  "0").replaceAll( "\"", "");
                int[] userLoc = entry.getValue();
                if(userLoc.length == 4) {
                    userWithCenters.add(new UserWithCenter(username, userLoc[0], userLoc[1], userLoc[2], userLoc[3]));
                }
            }
            userLocations.put(time, userWithCenters);
            timeLine.add(time);
        }

        List<String> activeSpeaker = amazonUploadService.readCsvLine(CsvConstants.ACTIVE_SPEAKER_CSV, String.valueOf(meetingId));
        List<UserWithCenter> userWithCenterList = new ArrayList<>(activeSpeaker.size());
//        Long time = 0L;
        int timeLineInd = 0;
        for (String data : activeSpeaker) {
            String[] info = data.split(",");
            Double time = Double.parseDouble(info[0]);
            //x1=info[1] y1=info[2] x2 =info[3] y2 = info[4]
            int x1 = Integer.parseInt(info[1]);
            int y1 = Integer.parseInt(info[2]);
            int x2 = Integer.parseInt(info[3]);
            int y2 = Integer.parseInt(info[4]);
            UserWithCenter u = new UserWithCenter(time, x1, y1, x2, y2);
            while (timeLineInd + 1 < timeLine.size() && timeLine.get(timeLineInd + 1) < time) {
                timeLineInd++;
            }
            Long nowTime = timeLine.get(timeLineInd);
            List<UserWithCenter> userWithCenters = userLocations.get(nowTime);
            UserWithCenter nearestUser = findNearestUser(userWithCenters, u.getCenterX(), u.getCenterY());
            u.setUserName(nearestUser.getUserName());
            userWithCenterList.add(u);
//            time += 40;
        }
        int ind = 0;
        List<String[]> nlpData = amazonUploadService.readNlpLine(CsvConstants.NLP_FILE_NAME, String.valueOf(meetingId));
        System.out.println(userWithCenterList.get(userWithCenterList.size() - 1).getTime());
        int count = 0;
        for(String[] data : nlpData){
//            Double start = Double.valueOf(data[1]) * 1000;
            Double start = Double.valueOf(data[1]);
            while (ind + 1 < userWithCenterList.size() && userWithCenterList.get(ind).getTime() < start) {
                ind++;
            }
            if (userWithCenterList.get(ind).getTime() < start) {
                System.out.println(count++);
                System.out.println(start);
                data[0] = data[0].replace("_", "");
            } else {
                data[0] = userWithCenterList.get(ind).getUserName().replace("user", "speaker");
            }
        }
        amazonUploadService.saveNlpData(nlpData, meetingId);
    }

    @Override
    public void handleNewMatch(Long meetingId) throws IOException {
        //TODO: speaker匹配user or user匹配speaker
        log.info("[MeetingServiceImpl][handleNewMatch] meetingId :{}", meetingId);
        //已跳过标题行
        List<String> anchorResultData = amazonUploadService.readCsvLine(CsvConstants.ANCHOR_RESULT, String.valueOf(meetingId));
        //1.处理Anchor Result
        Map<Long, List<UserWithCenter>> userLocations = new HashMap<>();
        List<Long> timeLine = new ArrayList<>();
        for (String data : anchorResultData) {
            String[] info = data.split(",", 2);
            long time = Long.parseLong(info[0]);
            List<UserWithCenter> userWithCenters = new ArrayList<>();
            Gson gson = new Gson();
            String json = info[1].replace("\"", "");
            Map<String, int[]> coordinatesMap = gson.fromJson(json, new TypeToken<Map<String, int[]>>() {}.getType());
            for (Map.Entry<String, int[]> entry : coordinatesMap.entrySet()){
                String username = entry.getKey();
                username = username.replaceAll("_",  "0").replaceAll( "\"", "");
                int[] userLoc = entry.getValue();
                if(userLoc.length == 4) {
                    userWithCenters.add(new UserWithCenter(username, userLoc[0], userLoc[1], userLoc[2], userLoc[3]));
                }
            }
            if (userWithCenters.size() > 0) {
                userLocations.put(time, userWithCenters);
                timeLine.add(time);
            }

        }

        List<String> activeSpeaker = amazonUploadService.readCsvLine(CsvConstants.ACTIVE_SPEAKER_CSV, String.valueOf(meetingId));
//        HashMap<String, UserWithArea> map = new HashMap<>();
//        for (String data : activeSpeaker) {
//            String[] info = data.split(",");
//            String speaker = info[0].replace("_", "");
//            Double startTime = Double.parseDouble(info[1]);
//            Double endTime = Double.parseDouble(info[2]);
//            int x1 = Integer.parseInt(info[3]);
//            int y1 = Integer.parseInt(info[4]);
//            int x2 = Integer.parseInt(info[5]);
//            int y2 = Integer.parseInt(info[6]);
//            if (map.containsKey(speaker)) {
//                UserWithArea userWithArea = map.get(speaker);
//                if (userWithArea.getArea() < (x2 - x1) * (y2 - y1)) {
//                    map.put(speaker, new UserWithArea(speaker, x1, y1, x2, y2, startTime, endTime));
//                }
//            } else {
//                map.put(speaker, new UserWithArea(speaker, x1, y1, x2, y2, startTime, endTime));
//            }
//        }
//
//        HashMap<String, String> speakerUser = new HashMap<>();
//        for (Map.Entry<String, UserWithArea> entry : map.entrySet()) {
//            String speaker = entry.getKey();
//            UserWithArea userWithArea = entry.getValue();
//            Double speakerTime = userWithArea.getTime() * 1000;
//            Long userTime = 0l;
//            for (Long time : timeLine) {
//                userTime = time;
//                if (time >= speakerTime) {
//                    break;
//                }
//            }
//            List<UserWithCenter> userWithCenterList = userLocations.get(userTime);
//            UserWithCenter nearestUser = findNearestUser(userWithCenterList, userWithArea.getCenterX(), userWithArea.getCenterY());
//            speakerUser.put(speaker, nearestUser.getUserName());
//        }

        HashMap<String, HashMap<String, Integer>> speakerUserMap = new HashMap<>();
        List<UserWithArea> userWithAreaList = new ArrayList<>();
        for (String data : activeSpeaker) {
            String[] info = data.split(",");
            String speaker = info[0].replace("_", "");
            Double startTime = Double.parseDouble(info[1]);
            Double endTime = Double.parseDouble(info[2]);
            int x1 = Integer.parseInt(info[3]);
            int y1 = Integer.parseInt(info[4]);
            int x2 = Integer.parseInt(info[5]);
            int y2 = Integer.parseInt(info[6]);
            userWithAreaList.add(new UserWithArea(speaker, x1, y1, x2, y2, startTime, endTime));
        }

        for (UserWithArea userWithArea : userWithAreaList) {
            String speaker = userWithArea.getSpeaker();
            /*Double speakerTime = userWithArea.getTime() * 1000;
            Long userTime = 0l;
            for (Long time : timeLine) {
                userTime = time;
                if (time >= speakerTime) {
                    break;
                }
            }*/
            Set<Long> timelineList = getTimelineByStartAndEnd(timeLine, userWithArea.getStartTime() * 1000, userWithArea.getEndTime() * 1000);
            //System.out.println("debug");
            for (Long userTime : timelineList) {
                List<UserWithCenter> userWithCenterList = userLocations.get(userTime);
                UserWithCenter nearestUser = findNearestUser(userWithCenterList, userWithArea.getCenterX(), userWithArea.getCenterY());

                if (!intersects(userWithArea, nearestUser)) {
                    continue;
                }
                String userName = nearestUser.getUserName();
                speakerUserMap.computeIfAbsent(speaker, k -> new HashMap<>())
                        .merge(userName, 1, Integer::sum);
            }


        }
        log.info("[MeetingServiceImpl][handleNewMatch] meetingId :{}, speakerUserMap :{}", meetingId, speakerUserMap);
        HashMap<String, String> speakerUser = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Integer>> entry : speakerUserMap.entrySet()) {
            String speaker = entry.getKey();
            HashMap<String, Integer> userMap = entry.getValue();

            // 找到具有最大值的 key
            Optional<Map.Entry<String, Integer>> maxEntry = userMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            // 如果存在最大值，放入 speakerUser 中
            maxEntry.ifPresent(stringIntegerEntry -> speakerUser.put(speaker, stringIntegerEntry.getKey()));
        }
        LambdaQueryWrapper<SpeakerUser> queryWrapper = new LambdaQueryWrapper<SpeakerUser>()
                .eq(SpeakerUser::getMeeting_id, meetingId);
        speakerUserMapper.delete(queryWrapper);
        speakerUser.entrySet()
                .forEach(entry -> {
                    String speaker = entry.getKey();
                    String user = entry.getValue();
                    SpeakerUser entity = new SpeakerUser(meetingId, user, speaker);
                    speakerUserMapper.insert(entity);
                });
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<MeetingTable>()
                .eq(MeetingTable::getMeeting_id, meetingId)
                .set(MeetingTable::getIs_match, 1);
        update(updateWrapper);
    }

    private Set<Long> getTimelineByStartAndEnd(List<Long> time, Double start, Double end) {
        if (time.size() < 2) {
            return new HashSet<>(time);
        }
        HashSet<Long> result = new HashSet<>();
        //第一个是特例
        Long first = time.get(0);
        if (end <= first) {
            return new HashSet<>(Collections.singleton(first));
        }
        if (start <= first) {
            result.add(first);
        }
        Long last = time.get(time.size() - 1);
        if (start >= last) {
            return new HashSet<>(Collections.singleton(last));
        }
        if (end >= last) {
            result.add(last);
        }
        for (int i = 1; i < time.size(); i++) {
            Long pre = time.get(i - 1);
            Long cur = time.get(i);
            if ((pre <= start && cur >= start) || (pre <= end && cur >= end)) {
                result.add(pre);
            }
        }

        return result;
    }

    private boolean intersects(UserWithArea u1, UserWithCenter u2) {
        // 计算两个矩形中心点在x轴和y轴的距离
        double xDistance = Math.abs(u1.getCenterX() - u2.getCenterX());
        double yDistance = Math.abs(u1.getCenterY() - u2.getCenterY());

        // 计算两个矩形在x轴和y轴的投影长度之和的一半
        double totalWidth = (u1.getWidth() + u2.getWidth()) / 2.0;
        double totalHeight = (u1.getHeight() + u2.getHeight()) / 2.0;

        // 判断两个矩形是否相交
        return xDistance < totalWidth && yDistance < totalHeight;
    }
    @Override
    public MeetingTable getByMeetingId(Long meetingID) {
        LambdaQueryWrapper<MeetingTable> queryByMeetingId = new LambdaQueryWrapper<MeetingTable>()
                .eq(MeetingTable::getMeeting_id, meetingID);
        return getOne(queryByMeetingId);
    }

    @Override
    public List<MeetingTable> getMeetingByTeamId(Long teamId) {
        LambdaQueryWrapper<MeetingTable> queryByMeetingId = new LambdaQueryWrapper<MeetingTable>();
        queryByMeetingId.eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, 1)
                .orderByAsc(MeetingTable::getVideo_create_time);
        List<MeetingTable> meetingTables = list(queryByMeetingId);
        return meetingTables;
    }

    @Override
    public List<MeetingTable> getAllMeetingByTeamId(Long teamId) {
        LambdaQueryWrapper<MeetingTable> queryByMeetingId = new LambdaQueryWrapper<MeetingTable>();
        queryByMeetingId.eq(MeetingTable::getTeam_id, teamId)
                .orderByAsc(MeetingTable::getVideo_create_time);
        List<MeetingTable> meetingTables = list(queryByMeetingId);
        return meetingTables;
    }

    private UserWithCenter findNearestUser(List<UserWithCenter> userWithCenters, int targetX, int targetY) {
        UserWithCenter nearestUser = null;
        double minDistance = Double.MAX_VALUE;
        for (UserWithCenter userWithCenter : userWithCenters){
            int x = userWithCenter.getCenterX();
            int y = userWithCenter.getCenterY();
            double distance = Math.sqrt(Math.pow((x- targetX), 2) + Math.pow((y - targetY), 2));
            if (distance < minDistance){
                minDistance = distance;
                nearestUser = userWithCenter;
            }
        }
        return nearestUser;
    }

    private List<MeetingTable> getLatestNMeetingByTeam(Long teamId, int modCount) {
        LambdaQueryWrapper<MeetingTable> latestFiveMeetingQueryWrapper = new LambdaQueryWrapper<>();
        latestFiveMeetingQueryWrapper
                .eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, 1)
                .orderByDesc(MeetingTable::getVideo_create_time)
                .last("LIMIT " + modCount);
        return meetingMapper.selectList(latestFiveMeetingQueryWrapper);
    }

    @Override
    public RestResult getEMailNotSend() {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeetingTable::getIs_handle,DATA_HANDLE).eq(MeetingTable::getEmail_send,EMAIL_NOT_SEND);
        List<MeetingTable> meetingTables = meetingMapper.selectList(lambdaQueryWrapper);
        List<EmailSendVO> emailSendVOS = new ArrayList<>();
        for (MeetingTable m : meetingTables){
            EmailSendVO emailSendVO = new EmailSendVO();
            BeanUtils.copyProperties(m,emailSendVO);
            emailSendVOS.add(emailSendVO);
        }
        return RestResult.success().data(emailSendVOS);
    }

    @Override
    public RestResult updateEmailSend(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(MeetingTable::getMeeting_id,meetingID)
                .set(MeetingTable::getEmail_send,EMAIL_SEND);
        meetingMapper.update(null,lambdaUpdateWrapper);
        return RestResult.success();
    }

    @Override
    public boolean checkMatch(Long meeting_id) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeetingTable::getMeeting_id,meeting_id);
        MeetingTable meetingTables = meetingMapper.selectOne(lambdaQueryWrapper);
        return MeetingConstants.USER_SPEAKER_MATCH.equals(meetingTables.getIs_match());
    }

    @Override
    public RestResult updateMatch(Long meeting_id) {
        UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("is_match",MeetingConstants.USER_SPEAKER_MATCH);
        updateWrapper.eq("meeting_id",meeting_id);
        meetingMapper.update(null,updateWrapper);
        return RestResult.success().data("match success!");
    }

    @Override
    public RestResult deleteMatch(Long meetingID) {
        UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("is_match",MeetingConstants.USER_SPEAKER_NOT_MATCH);
        updateWrapper.eq("meeting_id",meetingID);
        meetingMapper.update(null,updateWrapper);
        return RestResult.success().data("delete success!");
    }

    /**
     * 更新会议类型的具体逻辑实现。
     *
     * @param meetingID 会议的ID。
     * @param meetingType 会议类型。
     * @return RestResult 包含更新操作的结果，如果成功，返回更新后的会议记录。
     */
    @Override
    public RestResult<Void> updateMeetingType(Long meetingID, String meetingType) {
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MeetingTable::getMeeting_id, meetingID)
                .set(MeetingTable::getMeeting_type, meetingType);
        meetingMapper.update(null, updateWrapper);
        return RestResult.success().message("update meeting type success");
    }

    /**
     * 更新指定会议的持续时间。
     *
     * @param meetingID 会议ID。
     * @param time_ms 会议持续时间，单位为毫秒。
     */
    @Override
    public void updateDuration(Long meetingID, Double time_ms) {
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<MeetingTable>()
                .eq(MeetingTable::getMeeting_id, meetingID)
                .set(MeetingTable::getDuration, time_ms);
        meetingMapper.update(null,updateWrapper);
    }

    @Override
    public void updateNlpFile(Long meetingID) {
        UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("nlp_file", 1);
        updateWrapper.eq("meeting_id",meetingID);
        meetingMapper.update(null,updateWrapper);
    }

    @Override
    public RestResult removeCvDataByMeetingId(Long meetingID) {
//        UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.set("cv_handle",0);
//        updateWrapper.set("is_handle",0);
//        updateWrapper.eq("meeting_id",meetingID);
//        meetingMapper.update(null, updateWrapper);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);
        emojiMapper.deleteByMap(deleteMap);
        cvUserMapper.deleteByMap(deleteMap);
        aResultMapper.deleteByMap(deleteMap);
        vResultMapper.deleteByMap(deleteMap);
        rResultMapper.deleteByMap(deleteMap);
        individualScoreMapper.deleteByMap(deleteMap);
        individualSyncMapper.deleteByMap(deleteMap);
        aSyncMapper.deleteByMap(deleteMap);
        vSyncMapper.deleteByMap(deleteMap);
        rSyncMapper.deleteByMap(deleteMap);
        individualAMapper.deleteByMap(deleteMap);
        individualVMapper.deleteByMap(deleteMap);
        individualRMapper.deleteByMap(deleteMap);
        aveSyncMapper.deleteByMap(deleteMap);
        heatmapMapper.deleteByMap(deleteMap);
        sectionMapper.deleteByMap(deleteMap);
        speakerUserMapper.deleteByMap(deleteMap);
        userDistanceMapper.deleteByMap(deleteMap);
        posAndNegRateMapper.deleteByMap(deleteMap);

        detectionCVMapper.deleteByMap(deleteMap);
        detectionNLPMapper.deleteByMap(deleteMap);
        universeGroupEmojiMapper.deleteByMap(deleteMap);
//        highlightStatementMapper.deleteByMap(deleteMap);
        userContributionMapper.deleteByMap(deleteMap);
        userAvatarMapper.deleteByMap(deleteMap);


        return RestResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult removeNlpDataByMeetingId(Long meetingID) {
//        UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.set("nlp_handle",0);
//        updateWrapper.set("is_match",0);
//        updateWrapper.set("is_handle",0);
//        updateWrapper.set("synchrony_moment_handle",0);
//        updateWrapper.eq("meeting_id", meetingID);
//        meetingMapper.update(null,updateWrapper);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);
        nlpMapper.deleteByMap(deleteMap);
        nlpWordCountMapper.deleteByMap(deleteMap);
        //TODO: 删除？
        summaryMapper.deleteByMap(deleteMap);
        pieSpeakerMapper.deleteByMap(deleteMap);
        pieEmotionMapper.deleteByMap(deleteMap);
        pieActMapper.deleteByMap(deleteMap);
        barEmotionMapper.deleteByMap(deleteMap);
        radarMapper.deleteByMap(deleteMap);
        speakerMapper.deleteByMap(deleteMap);
        speakerUserMapper.deleteByMap(deleteMap);
        synchronyMomentMapper.deleteByMap(deleteMap);
        nlpSummaryMapper.deleteByMap(deleteMap);

        return RestResult.success();
    }

    @Override
    public RestResult searchMeeting(SearchDTO searchDTO) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(searchDTO.getMeetingName() != null && !searchDTO.getMeetingName().isEmpty()){
            lambdaQueryWrapper.like(MeetingTable::getMeeting_name,searchDTO.getMeetingName());
        }

        if(searchDTO.getMeetingType() != null && !searchDTO.getMeetingType().isEmpty()){
            lambdaQueryWrapper.eq(MeetingTable::getMeeting_type,searchDTO.getMeetingType());
        }

        if(searchDTO.getStart() != null && searchDTO.getEnd() != null){
            lambdaQueryWrapper.between(MeetingTable::getVideo_create_time,searchDTO.getStart(),searchDTO.getEnd());
        }

        lambdaQueryWrapper.orderByDesc(MeetingTable::getVideo_create_time);
        List<MeetingTable> meetingTables = meetingMapper.selectList(lambdaQueryWrapper);
        return RestResult.success().data(meetingTables);
    }

    @Override
    public TeamSyncVO findTeamSync(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        MeetingTable meetingTable = meetingMapper.selectOne(lambdaUpdateWrapper);
        TeamSyncVO teamSyncVO = new TeamSyncVO();
        BeanUtils.copyProperties(meetingTable,teamSyncVO);
        return teamSyncVO;
    }

    @Override
    public void updateUserMerge(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> updateMerge = new LambdaUpdateWrapper<>();
        updateMerge.eq(MeetingTable::getMeeting_id,meetingID).set(MeetingTable::getIs_merge, MeetingConstants.USER_MERGE);
        meetingMapper.update(null,updateMerge);
    }

    @Override
    public boolean checkUserMerge(Long meetingID) {
        LambdaUpdateWrapper<MeetingTable> checkUserMergeWrapper = new LambdaUpdateWrapper<>();
        checkUserMergeWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        MeetingTable meetingTable = getOne(checkUserMergeWrapper);
        if(meetingTable != null && meetingTable.getIs_merge() == MeetingConstants.USER_MERGE){
            return true;
        }
        return false;
    }

    @Override
    public void processRadar(Long meetingId) {
        log.info("[MeetingServiceImpl][processRadar] meetingId :{}", meetingId);
        HashMap<String, String> map = new HashMap<>();
        map.put("Nodding", "A");
        map.put("EyeContact", "B");
        map.put("LeanForward", "C");
        map.put("Smile", "D");
        map.put("NotInterrupting", "E");
        map.put("PositiveLanguage", "F");
        map.put("RelaxedFace", "G");
        map.put("RelaxedPosture", "H");
        map.put("UprightPosture", "I");
        map.put("PositiveFace", "J");
        map.put("RaiseHand", "K");
        map.put("FacingScreen", "L");
        map.put("EngageLanguage", "M");


        List<DetectionVO> detectionVOList1 = detectionCVService.queryDataByMeetingIdAndType(meetingId, 0);
        List<DetectionVO> detectionVOList2 = detectionCVService.queryDataByMeetingIdAndType(meetingId, 1);

        //TODO: 先匹配
        List<DetectionVO> detectionVOList3 = detectionNLPService.queryDataByMeetingId(meetingId);
        LambdaQueryWrapper<SpeakerUser> queryWrapper = new LambdaQueryWrapper<SpeakerUser>()
                .eq(SpeakerUser::getMeeting_id, meetingId);
        List<SpeakerUser> speakerUserList = speakerUserMapper.selectList(queryWrapper);
        Map<String, String> userMap = speakerUserList.stream()
                .collect(Collectors.toMap(SpeakerUser::getSpeaker_name, SpeakerUser::getUser_name));
        detectionVOList3 = detectionVOList3.stream().
                filter(detection -> userMap.containsKey(detection.getUsers())) // 只保留map中存在的user键
                .collect(Collectors.toList()); // 收集为新的列表
        for (DetectionVO detectionVO : detectionVOList3) {
            String currentUser = detectionVO.getUsers();
            detectionVO.setUsers(userMap.get(currentUser));
        }

        detectionVOList2 = detectionVOList2.stream().
                filter(detection -> userMap.containsKey(detection.getUsers())) // 只保留map中存在的user键
                .collect(Collectors.toList()); // 收集为新的列表
        for (DetectionVO detectionVO : detectionVOList2) {
            String currentUser = detectionVO.getUsers();
            detectionVO.setUsers(userMap.get(currentUser));
        }

        List<DetectionVO> detectionVOList = Stream.concat(Stream.concat(detectionVOList1.stream(), detectionVOList2.stream()), detectionVOList3.stream())
                .sorted(Comparator.comparingDouble(DetectionVO::getStarts))
                .collect(Collectors.toList());
        HashMap<Integer, HashMap<String, List<DetectionVO>>> detectionsByMinute = classifyDetectionsByMinute(detectionVOList);
        List<DetectionRadar> detectionRadarList = new ArrayList<>();
        for (Map.Entry<Integer, HashMap<String, List<DetectionVO>>> entry : detectionsByMinute.entrySet()) {
            Integer time = entry.getKey();
            for (Map.Entry<String, List<DetectionVO>> entryWithUser : entry.getValue().entrySet()) {
                String user = entryWithUser.getKey();
                List<DetectionVO> detectionVOS = entryWithUser.getValue();
                Map<String, Long> frequencyMap = detectionVOS.stream()
                        .map(detection -> map.get(detection.getKeyword()))
                        .filter(value -> value != null)  // Ensure the keyword is in the map
                        .collect(Collectors.groupingBy(value -> value, Collectors.counting()));
                // Calculate the total sum of all frequencies
                long total = frequencyMap.values().stream().mapToLong(Long::longValue).sum();

                // Create a new map to store the proportions
                Map<String, Double> proportions = new HashMap<>();

                // Calculate the proportion for each key
                frequencyMap.forEach((key, value) -> {
                    double proportion = ((double) value / total) * 100;
                    proportions.put(key, proportion);
                });


                Map<String, Map<String, Double>> substances = calculateAll(frequencyMap);
                Map<String, Double> similarities = identifySubstance(proportions, substances);
                Map.Entry<String, Double> provided = similarities.entrySet().stream()
                        .min(Map.Entry.comparingByValue())
                        .orElseThrow(() -> new RuntimeException("No substances provided"));

                HashMap<String, String> radarMap = new HashMap<>();
                radarMap.put("attr1", "Trust");
                radarMap.put("attr2", "Psychological Safety");
                radarMap.put("attr3", "Enjoyment");
                radarMap.put("attr4", "Engagement");
                radarMap.put("attr5", "Participation");
                String key = radarMap.get(provided.getKey());
                DetectionRadar detectionRadar = new DetectionRadar(meetingId, user, time * 60, time * 60 + 59, frequencyMap.toString(),
                        proportions.toString(), substances.toString(), similarities.toString(), key);
                detectionRadarList.add(detectionRadar);
            }
        }
        HashMap<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id", meetingId);
        detectionRadarService.removeByMap(deleteMap);
        detectionRadarService.saveBatch(detectionRadarList);
    }

    // Calculate the difference in composition ratios between two substances
    private static double calculateSimilarity(Map<String, Double> unknown, Map<String, Double> known) {
        double diff = 0;
        for (String component : known.keySet()) {
            double knownAmount = known.get(component);
            double unknownAmount = unknown.getOrDefault(component, 0.0);
            diff += Math.abs(knownAmount - unknownAmount);
        }
        for (String component : unknown.keySet()) {
            if (!known.containsKey(component)) {
                diff += unknown.get(component);
            }
        }
        return diff;
    }

    // Identify the most similar known substance to an unknown substance
    public static Map<String, Double> identifySubstance(Map<String, Double> unknown, Map<String, Map<String, Double>> substances) {
        Map<String, Double> similarities = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : substances.entrySet()) {
            String name = entry.getKey();
            Map<String, Double> composition = entry.getValue();
            double similarity = calculateSimilarity(unknown, composition);
            similarities.put(name, similarity);
        }
//        return similarities.entrySet().stream()
//                .min(Map.Entry.comparingByValue())
//                .orElseThrow(() -> new RuntimeException("No substances provided"));
        return similarities;
    }

    private Map<String, Double> calculateProportions(Map<String, Long> group) {
        long total = group.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> proportions = new HashMap<>();
        group.forEach((key, value) -> proportions.put(key, (value * 100.0) / total));
        return proportions;
    }

    public Map<String, Map<String, Double>> calculateAll(Map<String, Long> frequencyMap) {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("attr1", Arrays.asList("A", "B", "C", "D", "E", "F"));
        categories.put("attr2", Arrays.asList("A", "B", "C", "E", "G", "H"));
        categories.put("attr3", Arrays.asList("D", "F", "I", "J"));
        categories.put("attr4", Arrays.asList("B", "K", "L", "M"));
        categories.put("attr5", Arrays.asList("K", "E"));

        Map<String, Map<String, Double>> results = new HashMap<>();
//        categories.forEach((categoryKey, categoryValues) -> {
//            Map<String, Long> attr = new HashMap<>();
//            for (String key : categoryValues) {
//                if (frequencyMap.containsKey(key)) {
//                    attr.put(key, frequencyMap.get(key));
//                }
//            }
//            results.put(categoryKey, calculateProportions(attr));
//        });
        HashMap<String, Double> map1 = new HashMap<>();
        map1.put("A", 28.5);
        map1.put("B", 14.3);
        map1.put("C", 14.3);
        map1.put("D", 14.3);
        map1.put("E", 14.3);
        map1.put("F", 14.3);
        HashMap<String, Double> map2 = new HashMap<>();
        map2.put("A", 16.7);
        map2.put("B", 16.7);
        map2.put("C", 16.7);
        map2.put("E", 16.7);
        map2.put("G", 16.6);
        map2.put("H", 16.6);
        HashMap<String, Double> map3 = new HashMap<>();
        map3.put("D", 25.0);
        map3.put("F", 25.0);
        map3.put("I", 25.0);
        map3.put("J", 25.0);
        HashMap<String, Double> map4 = new HashMap<>();
        map4.put("B", 25.0);
        map4.put("K", 25.0);
        map4.put("L", 25.0);
        map4.put("M", 25.0);
        HashMap<String, Double> map5 = new HashMap<>();
        map5.put("K", 50.0);
        map5.put("E", 50.0);
        results.put("attr1", map1);
        results.put("attr2", map2);
        results.put("attr3", map3);
        results.put("attr4", map4);
        results.put("attr5", map5);
        return results;
    }
    public static HashMap<Integer, HashMap<String, List<DetectionVO>>> classifyDetectionsByMinute(List<DetectionVO> detections) {
        HashMap<Integer, HashMap<String, List<DetectionVO>>> map = new HashMap<>();
        for (DetectionVO detection : detections) {
            int startMinute = (int) detection.getStarts() / 60;
            int endMinute = (int) detection.getEnds() / 60;
            for (int minute = startMinute; minute <= endMinute; minute++) {
                HashMap<String, List<DetectionVO>> userMap = map.getOrDefault(minute, new HashMap<>());
                List<DetectionVO> userDetections = userMap.getOrDefault(detection.getUsers(), new ArrayList<>());
                userDetections.add(detection);
                userMap.put(detection.getUsers(), userDetections);
                map.put(minute, userMap);
            }
        }

        // 过滤最后一个时间段，如果不足30秒
        if (!map.isEmpty()) {
            Integer lastMinute = Collections.max(map.keySet());
            HashMap<String, List<DetectionVO>> lastGroup = map.get(lastMinute);
            double minStart = lastMinute * 60.0;
            double maxStart = 0;
            for (List<DetectionVO> list : lastGroup.values()) {
                for (DetectionVO vo : list) {
                    /*if (vo.getStarts() < minStart) {
                        minStart = vo.getStarts();
                    }*/
                    if (vo.getEnds() > maxStart) {
                        maxStart = vo.getEnds();
                    }
                }
            }
            if ((maxStart - minStart) < 30.0) {
                map.remove(lastMinute);
            }
        }
        return map;
    }

    @Override
    public MeetingSummaryVO computeTeamMeetingSummary(List<MeetingTable> meetingTables) {
        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
        radarService.setEngagementAndAgency(meetingTables, meetingSummaryVO);
        computeAndSetAlignment(meetingTables, meetingSummaryVO);
        computeAndSetStress(meetingTables, meetingSummaryVO);
        computeAndSetBurnout(meetingSummaryVO);
        computeAndSetScore(meetingTables, meetingSummaryVO);
        return meetingSummaryVO;
    }

    private void computeAndSetScore(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
        Double totalScore = 0.0d;
        Integer totalCount = 0;
        for (MeetingTable meetingTable : meetingTables) {
            Double score = meetingTable.getTotal_score();
            if(score != null){
                totalScore += score;
                totalCount ++;
            }
        }
        if (totalCount != 0) {
            meetingSummaryVO.setScore(totalScore / totalCount);
        }
    }

    private void computeAndSetAlignment(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
        Double totalScore = 0.0d;
        Integer totalCount = 0;
        for (MeetingTable meetingTable : meetingTables) {
            if(meetingTable.getTotal_score() != null){
                // 归一化到0-1
                double normalizedX = meetingTable.getTotal_score() / 100.0;
                // 调整后的Sigmoid函数参数
                double k = 10;  // 控制曲线的陡度
                double x0 = 0.5; // 控制S形的中心点

                // 计算S型函数的输出
                double alignment = 1.0 / (1.0 + Math.exp(-k * (normalizedX - x0)));

                totalScore += alignment;
                totalCount ++;
            }
        }
        if(totalCount != 0 ){
            meetingSummaryVO.setAlignment(totalScore / totalCount);
        }
    }

    private void computeAndSetBurnout(MeetingSummaryVO meetingSummaryVO) {
        int totalCount = 0;
        Double totalScore = 0.0d;
        Double stress = meetingSummaryVO.getStress();
        Double engagement = meetingSummaryVO.getEngagement();
        if(stress != null && !Double.isNaN(stress)){
            totalCount ++;
            totalScore += stress;
        }
        if(engagement != null && !Double.isNaN(engagement)){
            totalCount ++;
            totalScore += engagement;
        }
        if(totalCount != 0){
            meetingSummaryVO.setBurnout(totalScore / totalCount);
        }
    }

    private void computeAndSetStress(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
        int totalCount = 0;
        Double totalScore = 0.0d;
        for (MeetingTable meetingTable : meetingTables) {
            Double hrv = meetingTable.getHrv();
            if(hrv != null && !Double.isNaN(hrv)){
                totalCount ++;
                totalScore += hrv;
            }
        }
        Double hrvAve = null;
        Double stress = null;
        if(totalCount != 0){
            hrvAve = totalScore / totalCount;
            stress = Math.pow(Math.E, -hrvAve);
        }

        meetingSummaryVO.setStress(stress);
    }
}
