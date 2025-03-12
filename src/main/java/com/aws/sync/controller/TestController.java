package com.aws.sync.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.entity.*;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.mapper.NlpMapper;
import com.aws.sync.service.*;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.utils.NlpUtil;
import com.aws.sync.utils.SSLUtilities;
import com.aws.sync.vo.GptRequestInfoVO;
import com.aws.sync.vo.MeetingRadarVO;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.aws.sync.constants.CsvConstants.WINDOW_LENGTH_MS;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private AmazonUploadService amazonUploadService;

    @Autowired
    private NlpService nlpService;

    @Autowired
    private CVHandleService cvHandleService;

    @Autowired
    GptService gptService;

    @Autowired
    RestTemplate restTemplate;

    @Resource
    NlpMapper nlpMapper;

    @Autowired
    WordRateService wordRateService;

    @Autowired
    SynchronyMomentService synchronyMomentService;

    @Autowired
    DetectionRadarService detectionRadarService;

    @Autowired
    SummaryService summaryService;

    @Autowired
    AResultService aResultService;

    @Autowired
    VResultService vResultService;


    @PostMapping("/gitlab1/{meetingId}")
    public RestResult testGitLabVideo(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testGitLabVideo] meetingId :{}", meetingId);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        if (meetingTable == null) {
            return RestResult.fail().message("meetingId not exist");
        }
        String video_url = meetingTable.getVideo_url();
        String regex = "https?://[^/]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(video_url);

        String fileKey;
        if (matcher.find()) {
            fileKey = matcher.group(1); // 返回第一个捕获组的内容
        } else {
            return RestResult.fail().message("invalid meetingId");
        }
        redisTemplate.opsForZSet().add("SingleModelPreProcess","meeting" + meetingTable.getMeeting_id() + ":" + fileKey + ":rppgCompare!" + 0.6, System.currentTimeMillis());
//        redisTemplate.opsForZSet().add("pose-estimation","meeting" + meetingId + ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
//        redisTemplate.opsForZSet().add("emotion-detector","meeting" + meetingId + ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
//        redisTemplate.opsForZSet().add("eye-blinking-detection","meeting" + meetingId+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        return RestResult.success();
    }

    @PostMapping("/eye-blinking-detection/{meetingId}")
    public RestResult testPushRedisForVirajModels(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testPushRedisForVirajModels] meetingId :{}", meetingId);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        if (meetingTable == null) {
            return RestResult.fail().message("meetingId not exist");
        }
        String video_url = meetingTable.getVideo_url();
        String regex = "https?://[^/]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(video_url);

        String fileKey;
        if (matcher.find()) {
            fileKey = matcher.group(1); // 返回第一个捕获组的内容
        } else {
            return RestResult.fail().message("invalid meetingId");
        }
        //redisTemplate.opsForZSet().add("SingleModelPreProcess","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":rppgCompare!" + 0.6, System.currentTimeMillis());
        //redisTemplate.opsForZSet().add("video-speaker-detector","meeting" + meetingId+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        //redisTemplate.opsForZSet().add("pose-estimation","meeting" + meetingId + ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        //redisTemplate.opsForZSet().add("emotion-detector","meeting" + meetingId + ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        redisTemplate.opsForZSet().add("eye-blinking-detection","meeting" + meetingId+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        System.out.println(fileKey);
        return RestResult.success();
    }

    @PostMapping("/test_cv_nlp_models/{meetingId}")
    public RestResult testCVNlpModels(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testCVNlpModels] meetingId :{}", meetingId);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        if (meetingTable == null) {
            return RestResult.fail().message("meetingId not exist");
        }
        String video_url = meetingTable.getVideo_url();
        String regex = "https?://[^/]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(video_url);

        String fileKey;
        if (matcher.find()) {
            fileKey = matcher.group(1); // 返回第一个捕获组的内容
        } else {
            return RestResult.fail().message("invalid meetingId");
        }
        redisTemplate.opsForZSet().add("meeting_test","meeting" + meetingId+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
        System.out.println(fileKey);
        return RestResult.success();
    }

    @PostMapping("/remove_redis_for_viraj_models/")
    public RestResult testRemoveRedisForVirajModels() {
        log.info("[TestController][testRemoveRedisForVirajModels]");
        redisTemplate.delete("video-speaker-detector");
        redisTemplate.delete("pose-estimation");
        redisTemplate.delete("eye-blinking-detection");
        redisTemplate.delete("emotion-detector");
        return RestResult.success();
    }

    /**
     * 计算Brain Score
     * @param meetingId meetingId
     * */
    @PostMapping("/brain-score/{meetingId}")
    public RestResult testBrainScore(@PathVariable("meetingId") Long meetingId) throws IOException {
        try {
            meetingService.handleBrainScore(meetingId);
        } catch (SystemException e) {
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }

    @ApiOperation("Test Score")
    @PostMapping("/test-score/{meetingID}")
    public RestResult testScore(@PathVariable("meetingID")Long meetingID) throws Exception {

        String meeting = Long.toString(meetingID);
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);

        List<String[]> sync_a = new ArrayList<>();
        List<String[]> sync_v= new ArrayList<>();
        List<String[]> sync_r = new ArrayList<>();
        List<IndividualSyncA> isa = new ArrayList<>();
        List<IndividualSyncV> isv = new ArrayList<>();
        List<IndividualSyncR> isr = new ArrayList<>();


        List<Async> listAsync = CsvUtil.get_and_save_sync_a(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_A, dataA, sync_a, meetingID, isa);
        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_V, dataV, sync_v, meetingID, isv);
        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, meetingID, isr);
        //处理score
        Score scores = CsvUtil.get_scores(sync_a, sync_v, sync_r);
        return RestResult.success().data(scores);
    }

    @ApiOperation("remove meeting data")
    @PostMapping("/remove-meeting-data/{meetingId}")
    public RestResult removeMeetingData(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][removeMeetingData] meetingId :{}", meetingId);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        if (meetingTable != null) {
            //删除s3数据 file + video + thumb
            String video_url = meetingTable.getVideo_url();
            String thumbnail = meetingTable.getThumbnail();
            String keyByUrl = getKeyByUrl(video_url);
            String keyByUrl1 = getKeyByUrl(thumbnail);
