package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.TeamMeetingDTO;
import com.aws.sync.dto.TimeSearchDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.service.*;
import com.aws.sync.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/team")
public class TeamController {
    @Autowired
    MeetingService meetingService;
    
    @Autowired
    RadarService radarService;

    @Autowired
    PieEmotionService pieEmotionService;

    @Autowired
    CVUserService cvUserService;

    @Autowired
    TeamService teamService;

    @ApiOperation("Get team performance")
    @GetMapping("/performance/{teamId}")
    public RestResult getTeamPerformance(@PathVariable("teamId") Long teamId, TeamMeetingDTO teamMeetingDTO){
        List<MeetingTable> meetingsByTeamId = meetingService.findMeetingsByTeamId(teamId, teamMeetingDTO);
        List<TeamPerformanceVO> teamPerformanceVOList = new ArrayList<>(meetingsByTeamId.size());
        for (MeetingTable meetingTable : meetingsByTeamId) {
            TeamPerformanceVO teamPerformanceVO = covertToTeamPerformance(meetingTable);
            teamPerformanceVOList.add(teamPerformanceVO);
        }
        return RestResult.success().data(teamPerformanceVOList);
    }

    @ApiOperation("Get team sentiment")
    @GetMapping("/sentiment/{teamId}")
    public RestResult getTeamSentiment(@PathVariable("teamId") Long teamId, TeamMeetingDTO teamMeetingDTO){
        List<MeetingTable> meetingsByTeamId = meetingService.findMeetingsByTeamId(teamId, teamMeetingDTO);
        List<TeamSentimentVO> teamSentimentVOList = new ArrayList<>(meetingsByTeamId.size());
        for (MeetingTable meetingTable : meetingsByTeamId) {
            List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingTable.getMeeting_id());
            handlePieEmotionRate(emotion);
            TeamSentimentVO t = new TeamSentimentVO();
            t.setMeeting_id(meetingTable.getMeeting_id());
            t.setCreate_time(meetingTable.getVideo_create_time());
            t.setSentiment(emotion);
            t.setMeeting_name(meetingTable.getMeeting_name());
            teamSentimentVOList.add(t);
        }
        return RestResult.success().data(teamSentimentVOList);
    }

    private void handlePieEmotionRate(List<PieEmotionVO> emotion) {
        BigDecimal total = BigDecimal.ZERO;
        total.setScale(3, RoundingMode.HALF_UP);
        if (emotion.size() > 3) {
            return;
        }
        for (int i = 0; i < emotion.size(); i++) {
            if (i != emotion.size() - 1) {
                BigDecimal t = BigDecimal.valueOf(emotion.get(i).getEmotion_time_rate());
                t.setScale(3, RoundingMode.HALF_UP);
                total = total.add(t);
                emotion.get(i).setEmotion_time_rate(t.doubleValue());
            } else {
                emotion.get(i).setEmotion_time_rate(BigDecimal.ONE.subtract(total).doubleValue());
            }
        }
    }

    @ApiOperation("Get team dimension")
    @GetMapping("/dimension/{teamId}")
    public RestResult getTeamDimension(@PathVariable("teamId") Long teamId, TeamMeetingDTO teamMeetingDTO){
        List<MeetingTable> meetingsByTeamId = meetingService.findMeetingsByTeamId(teamId, teamMeetingDTO);
        List<TeamDimensionVO> teamDimensionVOList = new ArrayList<>(meetingsByTeamId.size());
        for (MeetingTable meetingTable : meetingsByTeamId) {
            List<RadarVO> kv = radarService.findKV(meetingTable.getMeeting_id());
            TeamDimensionVO t = new TeamDimensionVO();
            t.setMeeting_id(meetingTable.getMeeting_id());
            t.setCreate_time(meetingTable.getVideo_create_time());
            t.setDimension(kv);
            t.setMeeting_name(meetingTable.getMeeting_name());
            teamDimensionVOList.add(t);
        }
        return RestResult.success().data(teamDimensionVOList);
    }

    @ApiOperation("Get global team synchrony")
    @GetMapping("/global-synchrony/{teamId}")
    public RestResult getGlobalSynchrony(@PathVariable("teamId") Long teamId, TeamMeetingDTO teamMeetingDTO){
        List<MeetingTable> meetingsByTeamId = meetingService.findMeetingsByTeamId(teamId, teamMeetingDTO);
        List<TeamGlobalSynchronyVO> teamGlobalSynchronyVOList = new ArrayList<>(meetingsByTeamId.size());
        for (MeetingTable meetingTable : meetingsByTeamId) {
            teamGlobalSynchronyVOList.add(covertToTeamGlobalSynchrony(meetingTable));
        }
        return RestResult.success().data(teamGlobalSynchronyVOList);
    }

