package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.entity.AnalysisInfo;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.VideoAnalysisTable;
import com.aws.sync.mapper.AnalysisMapper;
import com.aws.sync.mapper.VideoAnalysisMapper;
import com.aws.sync.service.*;
import com.aws.sync.vo.GptRequestInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aws.sync.constants.CsvConstants.*;

@Slf4j
@Service
public class AnalysisServiceImpl extends ServiceImpl<AnalysisMapper, AnalysisInfo> implements AnalysisService {

    @Autowired
    MeetingService meetingService;

    @Autowired
    NlpService nlpService;

    @Autowired
    CVHandleService cvHandleService;

    @Autowired
    GptService gptService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    VideoAnalysisMapper videoAnalysisMapper;

    @Autowired
    SectionService sectionService;

    @Autowired
    WebClient.Builder webClientBuilder;

    @Override
    public RestResult saveAnalysisInfo(Long meetingId, HashMap<String, List<Long>> time) {
        List<AnalysisInfo> analysisInfoList = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : time.entrySet()) {
            String key = entry.getKey();
            List<Long> value = entry.getValue();
            if (value.size() == 2) {
                analysisInfoList.add(new AnalysisInfo(meetingId, key, value.get(0), value.get(1)));
            }
        }
        saveBatch(analysisInfoList);
        return RestResult.success();
    }

    @Override
    @Async
    public void judgeIfAnalysisAsync(VideoAnalysisTable videoAnalysisTable, String currentFile) throws Exception {
        LambdaUpdateWrapper<VideoAnalysisTable>  videoAnalysisTableLambdaUpdateWrapper = new LambdaUpdateWrapper<VideoAnalysisTable>()
                .eq(VideoAnalysisTable::getMeeting_id, videoAnalysisTable.getMeeting_id());
        log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] videoAnalysisTable :{}, currentFile :{}", videoAnalysisTable, currentFile);
        if ("COMPLETED".equals(videoAnalysisTable.getEmotion_detection())
                && "COMPLETED".equals(videoAnalysisTable.getAnchor_status())
                && (EMOTION_DETECTION.equals(currentFile) || ANCHOR_RESULT.equals(currentFile))) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part 1, meetingId:{}", videoAnalysisTable.getMeeting_id());
            meetingService.handleDetection(videoAnalysisTable.getMeeting_id(), CsvConstants.EMOTION_DETECTION, 0);
            videoAnalysisTableLambdaUpdateWrapper.set(VideoAnalysisTable::getDetectionCV0, "COMPLETED");
            videoAnalysisMapper.update(null, videoAnalysisTableLambdaUpdateWrapper);
        }
        if ("COMPLETED".equals(videoAnalysisTable.getPosture_detection())
                && (POSTURE_DETECTION.equals(currentFile))) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part 2, meetingId:{}", videoAnalysisTable.getMeeting_id());
            meetingService.handlePostureDetection(videoAnalysisTable.getMeeting_id(), CsvConstants.POSTURE_DETECTION, 1);
            videoAnalysisTableLambdaUpdateWrapper.set(VideoAnalysisTable::getDetectionCV1, "COMPLETED");
            videoAnalysisMapper.update(null, videoAnalysisTableLambdaUpdateWrapper);
        }

        if ("COMPLETED".equals(videoAnalysisTable.getBlink_results())
            && BLINK_RESULT.equals(currentFile)) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part 3, meetingId:{}", videoAnalysisTable.getMeeting_id());
            meetingService.handleBrainScore(videoAnalysisTable.getMeeting_id());
        }

        if ("COMPLETED".equals(videoAnalysisTable.getNlp_status())
                && "COMPLETED".equals(videoAnalysisTable.getCv_status())
                && (CV_FILE_COMPLETE.equals(currentFile) || NLP_FILE_NAME.equals(currentFile))) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part :4, meetingId :{}", videoAnalysisTable.getMeeting_id());
            nlpService.handleNlp(videoAnalysisTable.getMeeting_id());
            cvHandleService.handleCV(videoAnalysisTable.getMeeting_id());
            MeetingTable meetingTable = meetingService.getByMeetingId(videoAnalysisTable.getMeeting_id());
            String redisValue = "meeting" + meetingTable.getMeeting_id()+ ":" + getKeyByUrl(meetingTable.getVideo_url()) + ":" + 0.6;
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] put data to redis, redisValue:{}", redisValue);

            redisTemplate.opsForZSet().add("pose-estimation","meeting" + redisValue, System.currentTimeMillis());
            redisTemplate.opsForZSet().add("emotion-detector","meeting" + redisValue, System.currentTimeMillis());
            redisTemplate.opsForZSet().add("eye-blinking-detection","meeting" + redisValue, System.currentTimeMillis());


            redisTemplate.opsForZSet().add("video-speaker-detector", redisValue, System.currentTimeMillis());

