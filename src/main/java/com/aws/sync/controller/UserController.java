//package com.aws.sync.controller;
//
//import com.aws.sync.config.common.RestResult;
//import com.aws.sync.entity.MeetingTable;
//import com.aws.sync.service.MeetingService;
//import com.aws.sync.service.PieEmotionService;
//import com.aws.sync.service.RadarService;
//import com.aws.sync.vo.*;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//
//
//@Slf4j
//@RestController
//@CrossOrigin
//@RequestMapping("/user")
//public class UserController {
////    @Autowired
////    UserAvatarService userAvatarService;
////
////    @Autowired
////    AmazonUploadService s3Service;
////
////    @ApiOperation("获取头像列表")
////    @GetMapping("/avatar/{meetingID}")
////    @ResponseBody
////    public RestResult userAvatarList(@PathVariable("meetingID") Long meetingID){
////        List<UserAvatarVO> userAvatar = userAvatarService.findUserAvatar(meetingID);
////        Map<String,String> m = new HashMap<>();
////        for(UserAvatarVO u : userAvatar){
////            m.put(u.getUsers(),u.getUrl());
////        }
////        Map<String,Object> res = new HashMap<>();
////        res.put("userAvatar",m);
////        return RestResult.success().data(res);
////    }
////
////    @ApiOperation("获取用户头像")
////    @RequestMapping(value = {"/img/{meetingID}/{url}"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
////    @ResponseBody
////    public RestResult userAvatar(HttpServletRequest httpServletRequest,
////                              HttpServletResponse httpServletResponse,@PathVariable("meetingID") Long meetingID,@PathVariable("url") String url) throws IOException {
////
////        byte[] img = s3Service.downloadUserAvatar(Long.toString(meetingID) + "/" + url);
//////        httpServletResponse.setContentType("image/png");
////        OutputStream os = httpServletResponse.getOutputStream();
////        os.write(img);
////        os.flush();
////        os.close();
////        return RestResult.success();
////    }
//    @Autowired
//    MeetingService meetingService;
//
//    @Autowired
//    RadarService radarService;
//
//    @Autowired
//    PieEmotionService pieEmotionService;
//    @ApiOperation("Get teams info")
//    @PostMapping("/info")
//    public RestResult upload(@RequestParam(value = "page", defaultValue = "1") Integer pageNum,
//                             @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
//                             @RequestParam(value = "userId", required = false) String userId) {
//        List<Long> teams = new ArrayList<>();
//        List<TeamInfoVO> teamInfoVOList = new ArrayList<>();
//        for (Long teamId : teams) {
//            TeamInfoVO teamInfoVO = new TeamInfoVO();
//            teamInfoVO.setTeamId(teamId);
//            List<MeetingTable> meetingsByTeamId = meetingService.getLatestFiveMeetingByTeam(teamId);
//
//            //1.performance
//            //计算brain
//            double brain = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBrain_score() != null)
//                    .mapToDouble(MeetingTable::getBrain_score)
//                    .average()
//                    .orElse(0);
//            //计算body
//            double body = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBody_score() != null)
//                    .mapToDouble(MeetingTable::getBody_score)
//                    .average()
//                    .orElse(0);
//            //计算behaviour
//            double behavior = meetingsByTeamId.stream().filter(meetingTable -> meetingTable.getBehavior_score() != null)
//                    .mapToDouble(MeetingTable::getBody_score)
//                    .average()
//                    .orElse(0);
//            HashMap<String, Double> performance = new HashMap<>();
//            performance.put("brain", brain);
//            performance.put("body", body);
//            performance.put("behavior", behavior);
//            teamInfoVO.setPerformance(performance);
//
//            //2.sentiment
//            List<PieEmotionVO> neutral = new ArrayList<>();
//            List<PieEmotionVO> negative = new ArrayList<>();
//            List<PieEmotionVO> positive = new ArrayList<>();
//            for (MeetingTable meetingTable : meetingsByTeamId) {
//                List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingTable.getMeeting_id());
//                handlePieEmotionRate(emotion);
//                for (PieEmotionVO  pieEmotionVO : emotion) {
//                    if ("positive".equals(pieEmotionVO.getEmotion())) {
//                        positive.add(pieEmotionVO);
//                    } else if ("negative".equals(pieEmotionVO.getEmotion())) {
//                        negative.add(pieEmotionVO);
//                    } else if ("neutral".equals(pieEmotionVO.getEmotion())) {
//                        neutral.add(pieEmotionVO);
//                    }
//                }
//            }
//
//            double neutralScore = neutral.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
//                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
//                    .average()
//                    .orElse(0);
//
//            double negativeScore = negative.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
//                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
//                    .average()
//                    .orElse(0);
//
//            double positiveScore = positive.stream().filter(pieEmotionVO -> pieEmotionVO.getEmotion_time_rate() != null)
//                    .mapToDouble(PieEmotionVO::getEmotion_time_rate)
//                    .average()
//                    .orElse(0);
//
//            HashMap<String, Double> sentiment = new HashMap<>();
//            sentiment.put("neutral", neutralScore);
//            sentiment.put("negative", negativeScore);
//            sentiment.put("positive", positiveScore);
//            teamInfoVO.setSentiment(sentiment);
//
//            //3.global
//            List<TeamGlobalSynchronyVO> teamGlobalSynchronyVOList = new ArrayList<>(meetingsByTeamId.size());
//            for (MeetingTable meetingTable : meetingsByTeamId) {
//                teamGlobalSynchronyVOList.add(covertToTeamGlobalSynchrony(meetingTable));
//            }
//            double global = teamGlobalSynchronyVOList.stream().filter(teamGlobalSynchronyVO -> teamGlobalSynchronyVO.getTotal_score() != null)
//                    .mapToDouble(TeamGlobalSynchronyVO::getTotal_score)
//                    .average()
//                    .orElse(0);
//            teamInfoVO.setGlobal(global);
//
//            //4.status
//            MeetingSummaryVO meetingSummaryVO = new MeetingSummaryVO();
//            if(meetingsByTeamId.size() == 0){
//                List<MeetingTable> meetingTableLast = new ArrayList<>();
//                List<MeetingTable> meetingTableFirst = new ArrayList<>();
//                MeetingSummaryVO meetingSummaryLast = new MeetingSummaryVO();
//                MeetingSummaryVO meetingSummaryFist = new MeetingSummaryVO();
//                meetingTableLast.add(meetingsByTeamId.get(0));
//                meetingTableFirst.add(meetingsByTeamId.get(meetingsByTeamId.size() - 1));
//
//                computeTeamMeetingSummary(meetingSummaryVO, meetingsByTeamId);
//                computeTeamMeetingSummary(meetingSummaryLast, meetingTableLast);
//                computeTeamMeetingSummary(meetingSummaryFist, meetingTableFirst);
//
//                HashMap<String, Double> status = new HashMap<>();
//
//                if (meetingSummaryFist.getEngagement() != null && meetingSummaryLast.getEngagement() != null) {
//                    status.put("engagement", meetingSummaryLast.getEngagement() - meetingSummaryFist.getEngagement() * 100 / meetingSummaryFist.getEngagement());
//                }
//                if (meetingSummaryFist.getAlignment() != null && meetingSummaryLast.getAlignment() != null) {
//                    status.put("alignment", meetingSummaryLast.getAlignment() - meetingSummaryFist.getAlignment() * 100 / meetingSummaryFist.getAlignment());
//                }
//                if (meetingSummaryFist.getAgency() != null && meetingSummaryLast.getAgency() != null) {
//                    status.put("agency", meetingSummaryLast.getAgency() - meetingSummaryFist.getAgency() * 100 / meetingSummaryFist.getAgency());
//                }
//                if (meetingSummaryFist.getStress() != null && meetingSummaryLast.getStress() != null) {
//                    status.put("stress", meetingSummaryLast.getStress() - meetingSummaryFist.getStress() * 100 / meetingSummaryFist.getStress());
//                }
//                if (meetingSummaryFist.getBurnout() != null && meetingSummaryLast.getBurnout() != null) {
//                    status.put("burnout", meetingSummaryLast.getBurnout() - meetingSummaryFist.getBurnout() * 100 / meetingSummaryFist.getBurnout());
//                }
//                if (meetingSummaryFist.getScore() != null && meetingSummaryLast.getScore() != null) {
//                    status.put("score", meetingSummaryLast.getScore() - meetingSummaryFist.getScore() * 100 / meetingSummaryFist.getScore());
//                }
//                teamInfoVO.setStatus(status);
//            }
//
//            teamInfoVOList.add(teamInfoVO);
//        }
//        return RestResult.success().data(teamInfoVOList);
//    }
//
//    private void handlePieEmotionRate(List<PieEmotionVO> emotion) {
//        BigDecimal total = BigDecimal.ZERO;
//        total.setScale(3, RoundingMode.HALF_UP);
//        if (emotion.size() > 3) {
//            return;
//        }
//        for (int i = 0; i < emotion.size(); i++) {
//            if (i != emotion.size() - 1) {
//                BigDecimal t = BigDecimal.valueOf(emotion.get(i).getEmotion_time_rate());
//                t.setScale(3, RoundingMode.HALF_UP);
//                total = total.add(t);
//                emotion.get(i).setEmotion_time_rate(t.doubleValue());
//            } else {
//                emotion.get(i).setEmotion_time_rate(BigDecimal.ONE.subtract(total).doubleValue());
//            }
//        }
//    }
//
//
//    public void computeTeamMeetingSummary(MeetingSummaryVO meetingSummaryVO, List<MeetingTable> meetingTables){
//        radarService.setEngagementAndAgency(meetingTables, meetingSummaryVO);
//        computeAndSetAlignment(meetingTables, meetingSummaryVO);
//        computeAndSetStress(meetingTables, meetingSummaryVO);
//        computeAndSetBurnout(meetingSummaryVO);
//        computeAndSetScore(meetingTables, meetingSummaryVO);
//    }
//
//    private void computeAndSetScore(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
//        Double totalScore = 0.0d;
//        Integer totalCount = 0;
//        for (MeetingTable meetingTable : meetingTables) {
//            Double score = meetingTable.getTotal_score();
//            if(score != null){
//                totalScore += score;
//                totalCount ++;
//            }
//        }
//        if (totalCount != 0) {
//            meetingSummaryVO.setScore(totalScore / totalCount);
//        }
//    }
//
//    private void computeAndSetAlignment(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
//        Double totalScore = 0.0d;
//        Integer totalCount = 0;
//        for (MeetingTable meetingTable : meetingTables) {
//            if(meetingTable.getTotal_score() != null){
//                // 归一化到0-1
//                double normalizedX = meetingTable.getTotal_score() / 100.0;
//                // 调整后的Sigmoid函数参数
//                double k = 10;  // 控制曲线的陡度
//                double x0 = 0.5; // 控制S形的中心点
//
//                // 计算S型函数的输出
//                double alignment = 1.0 / (1.0 + Math.exp(-k * (normalizedX - x0)));
//
//                totalScore += alignment;
//                totalCount ++;
//            }
//        }
//        if(totalCount != 0 ){
//            meetingSummaryVO.setAlignment(totalScore / totalCount);
//        }
//    }
//
//    private void computeAndSetBurnout(MeetingSummaryVO meetingSummaryVO) {
//        int totalCount = 0;
//        Double totalScore = 0.0d;
//        Double stress = meetingSummaryVO.getStress();
//        Double engagement = meetingSummaryVO.getEngagement();
//        if(stress != null && !Double.isNaN(stress)){
//            totalCount ++;
//            totalScore += stress;
//        }
//        if(engagement != null && !Double.isNaN(engagement)){
//            totalCount ++;
//            totalScore += engagement;
//        }
//        if(totalCount != 0){
//            meetingSummaryVO.setBurnout(totalScore / totalCount);
//        }
//    }
//
//    private void computeAndSetStress(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
//        int totalCount = 0;
//        Double totalScore = 0.0d;
//        for (MeetingTable meetingTable : meetingTables) {
//            Double hrv = meetingTable.getHrv();
//            if(hrv != null && !Double.isNaN(hrv)){
//                totalCount ++;
//                totalScore += hrv;
//            }
//        }
//        Double hrvAve = null;
//        Double stress = null;
//        if(totalCount != 0){
//            hrvAve = totalScore / totalCount;
//            stress = Math.pow(Math.E, -hrvAve);
//        }
//
//        meetingSummaryVO.setStress(stress);
//    }
//
//    private TeamGlobalSynchronyVO covertToTeamGlobalSynchrony(MeetingTable meetingTable) {
//        TeamGlobalSynchronyVO t = new TeamGlobalSynchronyVO();
//        t.setMeeting_id(meetingTable.getMeeting_id());
//        t.setCreate_time(meetingTable.getVideo_create_time());
//        t.setTotal_score(meetingTable.getTotal_score());
//        t.setMeeting_name(meetingTable.getMeeting_name());
//        return t;
//    }
//}
