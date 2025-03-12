package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.config.common.SectionTeamInsightEnum;
import com.aws.sync.config.common.SectionUserInsightEnum;
import com.aws.sync.entity.*;
import com.aws.sync.service.*;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.vo.*;
import com.aws.sync.vo.csv.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/csv")
@CrossOrigin
public class CsvController {
    @Autowired
    AResultService aResultService;

    @Autowired
    VResultService vResultService;

    @Autowired
    RResultService rResultService;

    @Autowired
    ASyncService aSyncService;

    @Autowired
    VSyncService vSyncService;

    @Autowired
    RSyncService rSyncService;

    @Autowired
    MeetingService meetingService;

    @Autowired
    HeatmapService heatmapService;

    @Autowired
    NlpService nlpService;

    @Autowired
    PieSpeakerService pieSpeakerService;

    @Autowired
    PieEmotionService pieEmotionService;

    @Autowired
    PieActService pieActService;

    @Autowired
    BarEmotionService barEmotionService;

    @Autowired
    RadarService radarService;

    @Autowired
    SectionService sectionService;

    @Autowired
    IndividualAService individualAService;

    @Autowired
    IndividualVService individualVService;

    @Autowired
    IndividualRService individualRService;

    @Autowired
    IndividualSyncService individualSyncService;

    @Autowired
    AveSyncService aveSyncService;

    @Autowired
    UserAvatarService userAvatarService;

    @Autowired
    CVUserService cvUserServiceUser;

    @Autowired
    NlpWordCountService nlpWordCountService;

    @Autowired
    IndividualScoreService individualScoreService;

    @Autowired
    EmojiService emojiService;

    @Autowired
    HighlightStatementService highlightStatementService;

    @Autowired
    UserDistanceService userDistanceService;

    @Autowired
    PosAndNegRateService posAndNegRateService;

    @Autowired
    SynchronyMomentService synchronyMomentService;

    @Autowired
    UserContributionService userContributionService;

    @Autowired
    UniverseGroupService universeGroupService;

    @Autowired
    WordRateService wordRateService;

    @ApiOperation("GetAResult")
    @GetMapping("/aresult")
    @ResponseBody
    public RestResult getAResult(@RequestParam("meetingID") Long meetingID){
        List<AMean> mean = aResultService.findMean(meetingID);
        List<List<Object>> aMean = new ArrayList<>();
        for(AMean a : mean){
            aMean.add(Arrays.asList(a.getTime_ms(),a.getA_mean()));
        }

        int count = cvUserServiceUser.findUserCount(meetingID);
        List<AllUser> user = aResultService.findUser(meetingID);
        Map<String, Object> ans = CsvUtil.handleResult(user, count);
        ans.put("a_mean",aMean);

        return RestResult.success().data(ans);
    }

    @ApiOperation("GetVResult")
    @GetMapping("/vresult")
    @ResponseBody
    public RestResult getVResult(@RequestParam("meetingID") Long meetingID){
        List<VMean> mean = vResultService.findMean(meetingID);
        List<List<Object>> vMean = new ArrayList<>();
        for(VMean v : mean){
            vMean.add(Arrays.asList(v.getTime_ms(),v.getV_mean()));
        }

        int count = cvUserServiceUser.findUserCount(meetingID);
        List<AllUser> user = vResultService.findUser(meetingID);
        Map<String, Object> ans = CsvUtil.handleResult(user, count);
        ans.put("v_mean",vMean);

        return RestResult.success().data(ans);
//        List<VMean> mean = vResultService.findMean(meetingID);
//        List<AllUser> user = vResultService.findUser(meetingID);
//        List<List<Object>> vMean = new ArrayList<>();
//        List<List<Object>> vUser00 = new ArrayList<>();
//        List<List<Object>> vUser01 = new ArrayList<>();
//        List<List<Object>> vUser10 = new ArrayList<>();
//        for(VMean v : mean){
//            vMean.add(Arrays.asList(v.getTime_ms(),v.getV_mean()));
//        }
//        for(AllUser v : user){
//            vUser00.add(Arrays.asList(v.getTime_ms(),v.getUser00()));
//            vUser01.add(Arrays.asList(v.getTime_ms(),v.getUser01()));
//            vUser10.add(Arrays.asList(v.getTime_ms(),v.getUser10()));
//        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("v_mean",vMean);
//        map.put("user00",vUser00);
//        map.put("user01",vUser01);
//        map.put("user10",vUser10);
//        return RestResult.success().data(map);
    }

