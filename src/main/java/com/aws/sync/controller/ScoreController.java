package com.aws.sync.controller;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.constants.MeetingConstants;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.dto.MeetingDetails;
import com.aws.sync.dto.TeamScoreDTO;
import com.aws.sync.dto.video.VideoUploadDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.PartETagWrapper;
import com.aws.sync.entity.ScoreParameter;
import com.aws.sync.service.AmazonUploadService;
import com.aws.sync.service.MeetingService;
import com.aws.sync.service.ScoreService;
import com.aws.sync.service.SpeakerUserService;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.utils.NlpUtil;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.vo.score.ScoreVO;
import com.aws.sync.vo.score.TeamScoreVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Description 用于处理score相关计算
 * @author madi
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/score")
public class ScoreController {

    /** nlp文件名 */
    private static final String NLP_FILE_NAME = "nlp_result.txt";

    private static final String NLP_NEW_FILE_NAME = "nlp_result.txt";

    /** 会议处理标记 */
    private static final int MEETING_HANDLE = 1;

    /** 诊断日志 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreController.class);

    @Autowired
    SpeakerUserService speakerUserService;

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    MeetingService meetingService;

    @Autowired
    ScoreService scoreService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * Description 处理单个meeting的score
     * @return RestResult 返回score
     */
    @ApiOperation("Handle meeting score")
    @PostMapping("/{meetingID}")
    public RestResult handleScore(@PathVariable("meetingID")Long meetingID,
                                  @RequestParam("coefficientBody")Double coefficientBody,
                                  @RequestParam("coefficientBehaviour")Double coefficientBehaviour,
                                  @RequestParam("coefficientTotal")Double coefficientTotal,
                                  @RequestParam("weightBody")double weightBody,
                                  @RequestParam("weightBehaviour")double weightBehaviour,
                                  @RequestParam("weightNlpTime")double weighNlpTime,
                                  @RequestParam("weightEqualParticipation")double weightEqualParticipation
    ) throws Exception {
        LOGGER.info("[ScoreController][handleScore]: the post method start!");
        //add one need modify removeCv
        List<String> userList = new ArrayList<>();
        HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingID);
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp(NLP_NEW_FILE_NAME, Long.toString(meetingID), userList);

        List<Double> speakers_time = new ArrayList<>();
        Double nlp_time = NlpUtil.get_time(nlp_data, speakers_time, userList) * 1000;
        Double equal_participation = NlpUtil.get_equal_participation(speakers_time, nlp_time, userList);