//    @ApiOperation("Get team summary paras")
//    @GetMapping("/team-summary-paras/{teamId}")
//    public RestResult getMeetingSummary(@PathVariable("teamId")Long teamId){
////        List<MeetingTable> meetingTables = meetingService.getLatestFiveMeetingByTeam(teamId);
////        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
////        radarService.setEngagementAndAgency(meetingTables, meetingSummaryVO);
////        computeAndSetAlignment(meetingTables, meetingSummaryVO);
////        computeAndSetStress(meetingTables, meetingSummaryVO);
////        computeAndSetBurnout(meetingSummaryVO);
//        List<MeetingTable> meetingTables = meetingService.getLatestFiveMeetingByTeam(teamId, "AllDates");
//        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
//        if(meetingTables.size() == 0){
//            return RestResult.success().data(meetingSummaryVO);
//        }
//
//        List<MeetingTable> meetingTableLast = new ArrayList<>();
//        List<MeetingTable> meetingTableFirst = new ArrayList<>();
//        MeetingSummaryVO meetingSummaryLast = new MeetingSummaryVO();
//        MeetingSummaryVO meetingSummaryFist = new MeetingSummaryVO();
//        meetingTableLast.add(meetingTables.get(0));
//        meetingTableFirst.add(meetingTables.get(meetingTables.size() - 1));
//
//        computeTeamMeetingSummary(meetingSummaryVO, meetingTables);
//        computeTeamMeetingSummary(meetingSummaryLast, meetingTableLast);
//        computeTeamMeetingSummary(meetingSummaryFist, meetingTableFirst);
//
//        HashMap<String,List<Number>> ans = new HashMap<>();
//
////        Double engagementRate;
////        Double alignmentRate;
////        Double agencyRate;
////        Double burnoutRate;
////        Double scoreRate;
//        if (meetingSummaryFist.getEngagement() != null && meetingSummaryLast.getEngagement() != null) {
//            ans.put("engagement", Arrays.asList(meetingSummaryVO.getEngagement(),
//                    (meetingSummaryLast.getEngagement() - meetingSummaryFist.getEngagement()) * 100 / meetingSummaryFist.getEngagement()));
//        }
//        if (meetingSummaryFist.getAlignment() != null && meetingSummaryLast.getAlignment() != null) {
//            ans.put("alignment", Arrays.asList(meetingSummaryVO.getAlignment(),
//                    (meetingSummaryLast.getAlignment() - meetingSummaryFist.getAlignment()) * 100 / meetingSummaryFist.getAlignment()));
//        }
//        if (meetingSummaryFist.getAgency() != null && meetingSummaryLast.getAgency() != null) {
//            ans.put("agency", Arrays.asList(meetingSummaryVO.getAgency(),
//                    (meetingSummaryLast.getAgency() - meetingSummaryFist.getAgency()) * 100 / meetingSummaryFist.getAgency()));
//        }
//        if (meetingSummaryFist.getStress() != null && meetingSummaryLast.getStress() != null) {
//            ans.put("stress", Arrays.asList(meetingSummaryVO.getStress(),
//                    (meetingSummaryLast.getStress() - meetingSummaryFist.getStress()) * 100 / meetingSummaryFist.getStress()));
//        }
//        if (meetingSummaryFist.getBurnout() != null && meetingSummaryLast.getBurnout() != null) {
//            ans.put("burnout", Arrays.asList(meetingSummaryVO.getBurnout(),
//                    (meetingSummaryLast.getBurnout() - meetingSummaryFist.getBurnout()) * 100 / meetingSummaryFist.getBurnout()));
//        }
//        if (meetingSummaryFist.getScore() != null && meetingSummaryLast.getScore() != null) {
//            ans.put("score", Arrays.asList(meetingSummaryVO.getScore(),
//                    (meetingSummaryLast.getScore() - meetingSummaryFist.getScore()) * 100 / meetingSummaryFist.getScore()));
//        }
////        System.out.println("debug");
//        return RestResult.success().data(ans);
//    }

    @ApiOperation("Get team summary paras")
    @GetMapping("/team-summary-paras/{teamId}")
    public RestResult getMeetingSummary(@PathVariable("teamId")Long teamId){
        List<MeetingTable> meetingTables = meetingService.getLatestFiveMeetingByTeam(teamId, "AllDates");
        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
        if(meetingTables.size() == 0){
            return RestResult.success().data(meetingSummaryVO);
        }

        List<MeetingTable> meetingTableLast = new ArrayList<>();
        List<MeetingTable> meetingTableFirst = new ArrayList<>();

        meetingTableLast.add(meetingTables.get(0));
        meetingTableFirst.add(meetingTables.get(meetingTables.size() - 1));


        meetingSummaryVO = meetingService.computeTeamMeetingSummary(meetingTables);
        MeetingSummaryVO meetingSummaryLast = meetingService.computeTeamMeetingSummary(meetingTableLast);
        MeetingSummaryVO meetingSummaryFist = meetingService.computeTeamMeetingSummary(meetingTableFirst);

        HashMap<String, List<Number>> ans = new HashMap<>();

//        Double engagementRate;
//        Double alignmentRate;
//        Double agencyRate;
//        Double burnoutRate;
//        Double scoreRate;
        if (meetingSummaryFist.getEngagement() != null && meetingSummaryLast.getEngagement() != null) {
            ans.put("engagement", Arrays.asList(meetingSummaryVO.getEngagement(),
                    (meetingSummaryLast.getEngagement() - meetingSummaryFist.getEngagement())));
        }
        if (meetingSummaryFist.getAlignment() != null && meetingSummaryLast.getAlignment() != null) {
            ans.put("alignment", Arrays.asList(meetingSummaryVO.getAlignment(),
                    (meetingSummaryLast.getAlignment() - meetingSummaryFist.getAlignment()) ));
        }
        if (meetingSummaryFist.getAgency() != null && meetingSummaryLast.getAgency() != null) {
            ans.put("agency", Arrays.asList(meetingSummaryVO.getAgency(),
                    (meetingSummaryLast.getAgency() - meetingSummaryFist.getAgency()) ));
        }
        if (meetingSummaryFist.getStress() != null && meetingSummaryLast.getStress() != null) {
            ans.put("stress", Arrays.asList(meetingSummaryVO.getStress() * 100,
                    (meetingSummaryLast.getStress() * 100 - meetingSummaryFist.getStress() * 100)));
        }
        if (meetingSummaryFist.getBurnout() != null && meetingSummaryLast.getBurnout() != null) {
            ans.put("burnout", Arrays.asList(meetingSummaryVO.getBurnout(),
                    (meetingSummaryLast.getBurnout() - meetingSummaryFist.getBurnout())));
        }
        if (meetingSummaryFist.getScore() != null && meetingSummaryLast.getScore() != null) {
            ans.put("score", Arrays.asList(meetingSummaryVO.getScore(),
                    (meetingSummaryLast.getScore() - meetingSummaryFist.getScore())));
        }
        return RestResult.success().data(ans);
    }


    @ApiOperation("Get team summary list")
    @GetMapping("/team-summary-list/{teamId}")
    public RestResult getMeetingSummaries(@PathVariable("teamId")Long teamId){
        List<MeetingTable> meetingTables = meetingService.getLatestFiveMeetingByTeam(teamId, "AllDates");
        List<MeetingSummaryVO> summaryResult = new ArrayList<>();

        if(meetingTables.size() == 0){
            return RestResult.success().data(summaryResult);
        }

        for (MeetingTable meetingTable : meetingTables) {
            MeetingSummaryVO meetingSummaryVO = meetingService.computeTeamMeetingSummary(Arrays.asList(meetingTable));
            meetingSummaryVO.setMeetingId(meetingTable.getMeeting_id());
            summaryResult.add(meetingSummaryVO);
        }

        return RestResult.success().data(summaryResult);
    }

    @ApiOperation("Get meeting details")
    @GetMapping("/meeting-details/{teamId}")
    public RestResult getMeetingDetails(@PathVariable(value = "teamId") Long teamId) {

        MeetingDetailsVO meetingDetailsVO = new MeetingDetailsVO(teamId);

        List<MeetingTable> meetingsByTeamId = meetingService.getMeetingByTeamId(teamId);

        List<Long> meetingIdList = meetingsByTeamId.stream()
                .map(MeetingTable::getMeeting_id)
                .collect(Collectors.toList());
        meetingDetailsVO.setMeetingIds(meetingIdList);

        //1.performance
        List<Double> brain = meetingsByTeamId.stream()
                .map(MeetingTable::getBrain_score)
                .collect(Collectors.toList());
        List<Double> body = meetingsByTeamId.stream()
                .map(MeetingTable::getBody_score)
                .collect(Collectors.toList());
        List<Double> behavior = meetingsByTeamId.stream()
                .map(MeetingTable::getBehavior_score)
                .collect(Collectors.toList());
        HashMap<String, List<Double>> performance = new HashMap<>();
        performance.put("brain", brain);
        performance.put("body", body);
        performance.put("behavior", behavior);
        meetingDetailsVO.setPerformance(performance);

        //2.sentiment
        List<Double> neutral = new ArrayList<>();
        List<Double> negative = new ArrayList<>();
        List<Double> positive = new ArrayList<>();
        for (MeetingTable meetingTable : meetingsByTeamId) {
            List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingTable.getMeeting_id());
            handlePieEmotionRate(emotion);
            for (PieEmotionVO  pieEmotionVO : emotion) {
                if ("positive".equals(pieEmotionVO.getEmotion())) {
                    positive.add(pieEmotionVO.getEmotion_time_rate());
                } else if ("negative".equals(pieEmotionVO.getEmotion())) {
                    negative.add(pieEmotionVO.getEmotion_time_rate());
                } else if ("neutral".equals(pieEmotionVO.getEmotion())) {
                    neutral.add(pieEmotionVO.getEmotion_time_rate());
                }
            }
        }

