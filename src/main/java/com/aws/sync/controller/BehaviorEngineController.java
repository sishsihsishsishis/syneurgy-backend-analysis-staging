package com.aws.sync.controller;

import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.NlpSummary;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.entity.match.SpeakerUser;
import com.aws.sync.service.*;
import com.aws.sync.vo.BehaviourEngineCountVO;
import com.aws.sync.vo.BehaviourEngineVO;
import com.aws.sync.vo.MeetingDetectionVO;
import com.aws.sync.vo.MeetingRadarVO;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/behaviour")
public class BehaviorEngineController {

    @Autowired
    NlpSummaryService nlpSummaryService;

    @Autowired
    DetectionCVService detectionCVService;

    @Autowired
    DetectionNLPService detectionNLPService;

    @Autowired
    CVUserService cvUserService;

    @Autowired
    SpeakerUserService speakerUserService;

    @Autowired
    DetectionRadarService detectionRadarService;


    @ApiOperation("Get behaviour engine")
    @GetMapping("/{meetingId}")
    public RestResult getMeetingInfoByType(@PathVariable("meetingId") Long meetingId,
                                           @RequestParam(value = "type", required = false)String type,
                                           @RequestParam(value = "subType", required = false)String subType) {
        try {
            LambdaQueryWrapper<NlpSummary> nlpSummaryMapperLambdaQueryWrapper = new LambdaQueryWrapper<>();
            nlpSummaryMapperLambdaQueryWrapper.eq(NlpSummary::getMeeting_id, meetingId);
            if (type != null && !type.isEmpty()) {
                nlpSummaryMapperLambdaQueryWrapper.eq(NlpSummary::getType ,type);
            }
            if (subType != null && !type.isEmpty()) {
                nlpSummaryMapperLambdaQueryWrapper.eq(NlpSummary::getSubtype, subType);
            }
            nlpSummaryMapperLambdaQueryWrapper.orderByAsc(NlpSummary::getStarts);
            List<NlpSummary> list = nlpSummaryService.list(nlpSummaryMapperLambdaQueryWrapper);
            HashMap<String,List<List<Double>>> summaryMap = new HashMap<>();
            for (NlpSummary nlpSummary : list) {
                List<List<Double>> existingOrNewList = summaryMap.getOrDefault(nlpSummary.getSpeakers(), new ArrayList<>());
                existingOrNewList.add(Arrays.asList(nlpSummary.getStarts(), nlpSummary.getEnds()));
                summaryMap.putIfAbsent(nlpSummary.getSpeakers(), existingOrNewList);
            }
            BehaviourEngineVO behaviourEngineVO = new BehaviourEngineVO();
            HashMap<String,List<List<Double>>> hm = new HashMap<>();
            behaviourEngineVO.setInfo(summaryMap);
            behaviourEngineVO.setMeetingId(meetingId);
            behaviourEngineVO.setType(type);
            behaviourEngineVO.setSubType(subType);
            return RestResult.success().data(behaviourEngineVO);
        } catch (SystemException e) {
            return RestResult.fail().message(e.getMessage());
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    @ApiOperation("Get behaviour engine type count")
    @GetMapping("/count/{meetingId}")
    public RestResult<List<BehaviourEngineCountVO>> getTypeCount(@PathVariable("meetingId") Long meetingId) {
        try {
            LambdaQueryWrapper<NlpSummary> nlpSummaryMapperLambdaQueryWrapper = new LambdaQueryWrapper<>();
            nlpSummaryMapperLambdaQueryWrapper.eq(NlpSummary::getMeeting_id, meetingId);
            List<NlpSummary> list = nlpSummaryService.list(nlpSummaryMapperLambdaQueryWrapper);
            List<BehaviourEngineCountVO> resultList = list.stream()
                    .collect(Collectors.groupingBy(item -> Arrays.asList(item.getSpeakers(), item.getType(), item.getSubtype()),
                            Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(entry -> new BehaviourEngineCountVO(meetingId, entry.getKey().get(0), entry.getKey().get(1), entry.getKey().get(2), entry.getValue()))
                    .sorted(Comparator.comparing(BehaviourEngineCountVO::getSpeakers))
                    .collect(Collectors.toList());
            return RestResult.success().data(resultList);
        } catch (SystemException e) {
            return RestResult.fail().message(e.getMessage());
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    @ApiOperation("Get behaviour engine by speaker")
    @GetMapping("/info/{meetingId}")
    public RestResult getMeetingInfoBySpeaker(@PathVariable("meetingId") Long meetingId,
                                           @RequestParam(value = "speaker")String speaker) {
        try {
            LambdaQueryWrapper<NlpSummary> nlpSummaryMapperLambdaQueryWrapper = new LambdaQueryWrapper<>();
            nlpSummaryMapperLambdaQueryWrapper.eq(NlpSummary::getMeeting_id, meetingId)
                    .eq(NlpSummary::getSpeakers, speaker);

            nlpSummaryMapperLambdaQueryWrapper.orderByAsc(NlpSummary::getStarts);
            List<NlpSummary> list = nlpSummaryService.list(nlpSummaryMapperLambdaQueryWrapper);
            HashMap<String, Object> map = new HashMap<>();
            map.put("info", list);
            map.put("count", list.size());
            return RestResult.success().data(map);
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    @ApiOperation("Get detection result")
    @GetMapping("/detection/{userId}")
    public RestResult getDetectionResult(@PathVariable("userId") String userId) {
        List<MeetingDetectionVO> detectionResults = new ArrayList<>();
//        Map<Long, String> meetingTableList = cvUserService.getMeetingAndUserMapByUserId(userId);

        List<CVUser> meetingTableList = cvUserService.getMeetingAndUserMapByUserId(userId);

        for (CVUser entry : meetingTableList) {
            Long meetingId = entry.getMeeting_id();
            MeetingDetectionVO result = new MeetingDetectionVO();
            String username = entry.getUser_name();
            result.setMeetingId(meetingId);
            result.setUserId(userId);
            //result.setSpeaker(entry.getValue().replace("user", "speaker"));
            result.setUsername(username);
            List<DetectionVO> detectionNlpList = detectionNLPService.queryDataByMeetingId(meetingId);
            SpeakerUser speakerUser = speakerUserService.queryDataByMeetingIdAndName(meetingId, username);
            if (speakerUser != null) {
                result.setSpeaker(speakerUser.getSpeaker_name());
                result.addResult("nlp", detectionNlpList);
            } else {
                result.addResult("nlp", null);
            }


            List<DetectionVO> cvEmotion = detectionCVService.queryDataByMeetingIdAndType(meetingId, 0);
            result.addResult("cv_emotion", cvEmotion);

            List<DetectionVO> cvPosture = detectionCVService.queryDataByMeetingIdAndType(meetingId, 1);
            result.addResult("cv_posture", cvPosture);
            detectionResults.add(result);
        }

        return RestResult.success().data(detectionResults);

    }

    @ApiOperation("Get radar result")
    @GetMapping("/radar/{userId}")
    public RestResult getRadarResult(@PathVariable("userId") String userId) {
        List<MeetingRadarVO> radarResult = new ArrayList<>();
        List<CVUser> meetingTableList = cvUserService.getMeetingAndUserMapByUserId(userId);

        for (CVUser entry : meetingTableList) {
            Long meetingId = entry.getMeeting_id();
            MeetingRadarVO result = new MeetingRadarVO();
            String username = entry.getUser_name();
            result.setMeetingId(meetingId);
            result.setUserId(userId);
            result.setUsername(username);
            List<DetectionVO> detectionRadarList = detectionRadarService.queryDataByMeetingId(meetingId);
            List<DetectionVO> collect = detectionRadarList.stream()
                    .filter(detection -> username.equals(detection.getUsers()))
                    .collect(Collectors.toList());
            result.setResults(collect);
            radarResult.add(result);
        }
        return RestResult.success().data(radarResult);
    }

    @ApiOperation("Get meeting radar result")
    @GetMapping("/meeting-radar/{meetingId}")
    public RestResult getMeetingRadarResult(@PathVariable("meetingId") Long meetingId) {
        List<DetectionVO> detectionRadarList = detectionRadarService.queryDataByMeetingId(meetingId);
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", detectionRadarList);
        result.put("meetingId", meetingId);
        return RestResult.success().data(result);
    }
}