        String meeting = Long.toString(meetingID);
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
        //处理score
        Score scores = CsvUtil.get_score(
                dataR, dataV, dataA, coefficientBody, coefficientBehaviour,0, coefficientTotal, weightBody,
                weightBehaviour,0.0, weighNlpTime, weightEqualParticipation, nlp_time, equal_participation
        );
        scores.setMeeting_id(meetingID);
        meetingService.updateScore(scores);
        return RestResult.success().data(scores);
    }

    @ApiOperation("Handle team scores")
    @PostMapping("/team/{teamId}")
    public RestResult handleScores(@PathVariable("teamId")Long teamId,
                                   @RequestBody TeamScoreDTO teamScoreDTO
                                   ) throws Exception {
        LOGGER.info("[ScoreController][handleScores]: the post method start!");
        try {
            //取出team下所有处理过的meeting，进行score重新计算
            LambdaQueryWrapper<MeetingTable> queryMeetingsByTeamId = new LambdaQueryWrapper<>();
            queryMeetingsByTeamId.eq(MeetingTable::getTeam_id, teamId).eq(MeetingTable::getIs_handle, MEETING_HANDLE);
            List<MeetingTable> meetingTableList = meetingService.list(queryMeetingsByTeamId);
            HashMap<String, Object> result = new HashMap<>();
            List<ScoreVO> scoreVOList = new ArrayList<>();
            for (int i = 0; i < meetingTableList.size(); i++) {
                Long meetingID = meetingTableList.get(i).getMeeting_id();
                List<String> userList = new ArrayList<>();
                HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingID);
                //nlp data handle
                List<String[]> nlp_data = amazonUploadService.readNlp(NLP_NEW_FILE_NAME, Long.toString(meetingID), userList);

                List<Double> speakers_time = new ArrayList<>();
                Double nlp_time = NlpUtil.get_time(nlp_data, speakers_time, userList) * 1000;
                Double equal_participation = NlpUtil.get_equal_participation(speakers_time, nlp_time, userList);

                String meeting = Long.toString(meetingID);
                List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
                List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
                List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
                //处理score
//                Score scores = CsvUtil.get_score(
//                        dataR, dataV, dataA, coefficientBody, coefficientBehaviour,0, coefficientTotal, weightBody,
//                        weightBehaviour,0.0, weighNlpTime, weightEqualParticipation, nlp_time, equal_participation
//                );
                Score scores = CsvUtil.get_score(
                        dataR, dataV, dataA, teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(),0,
                        teamScoreDTO.getCoefficientTotal(), teamScoreDTO.getWeightBody(),
                        teamScoreDTO.getWeightBehaviour(),0.0, teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation(), nlp_time, equal_participation
                );
                scores.setMeeting_id(meetingID);
                meetingService.updateScore(scores);
                if (i == 0) {
                    HashMap<String,Object> deleteMap = new HashMap<>();
                    deleteMap.put("team_id", teamId);
                    scoreService.removeByMap(deleteMap);
                    ScoreParameter scoreParameter = new ScoreParameter(teamId,  teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(), 0.0, teamScoreDTO.getCoefficientTotal(),
                            0d, teamScoreDTO.getWeightBody(), teamScoreDTO.getWeightBehaviour(), teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation());
                    scoreService.save(scoreParameter);
                }
                scoreVOList.add(new ScoreVO(meetingID, teamId, 0d, scores.getBody_score(), scores.getBehavior_score(), scores.getTotal_score(),
                            scores.getNlp_speaker_time(), scores.getNlp_equal_participation(), teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(), 0.0d,
                        teamScoreDTO.getCoefficientTotal(), 0.0d, teamScoreDTO.getWeightBody(), teamScoreDTO.getWeightBehaviour(), teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation()));
            }
            return RestResult.success().data(scoreVOList);
        } catch (Exception e) {
            LOGGER.error("failed to handle score,the reason is:{}", e.getMessage());
            return RestResult.fail().message(e.getMessage());
        }
    }

    @ApiOperation("Handle team scores")
    @PostMapping("/teams")
    public RestResult handleTeamsScores(@RequestBody TeamScoreDTO teamScoreDTO
    ) {
        LOGGER.info("[ScoreController][handleTeamsScores] teamScoreDTO:{}", teamScoreDTO);
        try {
            List<ScoreVO> scoreVOList = new ArrayList<>();
            for (Long teamId : teamScoreDTO.getTeamIds()) {
                //取出team下所有处理过的meeting，进行score重新计算
                LambdaQueryWrapper<MeetingTable> queryMeetingsByTeamId = new LambdaQueryWrapper<>();
                queryMeetingsByTeamId.eq(MeetingTable::getTeam_id, teamId).eq(MeetingTable::getIs_handle, MEETING_HANDLE);
                List<MeetingTable> meetingTableList = meetingService.list(queryMeetingsByTeamId);

                HashMap<String, Object> result = new HashMap<>();
                for (int i = 0; i < meetingTableList.size(); i++) {
                    Long meetingID = meetingTableList.get(i).getMeeting_id();
                    System.out.println(meetingTableList);
                    List<String> userList = new ArrayList<>();
//                    HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingID);
                    //nlp data handle
                    List<String[]> nlp_data = amazonUploadService.readNlp(NLP_NEW_FILE_NAME, Long.toString(meetingID), userList);

                    List<Double> speakers_time = new ArrayList<>();
                    Double nlp_time = NlpUtil.get_time(nlp_data, speakers_time, userList) * 1000;
                    Double equal_participation = NlpUtil.get_equal_participation(speakers_time, nlp_time, userList);

                    String meeting = Long.toString(meetingID);
                    List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
                    List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
                    List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
                    //处理score
//                Score scores = CsvUtil.get_score(
//                        dataR, dataV, dataA, coefficientBody, coefficientBehaviour,0, coefficientTotal, weightBody,
//                        weightBehaviour,0.0, weighNlpTime, weightEqualParticipation, nlp_time, equal_participation
//                );
                    Score scores = CsvUtil.get_score(
                            dataR, dataV, dataA, teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(),0,
                            teamScoreDTO.getCoefficientTotal(), teamScoreDTO.getWeightBody(),
                            teamScoreDTO.getWeightBehaviour(),0.0, teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation(), nlp_time, equal_participation
                    );
                    scores.setMeeting_id(meetingID);
                    meetingService.updateScore(scores);
                    if (i == 0) {
                        HashMap<String,Object> deleteMap = new HashMap<>();
                        deleteMap.put("team_id", teamId);
                        scoreService.removeByMap(deleteMap);
                        ScoreParameter scoreParameter = new ScoreParameter(teamId,  teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(), teamScoreDTO.getCoefficientBrain(), teamScoreDTO.getCoefficientTotal(),
                                teamScoreDTO.getWeightBrain(), teamScoreDTO.getWeightBody(), teamScoreDTO.getWeightBehaviour(), teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation());
                        scoreService.save(scoreParameter);
                    }
                    scoreVOList.add(new ScoreVO(meetingID, teamId, 0d, scores.getBody_score(), scores.getBehavior_score(), scores.getTotal_score(),
                            scores.getNlp_speaker_time(), scores.getNlp_equal_participation(), teamScoreDTO.getCoefficientBody(), teamScoreDTO.getCoefficientBehaviour(), 0.0d,
                            teamScoreDTO.getCoefficientTotal(), 0.0d, teamScoreDTO.getWeightBody(), teamScoreDTO.getWeightBehaviour(), teamScoreDTO.getWeighNlpTime(), teamScoreDTO.getWeightEqualParticipation()));

                }
        }
            LOGGER.info("[ScoreController][handleTeamsScores] end!");
            return RestResult.success().data(scoreVOList);
        } catch (Exception e) {
            LOGGER.error("failed to handle score,the reason is:{}", e.getMessage());
            return RestResult.fail().message(e.getMessage());
        }
    }
    @ApiOperation("Get team scores")
    @GetMapping
    public RestResult getTeamScores(@RequestParam("teamId")Long teamId) {
        HashMap<String,List<TeamScoreVO>> ans = new HashMap<>();
        //TODO: 接收的是team列表
        List<TeamScoreVO> teamScoreVOList = new ArrayList<>();
        LambdaQueryWrapper<MeetingTable> queryMeetingsByTeamId = new LambdaQueryWrapper<>();
        queryMeetingsByTeamId
                .eq(MeetingTable::getTeam_id, teamId)
                .eq(MeetingTable::getIs_handle, MEETING_HANDLE)
                .orderByDesc(MeetingTable::getVideo_create_time);

        //获取team相关参数
        LambdaQueryWrapper<ScoreParameter> scoreParameterLambdaQueryWrapper = new LambdaQueryWrapper<>();
        scoreParameterLambdaQueryWrapper.eq(ScoreParameter::getTeam_id, teamId);
        List<ScoreParameter> list = scoreService.list(scoreParameterLambdaQueryWrapper);
        ScoreParameter scoreParameter = null;
        if (list.size() > 0) {
            scoreParameter = list.get(0);
        }

        List<MeetingTable> meetingTableList = meetingService.list(queryMeetingsByTeamId);
        for (MeetingTable meetingTable : meetingTableList) {
            TeamScoreVO t = new TeamScoreVO(meetingTable.getMeeting_id(), meetingTable.getTeam_id(),
                    meetingTable.getBrain_score(), meetingTable.getBody_score(), meetingTable.getBehavior_score(),meetingTable.getTotal_score(),
                    meetingTable.getNlp_speaker_time(), meetingTable.getNlp_equal_participation());
            t.setMeetingName(meetingTable.getMeeting_name());
            if (scoreParameter != null) {
                t.setCoefficient_behaviour(scoreParameter.getCoefficient_behaviour());
                t.setCoefficient_body(scoreParameter.getCoefficient_body());
                t.setCoefficient_brain(scoreParameter.getCoefficient_brain());
                t.setCoefficient_total(scoreParameter.getCoefficient_total());
                t.setWeight_behaviour(scoreParameter.getWeight_behaviour());
                t.setWeight_body(scoreParameter.getWeight_body());
                t.setWeight_brain(scoreParameter.getWeight_brain());
                t.setWeight_nlp_speak_time(scoreParameter.getWeight_nlp_speak_time());
                t.setWeight_nlp_equal_participation(scoreParameter.getWeight_nlp_equal_participation());
                t.setVideo_create_time(meetingTable.getVideo_create_time());
            }
            teamScoreVOList.add(t);
        }
        ans.put(teamId.toString(),teamScoreVOList);


        return RestResult.success().data(teamScoreVOList);
    }

    @ApiOperation("Get team scores")
    @GetMapping("/scores")
    public RestResult getTeamsScores(@RequestParam("teamIds") List<Long> teamIds) {
        LOGGER.info("[ScoreController][getTeamsScores] teamIds:{}", teamIds);
        HashMap<String,List<TeamScoreVO>> ans = new HashMap<>();
        //TODO: 接收的是team列表
        List<TeamScoreVO> teamScoreVOList = new ArrayList<>();
        for (Long teamId : teamIds) {
            LambdaQueryWrapper<MeetingTable> queryMeetingsByTeamId = new LambdaQueryWrapper<>();
            queryMeetingsByTeamId
                    .eq(MeetingTable::getTeam_id, teamId)
                    .eq(MeetingTable::getIs_handle, MEETING_HANDLE)
                    .orderByDesc(MeetingTable::getVideo_create_time);

            //获取team相关参数
            LambdaQueryWrapper<ScoreParameter> scoreParameterLambdaQueryWrapper = new LambdaQueryWrapper<>();
            scoreParameterLambdaQueryWrapper.eq(ScoreParameter::getTeam_id, teamId);
            List<ScoreParameter> list = scoreService.list(scoreParameterLambdaQueryWrapper);
            ScoreParameter scoreParameter = null;
            if (list.size() > 0) {
                scoreParameter = list.get(0);
            }

            List<MeetingTable> meetingTableList = meetingService.list(queryMeetingsByTeamId);
            for (MeetingTable meetingTable : meetingTableList) {
                TeamScoreVO t = new TeamScoreVO(meetingTable.getMeeting_id(), meetingTable.getTeam_id(),
                        meetingTable.getBrain_score(), meetingTable.getBody_score(), meetingTable.getBehavior_score(),meetingTable.getTotal_score(),
                        meetingTable.getNlp_speaker_time(), meetingTable.getNlp_equal_participation());
                t.setMeetingName(meetingTable.getMeeting_name());
                if (scoreParameter != null) {
                    t.setCoefficient_behaviour(scoreParameter.getCoefficient_behaviour());
                    t.setCoefficient_body(scoreParameter.getCoefficient_body());
                    t.setCoefficient_brain(scoreParameter.getCoefficient_brain());
                    t.setCoefficient_total(scoreParameter.getCoefficient_total());
                    t.setWeight_behaviour(scoreParameter.getWeight_behaviour());
                    t.setWeight_body(scoreParameter.getWeight_body());
                    t.setWeight_brain(scoreParameter.getWeight_brain());
                    t.setWeight_nlp_speak_time(scoreParameter.getWeight_nlp_speak_time());
                    t.setWeight_nlp_equal_participation(scoreParameter.getWeight_nlp_equal_participation());
                    t.setVideo_create_time(meetingTable.getVideo_create_time());
                }
                teamScoreVOList.add(t);
            }
//            ans.put(teamId.toString(), teamScoreVOList);
        }


        return RestResult.success().data(teamScoreVOList);
    }

    //TODO: 获取team Parameter
    @ApiOperation("Get team score parameter")
    @GetMapping("/parameter/{teamId}")
    public RestResult getTeamScoreParameter(@PathVariable("teamId")Long teamId) {
        LambdaQueryWrapper<ScoreParameter> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ScoreParameter::getTeam_id, teamId);
        List<ScoreParameter> scoreParameters = scoreService.list(lambdaQueryWrapper);
        ScoreParameter s = scoreParameters.size() > 0 ? scoreParameters.get(0) : new ScoreParameter();
        return RestResult.success().data(s);
    }

    @ApiOperation("Recalculate meeting data")
    @PostMapping("/threshold/{meetingId}")
    public RestResult completeUpload(@RequestParam("threshold") Double threshold,
                                     @PathVariable("meetingId")Long meetingId
    ) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeetingTable::getMeeting_id, meetingId);
        MeetingTable meetingTable = meetingService.getOne(lambdaQueryWrapper);
        if (meetingTable == null) {
            return RestResult.fail().message("Meeting does not exist");
        }
        String fileKey = meetingTable.getVideo_url();
        fileKey = "test/video/" +fileKey.substring(fileKey.lastIndexOf('/') + 1);
        redisTemplate.opsForZSet().add("meeting_test","meeting" + meetingId+ ":" + fileKey + ":" + threshold, System.currentTimeMillis());
        LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MeetingTable::getMeeting_id, meetingId)
                .set(MeetingTable::getCv_handle, 0)
                .set(MeetingTable::getNlp_handle, 0)
                .set(MeetingTable::getIs_match, 0)
                .set(MeetingTable::getIs_handle, 0)
                .set(MeetingTable::getThreshold, threshold);
        meetingService.update(updateWrapper);
        return RestResult.success().message("The data is handling...");
    }

    @ApiOperation("Get meeting details")
    @PostMapping("/detail/{meetingId}")
    public RestResult getMeetingDetails(@PathVariable("meetingId")Long meetingId) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeetingTable::getMeeting_id, meetingId);
        MeetingTable meetingTable = meetingService.getOne(lambdaQueryWrapper);
        if (meetingTable == null) {
            return RestResult.fail().message("Meeting does not exist");
        }
        MeetingDetails meetingDetails = new MeetingDetails();
        BeanUtils.copyProperties(meetingTable, meetingDetails);
        return RestResult.success().data(meetingDetails);
    }