    @ApiOperation("GetRppgResult")
    @GetMapping("/rresult")
    @ResponseBody
    public RestResult getRResult(@RequestParam("meetingID") Long meetingID){
        List<RMean> mean = rResultService.findMean(meetingID);
        List<List<Object>> rMean = new ArrayList<>();
        for(RMean r : mean){
            rMean.add(Arrays.asList(r.getTime_ms(),r.getR_mean()));
        }

        int count = cvUserServiceUser.findUserCount(meetingID);
        List<AllUser> user = rResultService.findUser(meetingID);
        Map<String, Object> ans = CsvUtil.handleResult(user, count);
        ans.put("r_mean",rMean);

        return RestResult.success().data(ans);
//        List<RMean> mean = rResultService.findMean(meetingID);
//        List<AllUser> user = rResultService.findUser(meetingID);
//        Map<String, Object> map = new HashMap<>();
//        List<List<Object>> rMean = new ArrayList<>();
//        List<List<Object>> rUser00 = new ArrayList<>();
//        List<List<Object>> rUser01 = new ArrayList<>();
//        List<List<Object>> rUser10 = new ArrayList<>();
//        for(RMean r : mean){
//            rMean.add(Arrays.asList(r.getTime_ms(),r.getR_mean()));
//        }
//        for(AllUser r : user){
//            rUser00.add(Arrays.asList(r.getTime_ms(),r.getUser00()));
//            rUser01.add(Arrays.asList(r.getTime_ms(),r.getUser10()));
//            rUser10.add(Arrays.asList(r.getTime_ms(),r.getUser10()));
//        }
//        map.put("rppg_mean",rMean);
//        map.put("user00",rUser00);
//        map.put("user01",rUser01);
//        map.put("user10",rUser10);
//        return RestResult.success().data(map);
    }