//        List<Double> neutral = meetingsByTeamId.stream()
//                .map(MeetingTable::getMeeting_id)
//                .map(pieEmotionService::findEmotion)
//                .peek(this::handlePieEmotionRate)
//                .flatMap(List::stream)
//                .collect(Collectors.groupingBy(PieEmotionVO::getEmotion,
//                        Collectors.mapping(PieEmotionVO::getEmotion_time_rate, Collectors.toList())))
//                .getOrDefault("neutral", new ArrayList<>());
//        List<Double> negative = meetingsByTeamId.stream()
//                .map(MeetingTable::getMeeting_id)
//                .map(pieEmotionService::findEmotion)
//                .peek(this::handlePieEmotionRate)
//                .flatMap(List::stream)
//                .collect(Collectors.groupingBy(PieEmotionVO::getEmotion,
//                        Collectors.mapping(PieEmotionVO::getEmotion_time_rate, Collectors.toList())))
//                .getOrDefault("negative", new ArrayList<>());
//        List<Double> positive = meetingsByTeamId.stream()
//                .map(MeetingTable::getMeeting_id)
//                .map(pieEmotionService::findEmotion)
//                .peek(this::handlePieEmotionRate)
//                .flatMap(List::stream)
//                .collect(Collectors.groupingBy(PieEmotionVO::getEmotion,
//                        Collectors.mapping(PieEmotionVO::getEmotion_time_rate, Collectors.toList())))
//                .getOrDefault("negative", new ArrayList<>());
        HashMap<String, List<Double>> sentiment = new HashMap<>();
        sentiment.put("neutral", neutral);
        sentiment.put("negative", negative);
        sentiment.put("positive", positive);
        meetingDetailsVO.setSentiment(sentiment);

        //3.global TODO:修改一下计算结果
        List<TeamGlobalSynchronyVO> teamGlobalSynchronyVOList = new ArrayList<>(meetingsByTeamId.size());
        for (MeetingTable meetingTable : meetingsByTeamId) {
            teamGlobalSynchronyVOList.add(covertToTeamGlobalSynchrony(meetingTable));
        }

        List<Double> global = meetingsByTeamId.stream()
                .map(MeetingTable::getTotal_score)
                .collect(Collectors.toList());
        meetingDetailsVO.setGlobal(global);


        //4.status