//    @ApiOperation("Get team scores")
//    @GetMapping
//    public RestResult getTeamScores(@RequestParam("teamIds")List<Long> teamIds) {
//        HashMap<String,List<TeamScoreVO>> ans = new HashMap<>();
//        //TODO: 接收的是team列表
//        for (int i = 0; i < teamIds.size(); i++) {
//            Long teamId = teamIds.get(i);
//            List<TeamScoreVO> teamScoreVOList = new ArrayList<>();
//            LambdaQueryWrapper<MeetingTable> queryMeetingsByTeamId = new LambdaQueryWrapper<>();
//            queryMeetingsByTeamId.eq(MeetingTable::getTeam_id, teamId).eq(MeetingTable::getIs_handle, MEETING_HANDLE);
//
//            //获取team相关参数
//            LambdaQueryWrapper<ScoreParameter> scoreParameterLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            scoreParameterLambdaQueryWrapper.eq(ScoreParameter::getTeam_id, teamId);
//            List<ScoreParameter> list = scoreService.list(scoreParameterLambdaQueryWrapper);
//            ScoreParameter scoreParameter = null;
//            if (list.size() > 0) {
//                scoreParameter = list.get(0);
//            }
//
//            List<MeetingTable> meetingTableList = meetingService.list(queryMeetingsByTeamId);
//            for (MeetingTable meetingTable : meetingTableList) {
//                TeamScoreVO t = new TeamScoreVO(meetingTable.getMeeting_id(), meetingTable.getTeam_id(),
//                        meetingTable.getBrain_score(), meetingTable.getBody_score(), meetingTable.getBehavior_score(),meetingTable.getTotal_score(),
//                        meetingTable.getNlp_speaker_time(), meetingTable.getNlp_equal_participation());
//                if (scoreParameter != null) {
//                    t.setCoefficient_behaviour(scoreParameter.getCoefficient_behaviour());
//                    t.setCoefficient_body(scoreParameter.getCoefficient_body());
//                    t.setCoefficient_brain(scoreParameter.getCoefficient_brain());
//                    t.setCoefficient_total(scoreParameter.getCoefficient_total());
//                    t.setWeight_behaviour(scoreParameter.getWeight_behaviour());
//                    t.setWeight_body(scoreParameter.getWeight_body());
//                    t.setWeight_brain(scoreParameter.getWeight_brain());
//                    t.setWeight_nlp_speak_time(scoreParameter.getWeight_nlp_speak_time());
//                    t.setWeight_nlp_equal_participation(scoreParameter.getWeight_nlp_equal_participation());
//                }
//                teamScoreVOList.add(t);
//            }
//            ans.put(teamId.toString(),teamScoreVOList);
//        }
//
//        return RestResult.success().data(ans);
//    }

}