    @ApiOperation("GetAsync")
    @GetMapping("/async")
    @ResponseBody
    public RestResult getASync(@RequestParam("meetingID") Long meetingID){
        List<SyncA> sync = aSyncService.findSync(meetingID);
        List<List<Object>> aSync = new ArrayList<>();
        for(SyncA a : sync){
            aSync.add(Arrays.asList(a.getStart_time(),a.getA_sync()));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("a_sync",aSync);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetVsync")
    @GetMapping("/vsync")
    @ResponseBody
    public RestResult getVSync(@RequestParam("meetingID") Long meetingID){
        List<SyncV> sync = vSyncService.findSync(meetingID);
        List<List<Object>> vSync = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (SyncV v : sync){
            vSync.add(Arrays.asList(v.getStart_time(),v.getV_sync()));
        }
        map.put("v_sync",vSync);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetRppgSync")
    @GetMapping("/rsync")
    @ResponseBody
    public RestResult getRSync(@RequestParam("meetingID") Long meetingID){
        List<SyncR> sync = rSyncService.findSync(meetingID);
        List<List<Object>> rSync = new ArrayList<>();
        for(SyncR r : sync){
            rSync.add(Arrays.asList(r.getStart_time(),r.getR_sync()));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("r_sync",rSync);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetScore")
    @GetMapping("/score")
    @ResponseBody
    public RestResult getScore(@RequestParam("meetingID") Long meetingID){
        Score score = meetingService.getScore(meetingID);
        Map<String, Object> map = new HashMap<>();
        map.put("behavior_score",score.getBehavior_score());
        map.put("body_score",score.getBody_score());
        map.put("total_score",score.getTotal_score());
        map.put("meetingStartTime",score.getMeeting_start_time());
        map.put("duration",score.getDuration());
        map.put("brain_score", score.getBrain_score());
        return RestResult.success().data(map);
    }

    @ApiOperation("Get Latest Five Score")
    @GetMapping("/latest5score/{teamID}")
    @ResponseBody
    public RestResult getLatestFiveScore(@PathVariable("teamID") Long teamID){
        //条件： 1.is_Handle == 1
        //      2.按照时间倒序
//        Score score = meetingService.getScore(teamID);
//        Map<String, Object> map = new HashMap<>();
//        map.put("behavior_score",score.getBehavior_score());
//        map.put("body_score",score.getBody_score());
//        map.put("total_score",score.getTotal_score());
//        map.put("meetingStartTime",score.getMeeting_start_time());
//        map.put("duration",score.getDuration());
//        map.put("brain_score",null);
//        return RestResult.success().data(map);
        return meetingService.getLatestFiveScore(teamID);
    }

    @ApiOperation("Get Latest Five Radar")
    @GetMapping("/latest5radar/{teamID}")
    @ResponseBody
    public RestResult getLatestFiveRadar(@PathVariable("teamID") Long teamID){
        return radarService.getLatestFiveRadar(teamID);
    }

    @ApiOperation("GetWordCount")
    @GetMapping("/wordcount/{meetingID}")
    @ResponseBody
    public RestResult getSpeakerWordCount(@PathVariable("meetingID")Long meetingID){
        return nlpWordCountService.getWordCountByMeetingID(meetingID);
    }

    @ApiOperation("GetHeatmap")
    @GetMapping("/heatmap")
    @ResponseBody
    public RestResult getHeatmap(@RequestParam("meetingID") Long meetingID){
        List<HeatmapVO> heatmap = heatmapService.findHeatmap(meetingID);
        List<List<Integer>> res = new ArrayList<>();
        for (HeatmapVO heatmapVO : heatmap) {
            res.add(Arrays.asList(heatmapVO.getX(),heatmapVO.getY(),heatmapVO.getImg()));
        }

        int maxImg = heatmap.stream().mapToInt(HeatmapVO::getImg).max().orElse(0);
        List<HeatmapVO> maxPoints = new ArrayList<>();
        for (HeatmapVO vo : heatmap) {
            if (vo.getImg() == maxImg) {
                maxPoints.add(vo);
            }
        }

        // 计算平均x和y
        double avgX = maxPoints.stream().mapToInt(HeatmapVO::getX).average().orElse(0);
        double avgY = maxPoints.stream().mapToInt(HeatmapVO::getY).average().orElse(0);


        int xMax = heatmap.stream().mapToInt(HeatmapVO::getX).max().orElse(150);
        int xMin = heatmap.stream().mapToInt(HeatmapVO::getX).min().orElse(150);
        int yMin = heatmap.stream().mapToInt(HeatmapVO::getY).min().orElse(150);
        int yMax = heatmap.stream().mapToInt(HeatmapVO::getY).max().orElse(150);
        Map<String, Object> map = new HashMap<>();
        map.put("xMin", xMin);
        map.put("xMax", xMax);
        map.put("yMin", yMin);
        map.put("yMax", yMax);
        // 将平均x和y转换为整数并返回
        List<Integer> averagePoint = Arrays.asList((int) avgX, (int) avgY, maxImg);
        map.put("heatmap", res);
        map.put("heat_point", averagePoint);

        return RestResult.success().data(map);
    }

    @ApiOperation("GetNlpData")
    @GetMapping("/nlp")
    @ResponseBody
    public RestResult getNlp(@RequestParam("meetingID")Long meetingID){
        List<NlpVO> nlpVOS = nlpService.findNlp(meetingID);
        List<NlpRateVO> speaker = pieSpeakerService.findSpeakerRate(meetingID);
        Map<String,Object> map = new HashMap<>();
        Map<String,Double> speaker_rate = new HashMap<>();
        for(NlpRateVO n : speaker){
            speaker_rate.put(n.getSpeaker(),n.getSpeaker_time_rate());
        }
        List<String> emotion = new ArrayList<>();
        List<String> dialogueAct = new ArrayList<>();
        emotion.add("neutral");
        emotion.add("positive");
        emotion.add("negative");
        dialogueAct.add("Statement-non-opinion");
        dialogueAct.add("Statement-opinion");
        dialogueAct.add("Collaborative Completion");
        dialogueAct.add("Abandoned or Turn-Exit");
        dialogueAct.add("Uninterpretable");
        dialogueAct.add("Yes-No-Question");
        dialogueAct.add("Others");
        map.put("Emotion",emotion);
        map.put("DialogueAct",dialogueAct);
        map.put("data",nlpVOS);
        map.put("speakers",speaker_rate);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetPieData")
    @GetMapping("/pie")
    @ResponseBody
    public RestResult getPie(@RequestParam("meetingID")Long meetingID){
        List<PieSpeakerVO> speaker = pieSpeakerService.findSpeaker(meetingID);
        List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingID);
        List<PieActVO> act = pieActService.findAct(meetingID);
        Map<String,Object> map = new HashMap<>();
        map.put("pie_speakers",speaker);
        map.put("pie_emotions",emotion);
        map.put("pie_acts",act);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetBarData")
    @GetMapping("/bar")
    @ResponseBody
    public RestResult getBar(@RequestParam("meetingID")Long meetingID){
        List<BarSpeakerVO> barSpeaker = pieSpeakerService.findBarSpeaker(meetingID);
        List<BarEmotionVO> emotion = barEmotionService.findEmotion(meetingID);

        Map<String,Object> map = new HashMap<>();
        Map<String,Map<String,Double>> m = new HashMap<>();
        for(BarSpeakerVO b : barSpeaker){
            Map<String,Double> emo = new HashMap<>();
            emo.put("negative",b.getNegative());
            emo.put("neutral",b.getNeutral());
            emo.put("positive",b.getPositive());
            m.put(b.getSpeaker(),emo);
        }

        Map<String,Map<String,Double>> e = new HashMap<>();
        Map<String,Double> neg = new HashMap<>();
        Map<String,Double> neu = new HashMap<>();
        Map<String,Double> pos = new HashMap<>();
        for(BarEmotionVO b : emotion){
            if(b.getEmotion().equals("negative")){
                neg.put(b.getUsers(),b.getScore());
            } else if (b.getEmotion().equals("neutral")) {
                neu.put(b.getUsers(),b.getScore());
            } else if (b.getEmotion().equals("positive")) {
                pos.put(b.getUsers(),b.getScore());
            }
        }
        e.put("negative",neg);
        e.put("neutral",neu);
        e.put("positive",pos);

        map.put("stacked_bar_speakers",m);
        map.put("stacked_bar_emotions",e);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetRadarData")
    @GetMapping("/radar")
    @ResponseBody
    public RestResult getRadar(@RequestParam("meetingID")Long meetingID){
        List<RadarVO> kv = radarService.findKV(meetingID);
        Map<String,Object> map = new HashMap<>();
        map.put("radar",kv);
        return RestResult.success().data(map);
    }

    @ApiOperation("GetSectionData")
    @GetMapping("/section")
    @ResponseBody
    public RestResult getSection(@RequestParam("meetingID")Long meetingID){
        Map<String, List<SectionVO>> map = sectionService.queryDataByMeetingId(meetingID);
        /*List<SectionTeamVO> team = sectionService.findTeam(meetingID);
        List<SectionUserVO> user = sectionService.findUser(meetingID);

        List<HighlightStatement> teamHighlightStatement = highlightStatementService.getHighlightStatementByTag(0);
        List<HighlightStatement> userHighlightStatement = highlightStatementService.getHighlightStatementByTag(1);
        HashMap<Integer,String> teamStatements = new HashMap<>();
        HashMap<Integer,String> userStatements = new HashMap<>();
        for (HighlightStatement highlightStatement : teamHighlightStatement) {
            teamStatements.put(highlightStatement.getLabel(), highlightStatement.getStatement());
        }
        for (HighlightStatement highlightStatement : userHighlightStatement) {
            userStatements.put(highlightStatement.getLabel(), highlightStatement.getStatement());
        }

        for (SectionTeamVO sectionTeamVO : team) {
            sectionTeamVO.setInsight(SectionTeamInsightEnum.getSentenceFromLabel(sectionTeamVO.getLabel()));
            sectionTeamVO.setHighlight_statement(teamStatements.get(sectionTeamVO.getLabel()));
        }

        for (SectionUserVO sectionUserVO : user) {
            sectionUserVO.setInsight(SectionUserInsightEnum.getSentenceFromLabel(sectionUserVO.getLabel()));
            sectionUserVO.setHighlight_statement(userStatements.get(sectionUserVO.getLabel()));
        }

        Map<String,List<SectionUserVO>> users = user.stream().collect(Collectors.groupingBy(SectionUserVO::getUsers));
        Map<String,Object> map = new HashMap<>();
        map.put("team", team);
        map.put("user", users);*/
        return RestResult.success().data(map);
    }

    @ApiOperation("GetVaData")
    @GetMapping("/va")
    @ResponseBody
    public RestResult getVa(@RequestParam("meetingID")Long meetingID){
        List<Long> timeline = aResultService.findTime(meetingID);
        List<DataAVO> data_a = aResultService.findData(meetingID);
        List<DataVVO> data_v = vResultService.findData(meetingID);
        List<String> userList = userAvatarService.findUsers(meetingID);
        List<List<List<Double>>> data = new ArrayList<>();
        int len = Math.min(data_a.size(), data_v.size());
        for(int i = 0; i < len; i++){
            List<List<Double>> va = new ArrayList<>();
//            List<Double> t1 = new ArrayList<>();
            Double a_mean = data_a.get(i).getA_mean();
            Double a_std = data_a.get(i).getA_std() == null ? 0d : data_a.get(i).getA_std();
            Double v_mean = data_v.get(i).getV_mean();
            Double v_std = data_v.get(i).getV_std() == null ? 0d : data_v.get(i).getV_std();
//            t1.add(Arrays.asList(v_mean,a_mean,v_std,a_std));
//            t1.add(v_mean);
//            t1.add(a_mean);
//            t1.add(v_std);
//            t1.add(a_std);
            va.add(Arrays.asList(v_mean,a_mean,v_std,a_std));

            DataAVO dataAVO = data_a.get(i);
            DataVVO dataVVO = data_v.get(i);
            for (int j = 0; j < userList.size(); j++) {
                switch (userList.get(j)){
                    case "user00" :
                        va.add(Arrays.asList(dataVVO.getUser00(),dataAVO.getUser00(),0d,0d));
                        break;
                    case "user01" :
                        va.add(Arrays.asList(dataVVO.getUser01(),dataAVO.getUser01(),0d,0d));
                        break;
                    case "user02" :
                        va.add(Arrays.asList(dataVVO.getUser02(),dataAVO.getUser02(),0d,0d));
                        break;
                    case "user03" :
                        va.add(Arrays.asList(dataVVO.getUser03(),dataAVO.getUser03(),0d,0d));
                        break;
                    case "user04" :
                        va.add(Arrays.asList(dataVVO.getUser04(),dataAVO.getUser04(),0d,0d));
                        break;
                    case "user05" :
                        va.add(Arrays.asList(dataVVO.getUser05(),dataAVO.getUser05(),0d,0d));
                        break;
                    case "user06" :
                        va.add(Arrays.asList(dataVVO.getUser06(),dataAVO.getUser06(),0d,0d));
                        break;
                    case "user07" :
                        va.add(Arrays.asList(dataVVO.getUser07(),dataAVO.getUser07(),0d,0d));
                        break;
                    case "user08" :
                        va.add(Arrays.asList(dataVVO.getUser08(),dataAVO.getUser08(),0d,0d));
                        break;
                    case "user09" :
                        va.add(Arrays.asList(dataVVO.getUser09(),dataAVO.getUser09(),0d,0d));
                        break;
                    case "user10" :
                        va.add(Arrays.asList(dataVVO.getUser10(),dataAVO.getUser10(),0d,0d));
                        break;
                    case "user11" :
                        va.add(Arrays.asList(dataVVO.getUser11(),dataAVO.getUser11(),0d,0d));
                        break;
                    case "user12" :
                        va.add(Arrays.asList(dataVVO.getUser12(),dataAVO.getUser12(),0d,0d));
                        break;
                    case "user13" :
                        va.add(Arrays.asList(dataVVO.getUser13(),dataAVO.getUser13(),0d,0d));
                        break;
                    case "user14" :
                        va.add(Arrays.asList(dataVVO.getUser14(),dataAVO.getUser14(),0d,0d));
                        break;
                    case "user15" :
                        va.add(Arrays.asList(dataVVO.getUser15(),dataAVO.getUser15(),0d,0d));
                        break;
                    case "user90" :
                        va.add(Arrays.asList(dataVVO.getUser90(),dataAVO.getUser90(),0d,0d));
                        break;
                    case "user91" :
                        va.add(Arrays.asList(dataVVO.getUser91(),dataAVO.getUser91(),0d,0d));
                        break;
                    case "user92" :
                        va.add(Arrays.asList(dataVVO.getUser92(),dataAVO.getUser92(),0d,0d));
                        break;
                    case "user93" :
                        va.add(Arrays.asList(dataVVO.getUser93(),dataAVO.getUser93(),0d,0d));
                        break;
                    case "user94" :
                        va.add(Arrays.asList(dataVVO.getUser94(),dataAVO.getUser94(),0d,0d));
                        break;
                    case "user95" :
                        va.add(Arrays.asList(dataVVO.getUser95(),dataAVO.getUser95(),0d,0d));
                        break;
                    case "user96" :
                        va.add(Arrays.asList(dataVVO.getUser96(),dataAVO.getUser96(),0d,0d));
                        break;
                    case "user97" :
                        va.add(Arrays.asList(dataVVO.getUser97(),dataAVO.getUser97(),0d,0d));
                        break;
                    case "user98" :
                        va.add(Arrays.asList(dataVVO.getUser98(),dataAVO.getUser98(),0d,0d));
                        break;
                    case "user99" :
                        va.add(Arrays.asList(dataVVO.getUser99(),dataAVO.getUser99(),0d,0d));
                        break;
                }
            }

            data.add(va);

//            List<Double> t2 = new ArrayList<>();
//            List<Double> t3 = new ArrayList<>();
//            List<Double> t4 = new ArrayList<>();
//            Double a_user00 = data_a.get(i).getUser00();
//            Double a_user01 = data_a.get(i).getUser01();
//            Double a_user10 = data_a.get(i).getUser10();
//            Double v_user00 = data_v.get(i).getUser00();
//            Double v_user01 = data_v.get(i).getUser01();
//            Double v_user10 = data_v.get(i).getUser10();
//            t2.add(v_user00);t2.add(a_user00);t2.add(0d);t2.add(0d);
//            t3.add(v_user01);t3.add(a_user01);t3.add(0d);t3.add(0d);
//            t4.add(v_user10);t4.add(a_user10);t4.add(0d);t4.add(0d);
//            va.add(t2);va.add(t3);va.add(t4);
        }

        Map<String,Object> map = new HashMap<>();
        map.put("timeline",timeline);
        map.put("userList",userList);
        map.put("data",data);
        return RestResult.success().data(map);
    }

    @ApiOperation("UpdateMeetingStartTime")
    @PostMapping("/updatetime/{meetingID}/{date}")
    @ResponseBody
    public RestResult updateStartTime(@PathVariable("meetingID")Long meetingID,@PathVariable("date")Long date){
//        System.out.println(System.currentTimeMillis());
        meetingService.updateMeetingStartTime(meetingID,date);
        return RestResult.success();
    }

    @ApiOperation("IndividualA")
    @GetMapping("/individuala/{meetingID}")
    public RestResult getIndividualA(@PathVariable("meetingID")Long meetingID){
        List<Double> timeLine = individualAService.findTimeLine(meetingID);
        List<String> userList = individualAService.findUserList(meetingID);
        HashMap<String,Object> res = new HashMap<>();
        HashMap<String,List<List<Double>>> individual_sync_value = new HashMap<>();
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        HashMap<String,List<List<Double>>> rate = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualVO> individualByUser = individualAService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            List<List<Double>> l2 = new ArrayList<>();
            List<List<Double>> l3 = new ArrayList<>();
            for (IndividualVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_sync()));
                l2.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_distance()));
                l3.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_rate()));
            }
            individual_sync_value.put(userList.get(i),l1);
            distance.put(userList.get(i),l2);
            rate.put(userList.get(i),l3);

        }

        res.put("individual_sync_value",individual_sync_value);
        res.put("distance",distance);
        res.put("rate",rate);
        res.put("time",timeLine.get(timeLine.size()-1));
        return RestResult.success().data(res);
    }