//            amazonUploadService.deleteFile(getKeyByUrl(keyByUrl));
//            amazonUploadService.deleteFile(getKeyByUrl(keyByUrl1));
            amazonUploadService.deleteFolder("test/meeting" + meetingId);

        }
        meetingService.removeMeetingDataByMeetingId(meetingId);
        return RestResult.success();
    }

    @ApiOperation("remove teams data")
    @PostMapping("/remove-teams-data/{teamId}")
    public RestResult removeTeamData(@PathVariable("teamId")Long teamId) {
        log.info("[TestController][removeTeamData] teamId :{}", teamId);
        List<MeetingTable> meetingTables = meetingService.getAllMeetingByTeamId(teamId);
        for (MeetingTable meetingTable : meetingTables) {
            Long meetingId = meetingTable.getMeeting_id();
            log.info("[TestController][removeTeamData] meetingId :{}", meetingId);
            if (meetingTable != null) {
                //删除s3数据 file + video + thumb
                String video_url = meetingTable.getVideo_url();
                String thumbnail = meetingTable.getThumbnail();
                String keyByUrl = getKeyByUrl(video_url);
                String keyByUrl1 = getKeyByUrl(thumbnail);
//            amazonUploadService.deleteFile(getKeyByUrl(keyByUrl));
//            amazonUploadService.deleteFile(getKeyByUrl(keyByUrl1));
                amazonUploadService.deleteFolder("test/meeting" + meetingId);

            }
            meetingService.removeMeetingDataByMeetingId(meetingId);
        }

        return RestResult.success();
    }

    @ApiOperation("test nlp")
    @PostMapping("/test-nlp/{meetingId}")
    public RestResult testNlp(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testNlp] meetingId :{}", meetingId);
        try {
            nlpService.handleNlp(meetingId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @ApiOperation("test cv")
    @PostMapping("/test-cv/{meetingId}")
    public RestResult testCv(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testCv] meetingId :{}", meetingId);
        try {
            cvHandleService.handleCV(meetingId);
//            //357
//            List<Long> meetings = Arrays.asList(354l,353l);
//            for (Long meeting : meetings) {
//                System.out.println(meeting + "0");
//                meetingService.handleDetection(meeting, CsvConstants.EMOTION_DETECTION, 0);
//                System.out.println(meeting);
//                meetingService.handleDetection(meeting, CsvConstants.POSTURE_DETECTION, 1);
//            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @ApiOperation("recover nlp data")
    @PostMapping("/recover-nlp/{meetingId}")
    public RestResult recoverNlpData(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][recoverNlpData] meetingId :{}", meetingId);
        List<String> userList = new ArrayList<>();
        String fileName = "nlp_result.txt";
        //nlp data handle
        List<String[]> nlp_data = null;
        try {
            nlp_data = amazonUploadService.readNlp(fileName, Long.toString(meetingId), userList);
            List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingId, nlp_data);
            LambdaQueryWrapper<NlpTable> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NlpTable::getMeeting_id, meetingId);
            nlpService.remove(queryWrapper);
            nlpService.insertNlp(nlpTables);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @ApiOperation("test nlp summary")
    @PostMapping("/nlp-summary/{meetingId}")
    public RestResult testNlpSummary(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testNlpSummary] meetingId :{}", meetingId);
        String fileName = "nlp_result.txt";
        try {
            List<String[]> nlpData = amazonUploadService.readNlpLine(fileName, Long.toString(meetingId));
            gptService.processNLPData(nlpData, meetingId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @ApiOperation("test radar trust")
    @PostMapping("/radar-trust/{meetingId}")
    public RestResult testRadarTrust(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testRadarTrust] meetingId :{}", meetingId);
        try {
            gptService.processRadarTrust(meetingId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @PostMapping("/fetch-analysis/{meetingId}")
    public RestResult sendPostRequestAsync(@PathVariable("meetingId")Long meetingId) {
        String url = "https://syneurgy.io/ai/fetch-analysis";
        log.info("[TestController][sendPostRequestAsync] meetingID :{}, url :{}", meetingId, url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"meeting_id\":\"" + meetingId + "\"}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = SSLUtilities.createRestTemplateWithDisabledSSL();
        // 发送POST请求
        String response = restTemplate.postForObject(url, request, String.class);
        return RestResult.success().data(response);
    }
    @ApiOperation("test match")
    @PostMapping("/match/{meetingId}")
    public RestResult testMatch(@PathVariable("meetingId")Long meetingId) {
        log.info("[TestController][testMatch] meetingId :{}", meetingId);
        try {
            meetingService.handleNewMatch(meetingId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return RestResult.success();
    }

    @ApiOperation("test analysis")
    @PostMapping("/analysis/{meetingId}")
    public RestResult testJudgeIfAnalysis(@PathVariable("meetingId")Long meetingId) throws Exception{
        log.info("[TestController][testJudgeIfAnalysis] part :1, meetingId :{}", meetingId);
        nlpService.handleNlp(meetingId);
        cvHandleService.handleCV(meetingId);
        /*MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        String redisValue = "meeting" + meetingTable.getMeeting_id()+ ":" + getKeyByUrl(meetingTable.getVideo_url()) + ":" + 0.6;
        log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] put data to redis, redisValue:{}", redisValue);
        redisTemplate.opsForZSet().add("video-speaker-detector1", redisValue, System.currentTimeMillis());*/
        //TODO: 后期更改为处理完所有
        return RestResult.success();
    }

    @PostMapping("/process-highlight/{meetingId}")
    public RestResult testProcessHighlight(@PathVariable("meetingId") String meetingId) throws Exception{
        log.info("[TestController][testProcessTeamHighlight] meetingId :{}", meetingId);
        List<String[]> nlpData = amazonUploadService.readNlpLine("nlp_result.txt", meetingId);
        gptService.processAllHighlight(nlpData, Long.valueOf(meetingId));
        return RestResult.success();
    }

    @PostMapping("/individual-brain-score/{meetingId}")
    public RestResult testIndividualBrainScore(@PathVariable("meetingId") String meetingID) throws Exception{
        log.info("[TestController][testIndividualBrainScore] meetingId :{}", meetingID);
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meetingID);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meetingID);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meetingID);
        List<String[]> sync_a = new ArrayList<>();
        List<String[]> sync_v= new ArrayList<>();
        List<String[]> sync_r = new ArrayList<>();
        List<IndividualSyncA> isa = new ArrayList<>();
        List<IndividualSyncV> isv = new ArrayList<>();
        List<IndividualSyncR> isr = new ArrayList<>();


        List<Async> listAsync = CsvUtil.get_and_save_sync_a(10000, CsvConstants.CSV_FILE_A, dataA, sync_a, Long.valueOf(meetingID), isa);
        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(10000, CsvConstants.CSV_FILE_V, dataV, sync_v, Long.valueOf(meetingID), isv);
        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, Long.valueOf(meetingID), isr);
        //此处通过rppg求Brain的方式对blink获取brainScore
        List<String[]> dataB = amazonUploadService.readBlinkData(CsvConstants.BLINK_RESULT, Long.valueOf(meetingID));
        List<String[]> sync_b = new ArrayList<>();
        List<IndividualSyncR> isb = new ArrayList<>();
        List<Rsync> listBsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_BLINK, dataB, sync_b, Long.valueOf(meetingID), isb);

        List<IndividualScore> individual_score = CsvUtil.get_individual_score(Long.valueOf(meetingID), isa, isv, isr, isb);
        return RestResult.success().data(individual_score);
    }

    @PostMapping("/generate-sync-file/{meetingId}")
    public RestResult testSync(@PathVariable("meetingId") String meetingID) throws Exception{
        log.info("[TestController][testSync] meetingId :{}", meetingID);
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meetingID);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meetingID);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meetingID);
        List<String[]> sync_a = new ArrayList<>();
        List<String[]> sync_v= new ArrayList<>();
        List<String[]> sync_r = new ArrayList<>();
        List<IndividualSyncA> isa = new ArrayList<>();
        List<IndividualSyncV> isv = new ArrayList<>();
        List<IndividualSyncR> isr = new ArrayList<>();


        List<Async> listAsync = CsvUtil.get_and_save_sync_a(10000, CsvConstants.CSV_FILE_A, dataA, sync_a, Long.valueOf(meetingID), isa);
        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(10000, CsvConstants.CSV_FILE_V, dataV, sync_v, Long.valueOf(meetingID), isv);
        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, Long.valueOf(meetingID), isr);

        return RestResult.success();
    }

    @PostMapping("/compare-time/{meetingId}")
    public RestResult testTime(@PathVariable("meetingId") Long meetingID) throws Exception{
        log.info("[TestController][testTime] meetingId :{}", meetingID);
        String meeting = String.valueOf(meetingID);
        long start1 = System.currentTimeMillis();
        List<String[]> strings = amazonUploadService.readNlpLine("nlp_result.txt", meeting);
        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingID, strings);
        long end1 = System.currentTimeMillis();
        System.out.println(end1 - start1);
        log.info("[TestController][testTime] time1:{}", end1 - start1);
        long start2 = System.currentTimeMillis();
        LambdaQueryWrapper<NlpTable> nlpTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
        nlpTableLambdaQueryWrapper.eq(NlpTable::getMeeting_id, meetingID).orderByAsc(NlpTable::getStarts);
        List<NlpTable> nlpTable = nlpMapper.selectList(nlpTableLambdaQueryWrapper);
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - start2);
        log.info("[TestController][testTime] time2:{}", end2 - start2);
        return RestResult.success();
    }

    @PostMapping("/word-rate/{meetingId}")
    public RestResult testWordRate(@PathVariable("meetingId") Long meetingID) throws IOException {
        log.info("[TestController][testWordRate] meetingId :{}", meetingID);
        List<String[]> nlp_data = amazonUploadService.readNlpLine("nlp_result.txt", Long.toString(meetingID));
        List<WordRate> wordRateList = nlpService.processWordRate(nlp_data, meetingID);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);
        wordRateService.removeByMap(deleteMap);
        wordRateService.saveBatch(wordRateList);
        return RestResult.success();
    }

    @PostMapping("/detection/{meetingId}")
    public RestResult testDetection(@PathVariable("meetingId") Long meetingID) throws IOException {
        log.info("[TestController][testDetection] meetingId :{}", meetingID);
//        nlpService.handleDetectionNlp(meetingID);
        //meetingService.handleDetection(meetingID, CsvConstants.EMOTION_DETECTION, 0);
        meetingService.handlePostureDetection(meetingID, CsvConstants.POSTURE_DETECTION, 1);
        meetingService.processRadar(meetingID);
        return RestResult.success();
    }

    @ApiOperation("Get radar result")
    @GetMapping("/radar/{meetingId}")
    public RestResult getRadarResult(@PathVariable("meetingId") Long meetingId) {
        MeetingRadarVO result = new MeetingRadarVO();
        result.setMeetingId(meetingId);
        List<DetectionVO> detectionRadarList = detectionRadarService.queryDataByMeetingId(meetingId);

        result.setResults(detectionRadarList);
        return RestResult.success().data(result);
    }

    @PostMapping("/synchrony-moment/{meetingId}")
    public RestResult testSynchronyMoment(@PathVariable("meetingId") Long meetingID) throws IOException {
        log.info("[TestController][testSynchronyMoment] meetingId :{}", meetingID);
        synchronyMomentService.saveSmallest3(meetingID);
        return RestResult.success();
    }


    @RequestMapping(value = {"/handle-all/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handleAll(@PathVariable("meetingID")Long meetingID) {
        log.info("[TestController][handleAll] meetingID :{}", meetingID);
        //add one need modify removeCv
        try {
            nlpService.handleNlp(meetingID);
            cvHandleService.handleCV(meetingID);
            MeetingTable meetingTable = meetingService.getByMeetingId(meetingID);
            String redisValue = "meeting" + meetingTable.getMeeting_id()+ ":" + getKeyByUrl(meetingTable.getVideo_url()) + ":" + 0.6;
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] put data to redis, redisValue:{}", redisValue);
            redisTemplate.opsForZSet().add("video-speaker-detector", redisValue, System.currentTimeMillis());
//            nlpService.sendPostRequestAsync("https://syneurgy.io/ai/fetch-analysis", meetingID);
            return RestResult.success();
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    @RequestMapping(value = {"/gpt-summary"}, method = RequestMethod.POST)
    public RestResult testGptSummary(@RequestParam("meetingID")Long meetingID,
                                     @RequestParam("teamId")Long teamId,
                                     @RequestParam(value = "handle", required = true)boolean handle) throws IOException {
        GptRequestInfoVO gptRequestInfoVO = new GptRequestInfoVO();
        String summaryDataByMeetingId = summaryService.getSummaryDataByMeetingId(meetingID);
        gptRequestInfoVO.setMeetingId(meetingID);
        gptRequestInfoVO.setTeamId(teamId);
        gptRequestInfoVO.setTranscript(summaryDataByMeetingId);
        if (handle) {
            nlpService.handleWordInfo(meetingID);
        }
        gptService.processTeamAndMeetingGptData(gptRequestInfoVO);
        return RestResult.success();
    }

    private static final String REDIS_KEY_PREFIX = "meeting:";
    @RequestMapping(value = {"/query"}, method = RequestMethod.GET)
    public RestResult query(@RequestParam("meetingID")Long meetingID) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        long startTime = System.currentTimeMillis(); // 记录开始时间
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meetingID.toString());
        List<String> userList = new ArrayList<>();
//        List<AResult> listA = CsvUtil.read_a(meetingID, dataA, userList);
        /*String redisKey = REDIS_KEY_PREFIX + meetingID;
        List<AResult> records = (List<AResult>) redisTemplate.opsForValue().get(redisKey);
        if (!CollectionUtils.isEmpty(records)) {
            long duration = System.currentTimeMillis() - startTime; // 记录耗时
            System.out.println("Cache hit. Time taken: " + duration + " nanoseconds.");
            return RestResult.success();
        }

        // 如果 Redis 没有命中，则从数据库中查询
        LambdaQueryWrapper<AResult> result = new LambdaQueryWrapper<AResult>()
                .eq(AResult::getMeeting_id, meetingID)
                .orderByAsc(AResult::getTime_ms);
        records = aResultService.list(result);

        // 将结果写入 Redis，并设置过期时间（例如 1 小时）
        if (!CollectionUtils.isEmpty(records)) {
            redisTemplate.opsForValue().set(redisKey, records, 1, TimeUnit.HOURS);
        }*/

        long duration = System.currentTimeMillis() - startTime; // 记录耗时


        System.out.println("Cache miss. Time taken: " + duration + " nanoseconds.");
        return RestResult.success().data("");
    }


    private String getKeyByUrl(String url) {
        String regex = "https?://[^/]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        String fileKey = null;
        if (matcher.find()) {
            fileKey = matcher.group(1); // 返回第一个捕获组的内容
        }
        return fileKey;
    }




}