//        for (MeetingTable meetingTable : meetingsByTeamId) {
//            MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
//            computeTeamMeetingSummary(meetingSummaryVO, Arrays.asList(meetingTable));
//        }
        HashMap<String, List<Double>> status = new HashMap<>();
        List<MeetingSummaryVO> meetingSummaryVOList = meetingsByTeamId.stream()
                .map(meetingTable -> {
                    return meetingService.computeTeamMeetingSummary(Arrays.asList(meetingTable));
                })
                .collect(Collectors.toList());
        List<Double> engagement = meetingSummaryVOList.stream()
                .collect(Collectors.mapping(MeetingSummaryVO::getEngagement, Collectors.toList()));
        List<Double> alignment = meetingSummaryVOList.stream()
                .map(MeetingSummaryVO::getAlignment)
                .collect(Collectors.toList());
        List<Double> agency = meetingSummaryVOList.stream()
                .map(MeetingSummaryVO::getAgency)
                .collect(Collectors.toList());
        List<Double> stress = meetingSummaryVOList.stream()
                .map(MeetingSummaryVO::getStress)
                .collect(Collectors.toList());
        List<Double> burnout = meetingSummaryVOList.stream()
                .map(MeetingSummaryVO::getBurnout)
                .collect(Collectors.toList());
        List<Double> score = meetingSummaryVOList.stream()
                .map(MeetingSummaryVO::getScore)
                .collect(Collectors.toList());
        status.put("engagement", engagement);
        status.put("alignment", alignment);
        status.put("agency", agency);
        status.put("stress", stress);
        status.put("burnout", burnout);
        status.put("score", score);
        meetingDetailsVO.setStatus(status);

        //5.dimension
        HashMap<String, List<Double>> dimension = new HashMap<>();