    @ApiOperation("IndividualV")
    @GetMapping("/individualv/{meetingID}")
    public RestResult getIndividualV(@PathVariable("meetingID")Long meetingID){
        List<Double> timeLine = individualVService.findTimeLine(meetingID);
        List<String> userList = individualVService.findUserList(meetingID);
        HashMap<String,Object> res = new HashMap<>();
        HashMap<String,List<List<Double>>> individual_sync_value = new HashMap<>();
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        HashMap<String,List<List<Double>>> rate = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualVO> individualByUser = individualVService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            List<List<Double>> l2 = new ArrayList<>();
            List<List<Double>> l3 = new ArrayList<>();
            for (IndividualVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_sync()));
                l2.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_distance()));
                l3.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_rate()));
            }
            individual_sync_value.put(userList.get(i),l1);
            distance.put(userList.get(i),l2);
            rate.put(userList.get(i),l3);

        }

        res.put("individual_sync_value",individual_sync_value);
        res.put("distance",distance);
        res.put("rate",rate);
        res.put("time",timeLine.get(timeLine.size()-1));
        return RestResult.success().data(res);
    }

    @ApiOperation("IndividualR")
    @GetMapping("/individualr/{meetingID}")
    public RestResult getIndividualR(@PathVariable("meetingID")Long meetingID){
        List<Double> timeLine = individualRService.findTimeLine(meetingID);
        List<String> userList = individualRService.findUserList(meetingID);
        HashMap<String,Object> res = new HashMap<>();
        HashMap<String,List<List<Double>>> individual_sync_value = new HashMap<>();
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        HashMap<String,List<List<Double>>> rate = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualVO> individualByUser = individualRService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            List<List<Double>> l2 = new ArrayList<>();
            List<List<Double>> l3 = new ArrayList<>();
            for (IndividualVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_sync()));
                l2.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_distance()));
                l3.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_rate()));
            }
            individual_sync_value.put(userList.get(i),l1);
            distance.put(userList.get(i),l2);
            rate.put(userList.get(i),l3);

        }

        res.put("individual_sync_value",individual_sync_value);
        res.put("distance",distance);
        res.put("rate",rate);
        res.put("time",timeLine.get(timeLine.size()-1));
        return RestResult.success().data(res);
    }

    @ApiOperation("Individual contribution")
    @GetMapping("/individual/{meetingID}")
    public RestResult getIndividual(@PathVariable("meetingID")Long meetingID){
        List<String> userList = individualSyncService.findUserList(meetingID);

        HashMap<String,Object> res = new HashMap<>();
        HashMap<String,List<List<Double>>> individual_sync_value = new HashMap<>();
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualAllVO> individualByUser = individualSyncService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            List<List<Double>> l2 = new ArrayList<>();
            for (IndividualAllVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_sync()));
                l2.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_distance()));
            }
            individual_sync_value.put(userList.get(i),l1);
            distance.put(userList.get(i),l2);

        }

        res.put("individual_sync_value",individual_sync_value);
        res.put("distance",distance);

        return RestResult.success().data(res);
    }

    @ApiOperation("Get individual sync")
    @GetMapping("/individual-sync/{meetingID}")
    public RestResult getIndividualSync(@PathVariable("meetingID")Long meetingID) {
        //TODO: 展示individual
        List<String> userList = individualSyncService.findUserList(meetingID);
        HashMap<String,Object> res = new HashMap<>();
        HashMap<String,List<List<Double>>> individual_sync_value = new HashMap<>();
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualAllVO> individualByUser = individualSyncService.findIndividualByUser(meetingID, userList.get(i));
            List<IndividualVO> individual = individualAService.findIndividualByUser(meetingID, userList.get(i));
            List<IndividualVO> individual1 = individualVService.findIndividualByUser(meetingID, userList.get(i));
            List<IndividualVO> individual2 = individualRService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            List<List<Double>> l2 = new ArrayList<>();
            for (IndividualAllVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_sync()));
                l2.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getIndividual_distance()));
            }
            individual_sync_value.put(userList.get(i),l1);
            distance.put(userList.get(i),l2);
        }

        res.put("individual_sync_value",individual_sync_value);
        res.put("distance",distance);

        return RestResult.success().data(res);
    }

    @ApiOperation("Ave Sync")
    @GetMapping("/avesync/{meetingID}")
    public RestResult getAveSync(@PathVariable("meetingID")Long meetingID){
//        aveSyncService
        List<AveSyncVO> aveSyncVOS = aveSyncService.findAveSync(meetingID);
        HashMap<String,List<List<Double>>> data = new HashMap<>();
        List<List<Double>> ave = new ArrayList<>();
        for (AveSyncVO aveSyncVO : aveSyncVOS) {
            ave.add(Arrays.asList(aveSyncVO.getTime_ms(),aveSyncVO.getAve()));
        }
        data.put("ave_sync",ave);
        return RestResult.success().data(data);
    }

    @ApiOperation("Get above Sync")
    @GetMapping("/above-sync/{meetingID}")
    public RestResult getAboveSync(@PathVariable("meetingID")Long meetingID,
                                   @RequestParam(value = "threshold") Double threshold){
//        aveSyncService
        List<AveSyncVO> aveSyncVOS = aveSyncService.findAveSync(meetingID);
        List<AveSyncVO> filteredList = aveSyncVOS.stream()
                .filter(aveSyncVO -> aveSyncVO.getAve() >= threshold)
                .collect(Collectors.toList());
        HashMap<String,List<List<Double>>> data = new HashMap<>();
        List<List<Double>> ave = new ArrayList<>();
        for (AveSyncVO aveSyncVO : filteredList) {
            ave.add(Arrays.asList(aveSyncVO.getTime_ms(),aveSyncVO.getAve()));
        }
        data.put("above_sync",ave);
        return RestResult.success().data(data);
    }

    @ApiOperation("Team and User distance")
    @GetMapping("/sync/{meetingID}")
    public RestResult getTeamAndUserSync(@PathVariable("meetingID")Long meetingID){
        HashMap<String,Object> ans = new HashMap<>();
        TeamSyncVO teamSyncVO = meetingService.findTeamSync(meetingID);
        ans.put("team",teamSyncVO);

        List<String> userList = individualSyncService.findUserList(meetingID);
        HashMap<String,List<List<Double>>> distance = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            List<IndividualAllVO> individualByUser = individualSyncService.findIndividualByUser(meetingID, userList.get(i));
            List<List<Double>> l1 = new ArrayList<>();
            for (IndividualAllVO individualVO : individualByUser) {
                l1.add(Arrays.asList(individualVO.getTime_ms(),individualVO.getDistance()));
            }
            distance.put(userList.get(i),l1);
        }

        LambdaQueryWrapper<UserDistance> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDistance::getMeeting_id,meetingID).orderByAsc(UserDistance::getTime_ms);
        List<UserDistance> list = userDistanceService.list(lambdaQueryWrapper);
        List<List<Number>> user = new ArrayList<>();
        for (UserDistance userDistance : list) {
            user.add(Arrays.asList(userDistance.getTime_ms(),userDistance.getLabel()));
        }
        ans.put("user",distance);
        ans.put("label",user);

        return RestResult.success().data(ans);
    }

    @ApiOperation("Individual Score")
    @GetMapping("/individualscore/{meetingID}")
    public RestResult getIndividualScore(@PathVariable("meetingID")Long meetingID){
        return individualScoreService.getUserScore(meetingID);
    }

    @ApiOperation("Get Emoji")
    @GetMapping("/emoji/{meetingID}")
    public RestResult getEmoji(@PathVariable("meetingID")Long meetingID){
        return emojiService.getEmojiByMeetingId(meetingID);
    }

    @ApiOperation("Get Positive And Negative Rate")
    @GetMapping("/positive-negative-rate/{meetingID}")
    public RestResult getPositiveAndNegativeRate(@PathVariable("meetingID") Long meetingID){
        return posAndNegRateService.getPositiveAndNegativeRateByMeetingId(meetingID);
    }

    @ApiOperation("Get Synchrony Moment")
    @GetMapping("/synchrony-moment/{meetingID}")
    public RestResult getSynchronyMoment(@PathVariable("meetingID")Long meetingID){
        List<SynchronyMomentVO> synchronyMomentVOList = synchronyMomentService.getSynchronyMomentVOByMeetingId(meetingID)
                .stream()
                .map(
                        e -> {
                            e.setStarts(e.getStarts() / 1000);
                            e.setEnds(e.getEnds() / 1000);
                            return e;
                        }
                ).collect(Collectors.toList());
        return RestResult.success().data(synchronyMomentVOList);
    }

    @ApiOperation("Get My Contribution")
    @GetMapping("/contribution/{meetingID}")
    public RestResult UserContribution(@PathVariable("meetingID")Long meetingID){
        return userContributionService.getUserContributionByMeetingId(meetingID);
    }

    @ApiOperation("Get Universe Group Emoji")
    @GetMapping("/universe-group-emoji/{meetingID}")
    public RestResult getUniverseGroupEmoji(@PathVariable("meetingID")Long meetingID){
        return universeGroupService.getUniverseGroupEmojiByMeetingId(meetingID);
    }

    @ApiOperation("Get Universe Group Meter")
    @GetMapping("/universe-group-meter/{meetingID}")
    public RestResult getUniverseGroupMeter(@PathVariable("meetingID")Long meetingID){
        List<AveSync> universeGroupMeter = aveSyncService.findUniverseGroupMeter(meetingID);
        HashMap<String,List<List<Double>>> data = new HashMap<>();
        List<List<Double>> universeGroupMeterList = new ArrayList<>();
        for (AveSync universeGroup : universeGroupMeter) {
            universeGroupMeterList.add(Arrays.asList(universeGroup.getTime_ms(), universeGroup.getCurrent(), universeGroup.getAcc_average()));
        }
        data.put("current_and_average", universeGroupMeterList);
        return RestResult.success().data(data);
    }

    @ApiOperation("Get Word Rate")
    @GetMapping("/word-rate/{meetingID}")
    public RestResult getWordRate(@PathVariable("meetingID")Long meetingID){
        Map<Integer, List<WordRateVO>> data = wordRateService.queryDataByMeetingID(meetingID);
        Map<String, Double> aveMap = calculateAverageRates(data);
        Map<Object, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("ave", aveMap);
        return RestResult.success().data(result);
    }

    private Map<String, Double> calculateAverageRates(Map<Integer, List<WordRateVO>> data) {
        // 合并所有List
        List<WordRateVO> allWordRates = data.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 计算平均值
        Map<String, Double> averageRates = allWordRates.stream()
                .collect(Collectors.groupingBy(WordRateVO::getName,
                        Collectors.averagingDouble(WordRateVO::getRate)));

        return averageRates;
    }
}