//            nlpService.sendPostRequestAsync("https://syneurgy.io/ai/fetch-analysis", videoAnalysisTable.getMeeting_id());
//            nlpService.sendPostRequestAsync("http://18.117.0.146:3000/fetch-analysis", videoAnalysisTable.getMeeting_id());
            //for devServer
            nlpService.sendPostRequestAsync("http://18.117.138.252:3000/fetch-analysis", videoAnalysisTable.getMeeting_id());

//            sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/speaker");
//            sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/blink");

            sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/speaker_staging");
            sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/blink_staging");
            gptService.processRadarTrust(videoAnalysisTable.getMeeting_id());
            GptRequestInfoVO info = new GptRequestInfoVO();
            info.setMeetingId(meetingTable.getMeeting_id());
            info.setTeamId(meetingTable.getTeam_id());
            gptService.processTeamAndMeetingGptData(info);

            sectionService.addAdditionInfoToSection(meetingTable.getMeeting_id());

        }
        if ("COMPLETED".equals(videoAnalysisTable.getNlp_status())
                && "COMPLETED".equals(videoAnalysisTable.getCv_status())
                && "COMPLETED".equals(videoAnalysisTable.getAnchor_status())
                && "COMPLETED".equals(videoAnalysisTable.getActive_speaker_status())
        && (NLP_FILE_NAME.equals(currentFile) || CV_FILE_COMPLETE.equals(currentFile)
                || ANCHOR_RESULT.equals(currentFile) || ACTIVE_SPEAKER_CSV.equals(currentFile))) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part :5, meetingId :{}", videoAnalysisTable.getMeeting_id());
            meetingService.handleNewMatch(videoAnalysisTable.getMeeting_id());
            nlpService.handleDetectionNlp(videoAnalysisTable.getMeeting_id());
            //TODO: 处理UserHighLight
            videoAnalysisTableLambdaUpdateWrapper.set(VideoAnalysisTable::getNlp_and_match, "COMPLETED");
            videoAnalysisMapper.update(null, videoAnalysisTableLambdaUpdateWrapper);
        }
        LambdaQueryWrapper<VideoAnalysisTable> queryWrapper = new LambdaQueryWrapper<VideoAnalysisTable>()
                .eq(VideoAnalysisTable::getMeeting_id, videoAnalysisTable.getMeeting_id());
        VideoAnalysisTable table = videoAnalysisMapper.selectOne(queryWrapper);
        if ("COMPLETED".equals(table.getDetectionCV0()) && "COMPLETED".equals(table.getDetectionCV1()) && "COMPLETED".equals(table.getNlp_and_match())) {
            log.info("[AnalysisServiceImpl][judgeIfAnalysisAsync] part :6, meetingId :{}", videoAnalysisTable.getMeeting_id());
            meetingService.processRadar(table.getMeeting_id());
        }
    }

    private  void sendAsyncRequest(String url) {
        String requestBody = "{}";  // 示例为空的请求体
//        WebClient.Builder builder = WebClient.builder();
        // 异步发送 POST 请求
    /*    webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class) // 解析响应体为 String
                .doOnTerminate(() -> System.out.println("Request completed"))  // 请求完成后执行
                .subscribe(response -> {
                    // 处理响应
                    System.out.println("Response: " + response);
                }, error -> {
                    // 错误处理
                    System.err.println("Error occurred: " + error.getMessage());
                });*/

        webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(5, Duration.ofMinutes(1))
                        .doBeforeRetry(retrySignal ->
                                System.out.println("Retrying after failure. Attempt: " + (retrySignal.totalRetries() + 1))
                        )
                )
                .doOnTerminate(() -> System.out.println("Request completed"))
                .subscribe(
                        response -> System.out.println("Response: " + response),
                        error -> System.err.println("Error occurred after 5 retries: " + error.getMessage())
                );
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