//        for (MeetingTable meetingTable : meetingsByTeamId) {
//            List<RadarVO> kv = radarService.findKV(meetingTable.getMeeting_id());
//            for (RadarVO radarVO : kv) {
//                String k = radarVO.getK();
//                Double v = radarVO.getV();
//                List<Double> doubles = dimension.getOrDefault(k, new ArrayList<>());
//                doubles.add(v);
//                dimension.put(k, doubles);
//            }
//        }
        meetingsByTeamId.forEach(
                meetingTable -> {
                    List<RadarVO> kv = radarService.findKV(meetingTable.getMeeting_id());
                    kv.forEach(
                            radarVO -> {
                                String k = radarVO.getK();
                                Double v = radarVO.getV();
                                dimension.computeIfAbsent(k, key -> new ArrayList<>()).add(v);
                            }
                    );
                }
        );
        meetingDetailsVO.setDimension(dimension);

        return RestResult.success().data(meetingDetailsVO);
    }

    @ApiOperation("Delete team's meeting")
    @DeleteMapping("/{teamId}")
    public RestResult deleteTeam(@PathVariable(value = "teamId") Long teamId) {
        log.info("[TeamController][deleteTeam] teamId :{}", teamId);
        LambdaQueryWrapper<MeetingTable> queryWrapper = new LambdaQueryWrapper<MeetingTable>()
                .eq(MeetingTable::getTeam_id, teamId);
        List<MeetingTable> meetingTableList = meetingService.list(queryWrapper);
        for (MeetingTable meetingTable : meetingTableList) {
            log.info("[TeamController][deleteTeam] meetingId :{}", meetingTable.getMeeting_id());
            meetingService.removeCvDataByMeetingId(meetingTable.getMeeting_id());
            meetingService.removeNlpDataByMeetingId(meetingTable.getMeeting_id());
            HashMap<String,Object> deleteMap = new HashMap<>();
            deleteMap.put("meeting_id", meetingTable.getMeeting_id());
            meetingService.removeByMap(deleteMap);
        }
        return RestResult.success();
    }

    @ApiOperation("Get teams info")
    @PostMapping("/info")
    public RestResult getTeamsInfo(@RequestParam(value = "page", defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "size", defaultValue = "5") Integer pageSize,
                             @RequestParam(value = "date", defaultValue = "AllDates") String date,
                             @RequestParam(value = "teamIds", required = false) List<Long> teamIds) {
        //TODO: 需要解决teamIds 结合 date出现没有meeting的情况，然后调节分页数据。
        HashMap<String, Object> hashMap = new HashMap<>();
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 5;
        }
        if (teamIds == null || teamIds.isEmpty()) {
            Page<Long> item = meetingService.selectDistinctTeamIds(pageNum, pageSize);
            teamIds = item.getRecords();
            hashMap.put("currentPage", item.getCurrent());
            hashMap.put("pageSize", item.getSize());
            hashMap.put("totalPages", item.getPages());
            hashMap.put("total", item.getTotal());
        } else {
            int totalItems = teamIds.size();
            int start = Math.min((pageNum - 1) * pageSize, totalItems);
            int end = Math.min(pageNum * pageSize, totalItems);
            teamIds = teamIds.subList(start, end);
            hashMap.put("currentPage", pageNum);
            hashMap.put("pageSize", pageSize);
            hashMap.put("totalPages", (int) Math.ceil((double) totalItems / pageSize));
            hashMap.put("total", totalItems);
        }
        List<TeamInfoVO> teamInfoVOList = new ArrayList<>();
        for (Long teamId : teamIds) {
            Long duration = 0L;
            TeamInfoVO teamInfoVO = new TeamInfoVO();
            teamInfoVO.setTeamId(teamId);

            List<MeetingTable> meetingsByTeamId = meetingService.getLatestNMeetingByTeam(teamId, date, pageSize);
            List<MeetingTable> meetingByTeamId = meetingService.getMeetingByTeamId(teamId);
            duration += meetingByTeamId.stream().filter(meetingTable -> meetingTable.getDuration() != null)
                    .mapToLong(MeetingTable::getDuration)
                    .sum();
            if (meetingsByTeamId.size() == 0) continue;
            //1.performance
            //计算brain
            double brain = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBrain_score() != null)
                    .mapToDouble(MeetingTable::getBrain_score)
                    .average()
                    .orElse(0);
            //计算body
            double body = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBody_score() != null)
                    .mapToDouble(MeetingTable::getBody_score)
                    .average()
                    .orElse(0);
            //计算behaviour
            double behavior = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBehavior_score() != null)
                    .mapToDouble(MeetingTable::getBehavior_score)
                    .average()
                    .orElse(0);
            HashMap<String, Double> performance = new HashMap<>();
            performance.put("brain", brain);
            performance.put("body", body);
            performance.put("behavior", behavior);
            teamInfoVO.setPerformance(performance);

            //2.sentiment
            List<PieEmotionVO> neutral = new ArrayList<>();
            List<PieEmotionVO> negative = new ArrayList<>();
            List<PieEmotionVO> positive = new ArrayList<>();
            for (MeetingTable meetingTable : meetingsByTeamId) {
                List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingTable.getMeeting_id());
                handlePieEmotionRate(emotion);
                for (PieEmotionVO  pieEmotionVO : emotion) {
                    if ("positive".equals(pieEmotionVO.getEmotion())) {
                        positive.add(pieEmotionVO);
                    } else if ("negative".equals(pieEmotionVO.getEmotion())) {
                        negative.add(pieEmotionVO);
                    } else if ("neutral".equals(pieEmotionVO.getEmotion())) {
                        neutral.add(pieEmotionVO);
                    }
                }
            }

            double neutralScore = neutral.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
                    .average()
                    .orElse(0);

            double negativeScore = negative.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
                    .average()
                    .orElse(0);

            double positiveScore = positive.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
                    .average()
                    .orElse(0);

            HashMap<String, Double> sentiment = new HashMap<>();
            sentiment.put("neutral", neutralScore);
            sentiment.put("negative", negativeScore);
            sentiment.put("positive", positiveScore);
            teamInfoVO.setSentiment(sentiment);

            //3.global TODO:修改一下计算结果
            List<TeamGlobalSynchronyVO> teamGlobalSynchronyVOList = new ArrayList<>(meetingsByTeamId.size());
            for (MeetingTable meetingTable : meetingsByTeamId) {
                teamGlobalSynchronyVOList.add(covertToTeamGlobalSynchrony(meetingTable));
            }

            double global = teamGlobalSynchronyVOList.stream().filter(teamGlobalSynchronyVO -> teamGlobalSynchronyVO.getTotal_score() != null)
                    .mapToDouble(TeamGlobalSynchronyVO::getTotal_score)
                    .average()
                    .orElse(0);
            teamInfoVO.setGlobal(global);

            //4.status
            List<MeetingScoreVO> scores = meetingsByTeamId.stream()
                    .map(meetingTable -> {
                        MeetingSummaryVO meetingSummaryVO = meetingService.computeTeamMeetingSummary(Collections.singletonList(meetingTable));
                        meetingSummaryVO.setMeetingId(meetingTable.getMeeting_id());
                        MeetingScoreVO meetingScoreVO = new MeetingScoreVO();
                        BeanUtils.copyProperties(meetingTable, meetingScoreVO);
                        meetingScoreVO.setStatus(meetingSummaryVO);
                        return meetingScoreVO;
                    })
                    .collect(Collectors.toList());
            /*List<MeetingScoreVO> scores = new ArrayList<>();
            for (MeetingTable meetingTable : meetingsByTeamId) {
                List<MeetingTable> meetingTableList = Arrays.asList(meetingTable);
                MeetingSummaryVO meetingSummaryVO = computeTeamMeetingSummary(meetingTableList);
                MeetingScoreVO meetingScoreVO = new MeetingScoreVO();
                BeanUtils.copyProperties(meetingTable, meetingScoreVO);
                meetingScoreVO.setStatus(meetingSummaryVO);
                scores.add(meetingScoreVO);
            }*/
            teamInfoVO.setScore(scores);
            if(meetingsByTeamId.size() != 0) {
                List<MeetingTable> meetingTableLast = new ArrayList<>();
                List<MeetingTable> meetingTableFirst = new ArrayList<>();
                meetingTableLast.add(meetingsByTeamId.get(0));
                meetingTableFirst.add(meetingsByTeamId.get(meetingsByTeamId.size() - 1));

                MeetingSummaryVO meetingSummaryLast = meetingService.computeTeamMeetingSummary(meetingTableLast);
                MeetingSummaryVO meetingSummaryFist = meetingService.computeTeamMeetingSummary(meetingTableFirst);

                HashMap<String, Double> status = new HashMap<>();

                if (meetingSummaryFist.getEngagement() != null && meetingSummaryLast.getEngagement() != null) {
                    status.put("engagement", (meetingSummaryLast.getEngagement() - meetingSummaryFist.getEngagement()) * 100 / meetingSummaryFist.getEngagement());
                }
                if (meetingSummaryFist.getAlignment() != null && meetingSummaryLast.getAlignment() != null) {
                    status.put("alignment", (meetingSummaryLast.getAlignment() - meetingSummaryFist.getAlignment()) * 100 / meetingSummaryFist.getAlignment());
                }
                if (meetingSummaryFist.getAgency() != null && meetingSummaryLast.getAgency() != null) {
                    status.put("agency", (meetingSummaryLast.getAgency() - meetingSummaryFist.getAgency()) * 100 / meetingSummaryFist.getAgency());
                }
                if (meetingSummaryFist.getStress() != null && meetingSummaryLast.getStress() != null) {
                    status.put("stress", (meetingSummaryLast.getStress() - meetingSummaryFist.getStress()) * 100 / meetingSummaryFist.getStress());
                }
                if (meetingSummaryFist.getBurnout() != null && meetingSummaryLast.getBurnout() != null) {
                    status.put("burnout", (meetingSummaryLast.getBurnout() - meetingSummaryFist.getBurnout()) * 100 / meetingSummaryFist.getBurnout());
                }
                if (meetingSummaryFist.getScore() != null && meetingSummaryLast.getScore() != null) {
                    status.put("score", (meetingSummaryLast.getScore() - meetingSummaryFist.getScore()) * 100 / meetingSummaryFist.getScore());
                }
                teamInfoVO.setStatus(status);
            }

            //5.dimension
            HashMap<String, Double[]> kvMap = new HashMap<>();
            for (MeetingScoreVO meetingTable : scores) {
                List<RadarVO> kv = radarService.findKV(meetingTable.getMeeting_id());
                Map<String, Double> dimension = kv.stream().collect(Collectors.toMap(RadarVO::getK, RadarVO::getV));
                meetingTable.setDimension(dimension);
                for (RadarVO radarVO : kv) {
                    String k = radarVO.getK();
                    Double v = radarVO.getV();

                    // 检查v值是否为0或NaN，如果是，则不加入计算
                    if (v == null || v.isNaN()) {
                        continue;
                    }

                    // 更新kvMap
                    Double[] sumAndCount = kvMap.getOrDefault(k, new Double[]{0.0, 0.0});
                    sumAndCount[0] += v; // 总和
                    sumAndCount[1] += 1; // 计数
                    kvMap.put(k, sumAndCount);
                }
            }
            // 计算平均值
            HashMap<String, Double> dimension = new HashMap<>();
            for (HashMap.Entry<String, Double[]> entry : kvMap.entrySet()) {
                String k = entry.getKey();
                Double[] sumAndCount = entry.getValue();
                if (sumAndCount[1] == 0) {
                    // 如果计数为0，即没有有效的v值
                    continue;
                }
                Double average = sumAndCount[0] / sumAndCount[1];
                dimension.put(k, average);
            }
            teamInfoVO.setDimension(dimension);
            teamInfoVO.setDuration(duration);
            teamInfoVOList.add(teamInfoVO);
        }

        hashMap.put("info", teamInfoVOList);
        return RestResult.success().data(hashMap);
    }

    @ApiOperation("Query user info")
    @GetMapping("/user")
    public RestResult getUserInfo(@RequestParam(value = "userId") String userId,
                                  @RequestParam(value = "teamId", required = false) Long teamId) {
        List<CVUser> cvUsers = null;
        if (teamId == null) {
            cvUsers = cvUserService.getByUserId(userId);
        } else {
            cvUsers = cvUserService.getByUserIdAndTeamId(userId, teamId);
        }
        return RestResult.success().data(cvUsers);
    }

    @ApiOperation("Match cv user with login user")
    @PostMapping("/match")
    public RestResult matchUserId(@RequestParam(value = "userId") String userId,
                                  @RequestParam(value = "username") Long username,
                                  @RequestParam(value = "meetingId") Long meetingId) {
        //在loginUserController中已经实现
        cvUserService.handleUserMatch(userId, username, meetingId);
        return RestResult.success();
    }

