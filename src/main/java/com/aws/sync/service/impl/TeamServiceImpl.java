package com.aws.sync.service.impl;

import com.aws.sync.dto.TimeSearchDTO;
import com.aws.sync.entity.AnalysisInfo;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    MeetingService meetingService;

    @Autowired
    AnalysisService analysisService;

    @Autowired
    CVUserService cvUserService;

    @Override
    public HashMap<String, Long> queryTimeInfo(String teamId, Long timestamp) {
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MeetingTable::getTeam_id, teamId)
                .gt(MeetingTable::getUpload_time, timestamp);
        HashMap<String, Long> timeInfo = new HashMap<>();
        List<MeetingTable> meetingTableList = meetingService.list(queryWrapper);
        long durationSum = meetingTableList.stream()
                .filter(meetingTable -> meetingTable.getDuration() != null)
                .mapToLong(MeetingTable::getDuration)
                .sum();


        long total = 0l;
        for (MeetingTable meetingTable : meetingTableList) {
            LambdaQueryWrapper<AnalysisInfo> analysisInfoQueryWrapper = new LambdaQueryWrapper<>();
            analysisInfoQueryWrapper.eq(AnalysisInfo::getMeetingId, meetingTable.getMeeting_id());
            List<AnalysisInfo> analysisInfoList = analysisService.list(analysisInfoQueryWrapper);
            long rPPG = 0l;
            long speakerDetectionModel = 0l;
            long aSR = 0l;
            for (AnalysisInfo analysisInfo : analysisInfoList) {
                String model = analysisInfo.getModel();
                if ("speaker-detection-model".equals(model)) {
                    speakerDetectionModel += analysisInfo.getEndTime() - analysisInfo.getStartTime();
                } else if ("rPPG".equals(model)) {
                    rPPG += analysisInfo.getEndTime() - analysisInfo.getStartTime();
                } else if ("ASR".equals(model) || "SpeakerDiariazation".equals(model) || "NLPEmotion".equals(model) || "DialogueAct".equals(model)) {
                    aSR += analysisInfo.getEndTime() - analysisInfo.getStartTime();
                }
            }
            long mx = Math.max(rPPG, Math.min(speakerDetectionModel, aSR));
            total += mx;
        }
//        long total = meetingTableList.stream()
//                .flatMap(meetingTable -> {
//                    LambdaQueryWrapper<AnalysisInfo> analysisInfoQueryWrapper = new LambdaQueryWrapper<>();
//                    analysisInfoQueryWrapper.eq(AnalysisInfo::getMeetingId, meetingTable.getMeeting_id());
//                    return analysisService.list(analysisInfoQueryWrapper).stream();
//                })
//                .collect(Collectors.groupingBy(AnalysisInfo::getMeetingId))
//                .values().stream()
//                .mapToLong(analysisInfos -> {
//                    long rPPG = analysisInfos.stream()
//                            .filter(info -> "rPPG".equals(info.getModel()))
//                            .mapToLong(info -> info.getEndTime() - info.getStartTime())
//                            .sum();
//                    long speakerDetectionModel = analysisInfos.stream()
//                            .filter(info -> "speaker-detection-model".equals(info.getModel()))
//                            .mapToLong(info -> info.getEndTime() - info.getStartTime())
//                            .sum();
//                    long aSR = analysisInfos.stream()
//                            .filter(info -> "ASR".equals(info.getModel()) || "SpeakerDiariazation".equals(info.getModel()) || "NLPEmotion".equals(info.getModel()) || "DialogueAct".equals(info.getModel()))
//                            .mapToLong(info -> info.getEndTime() - info.getStartTime())
//                            .sum();
//                    return Math.max(rPPG, Math.min(speakerDetectionModel, aSR));
//                }).sum();


        timeInfo.put("video_duration", durationSum);
        timeInfo.put("analysis_time", total);


        return null;
    }

    @Override
    public Long queryVideoTime(TimeSearchDTO timeSearchDTO) {
        List<MeetingTable> meetingTableList = new ArrayList<>();
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<>();

        if (timeSearchDTO.getTeamID() != null) {
            queryWrapper.eq(MeetingTable::getTeam_id, timeSearchDTO.getTeamID());
            addTimeConditions(queryWrapper, timeSearchDTO);
            meetingTableList = meetingService.list(queryWrapper);
        } else if (timeSearchDTO.getUserID() != null) {
            List<Long> meetingIds = cvUserService.list(
                            new LambdaQueryWrapper<CVUser>()
                                    .eq(CVUser::getUser_id, timeSearchDTO.getUserID())
                    ).stream()
                    .map(CVUser::getMeeting_id)
                    .distinct()  // 确保ID是唯一的，避免重复查询
                    .collect(Collectors.toList());

            if (!meetingIds.isEmpty()) {
                queryWrapper.in(MeetingTable::getMeeting_id, meetingIds);
                addTimeConditions(queryWrapper, timeSearchDTO);
                meetingTableList = meetingService.list(queryWrapper);
            }
        }

        // 计算duration总和
        return meetingTableList.stream()
                .filter(meetingTable -> meetingTable.getDuration() != null)
                .mapToLong(MeetingTable::getDuration)
                .sum();
    }

    private void addTimeConditions(LambdaQueryWrapper<MeetingTable> queryWrapper, TimeSearchDTO timeSearchDTO) {
        if (timeSearchDTO.getEnd() == null) {
            queryWrapper.ge(MeetingTable::getUpload_time, timeSearchDTO.getStart());
        } else {
            queryWrapper.between(MeetingTable::getUpload_time, timeSearchDTO.getStart(), timeSearchDTO.getEnd());
        }
    }
}