//    @ApiOperation("Get time info")
//    @GetMapping("/time")
//    public RestResult getTimeInfo(@PathVariable(value = "teamId") String teamId,
//                                  @RequestParam(value = "timestamp") Long timestamp) {
//        HashMap<String, Long> timeInfo = teamService.queryTimeInfo(teamId, timestamp);
//        return RestResult.success().data(timeInfo);
//    }

    @ApiOperation("Get time info")
    @PostMapping("/time")
    public RestResult getTimeInfo(@RequestBody TimeSearchDTO timeSearchDTO) {
        Long time = teamService.queryVideoTime(timeSearchDTO);
        HashMap<String, Long> timeMap = new HashMap<>();
        timeMap.put("total_time", time);
        return RestResult.success().data(timeMap);
    }

    @ApiOperation("Find team meeting")
    @RequestMapping(value = {"/meetings/{teamId}"}, method = RequestMethod.GET)
    public RestResult findTeamMeetingTypeByTeamId(@PathVariable("teamId")Long teamId,
                                                    @RequestParam(required = false, name = "meetingType" ) String meetingType) {
        List<MeetingTable> meetingTableList = meetingService.findMeetingInfoByTeamId(teamId, meetingType);
        return RestResult.success().data(meetingTableList);
    }
//    @GetMapping("/update")
//    public RestResult updateTeamId() {
//        List<CVUser> cvUsers = cvUserService.list();
//        for (CVUser cvUser : cvUsers) {
//            if (cvUser.getCreate_date() == null || cvUser.getTeam_id() == null) {
//                Long meeting_id = cvUser.getMeeting_id();
//                MeetingTable byMeetingId = meetingService.getByMeetingId(meeting_id);
//                LambdaUpdateWrapper<CVUser> cvUserUpdateWrapper = new LambdaUpdateWrapper<CVUser>();
//                cvUserUpdateWrapper.eq(CVUser::getMeeting_id, meeting_id).eq(CVUser::getUser_name, cvUser.getUser_name());
//                        if (byMeetingId != null) {
//                            cvUserUpdateWrapper  .set(CVUser::getTeam_id, byMeetingId.getTeam_id())
//                                    .set(CVUser::getCreate_date, byMeetingId.getVideo_create_time());
//                        }
//
//                cvUserService.update(null, cvUserUpdateWrapper);
//            }
//        }
//        return RestResult.success().data(cvUsers);
//    }
//    public MeetingSummaryVO computeTeamMeetingSummary(List<MeetingTable> meetingTables){
//        MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
//        radarService.setEngagementAndAgency(meetingTables, meetingSummaryVO);
//        computeAndSetAlignment(meetingTables, meetingSummaryVO);
//        computeAndSetStress(meetingTables, meetingSummaryVO);
//        computeAndSetBurnout(meetingSummaryVO);
//        computeAndSetScore(meetingTables, meetingSummaryVO);
//        return meetingSummaryVO;
//    }

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

    private TeamGlobalSynchronyVO covertToTeamGlobalSynchrony(MeetingTable meetingTable) {
        TeamGlobalSynchronyVO t = new TeamGlobalSynchronyVO();
        t.setMeeting_id(meetingTable.getMeeting_id());
        t.setCreate_time(meetingTable.getVideo_create_time());
        t.setTotal_score(meetingTable.getTotal_score());
        t.setMeeting_name(meetingTable.getMeeting_name());
        return t;
    }

    private TeamPerformanceVO covertToTeamPerformance(MeetingTable meetingTable) {
        TeamPerformanceVO t = new TeamPerformanceVO();
        t.setMeeting_id(meetingTable.getMeeting_id());
        t.setCreate_time(meetingTable.getVideo_create_time());
        t.setBehavior_score(meetingTable.getBehavior_score());
        t.setBody_score(meetingTable.getBody_score());
        t.setBrain_score(meetingTable.getBrain_score());
        t.setMeeting_name(meetingTable.getMeeting_name());
        return t;
    }


}
