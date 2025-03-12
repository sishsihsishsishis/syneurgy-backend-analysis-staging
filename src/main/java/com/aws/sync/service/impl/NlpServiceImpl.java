package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.*;
import com.aws.sync.entity.gpt.ChatCompletion;
import com.aws.sync.entity.gpt.Message;
import com.aws.sync.entity.gpt.RequestMessage;
import com.aws.sync.mapper.NlpMapper;
import com.aws.sync.mapper.SummaryMapper;
import com.aws.sync.mapper.WordInfoMapper;
import com.aws.sync.service.*;
import com.aws.sync.utils.NlpUtil;
import com.aws.sync.utils.SSLUtilities;
import com.aws.sync.vo.NlpDataVO;
import com.aws.sync.vo.NlpVO;
import com.aws.sync.vo.WordRateVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashvayne.chatgpt.dto.ChatRequest;
import io.github.flashvayne.chatgpt.dto.ChatResponse;
import io.github.flashvayne.chatgpt.dto.Choice;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.aws.sync.constants.MeetingConstants.SYNCHRONY_MOMENT_HANDLE;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
public class NlpServiceImpl extends ServiceImpl<NlpMapper, NlpTable> implements NlpService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.url}")
    private String url;

    private static final int MAX_RETRIES = 3; // 设置最大重试次数

    @Resource
    NlpMapper nlpMapper;

    @Autowired
    NlpSummaryService nlpSummaryService;

    @Autowired
    MeetingService meetingService;

    @Autowired
    AmazonUploadService amazonUploadService;

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
    CVHandleService cvHandleService;

    @Autowired
    SpeakerUserService speakerUserService;

    @Autowired
    NlpWordCountService nlpWordCountService;

    @Autowired
    ChatgptService chatgptService;

    @Resource
    SummaryMapper summaryMapper;

    @Resource
    SynchronyMomentService synchronyMomentService;

//    @Autowired
//    RestTemplate restTemplate;

    @Autowired
    DetectionNLPService detectionNLPService;

    @Autowired
    GptService gptService;

    @Autowired
    WordRateService wordRateService;

    @Autowired
    WordInfoService wordInfoService;

    private static final String URI = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNlp(List<NlpTable> nlpTables) {
            return nlpMapper.addBatch(nlpTables);
    }

    @Override
    public List<NlpVO> findNlp(Long meetingID) {
        LambdaQueryWrapper<NlpTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(NlpTable::getMeeting_id,meetingID).orderByAsc(NlpTable::getStarts);
        List<NlpTable> nlpTables = nlpMapper.selectList(lambdaQueryWrapper);
        List<NlpVO> result = new ArrayList<>();
        for (NlpTable nlpTable : nlpTables) {
            NlpVO nlpVO = new NlpVO();
            BeanUtils.copyProperties(nlpTable,nlpVO);
            result.add(nlpVO);
        }
        return result;
    }

    private String ArrToString(List<List<Double>> data){
        StringBuilder sb = new StringBuilder();
        for (List<Double> datum: data) {
            sb.append("[");
            for (int i = 0; i < datum.size(); i++) {
                if (i != datum.size() - 1) {
                    sb.append(datum.get(i) + ",");
                } else {
                    sb.append(datum.get(i));
                }
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public RestResult handleNlp(Long meetingID) throws IOException {
        log.info("[NlpServiceImpl][handleNlp] meetingID :{}", meetingID);
        HashMap<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id", meetingID);

        List<String> userList = new ArrayList<>();
        String fileName = "nlp_result.txt";
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp(fileName, Long.toString(meetingID), userList);
        List<WordRate> wordRates = processWordRate(nlp_data, meetingID);

        wordRateService.removeByMap(deleteMap);
        wordRateService.saveBatch(wordRates);

        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingID, nlp_data);


        //统计每个speaker的说话时长占比
        HashMap<String, Double> map = computeSpeakerTime(nlpTables);
        Map<String, Double> team = wordRates.stream()
                .filter(entry -> !entry.getName().equals("team"))
                .collect(Collectors.groupingBy(WordRate::getName,
                        Collectors.averagingDouble(WordRate::getRate)));
        List<WordInfo> wordInfoList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String speaker = entry.getKey();
            Double rate = entry.getValue();
            if (!team.containsKey(speaker)) {
                continue;
            }
            WordInfo wordInfo = new WordInfo(meetingID, speaker, team.get(speaker) * 60, rate * 100);
            wordInfoList.add(wordInfo);
        }

        wordInfoService.removeByMap(deleteMap);
        wordInfoService.saveBatch(wordInfoList);

        //统计30s的说话量
        Map<String, Map<Integer, Integer>> wordCount = NlpUtil.wordCount(nlpTables);

        nlpWordCountService.removeByMap(deleteMap);
        nlpWordCountService.addOneByOne(wordCount, meetingID);

        removeByMap(deleteMap);
        insertNlp(nlpTables);

        gptService.processNLPData(nlp_data, meetingID);
        gptService.processAllHighlight(nlp_data, meetingID);

        List<String> speakers_keys = new ArrayList<>();
        List<Double> speakers_time = new ArrayList<>();
        List<Double> speakers_rate = new ArrayList<>();

        List<String> emotions_keys = new ArrayList<>();
        List<Double> emotions_time = new ArrayList<>();
        List<Double> emotions_rate = new ArrayList<>();

        List<String> acts_keys = new ArrayList<>();
        List<Double> acts_time = new ArrayList<>();
        List<Double> acts_rate = new ArrayList<>();

        List<List<Double>> bar_speakers = new ArrayList<>();
        List<List<Double>> bar_emotions = new ArrayList<>();

        List<Double> total_time = new ArrayList<>();
        List<String> sentences_array = new ArrayList<String>();


        NlpUtil.get_pie_and_bar(nlp_data, speakers_keys, speakers_time, speakers_rate,
                emotions_keys, emotions_time, emotions_rate,
                acts_keys, acts_time, acts_rate,
                bar_speakers, bar_emotions, total_time, sentences_array, userList);


        List<PieSpeaker> pieSpeakers = new ArrayList<>();
        List<PieEmotion> pieEmotions = new ArrayList<>();
        List<PieAct> pieActs = new ArrayList<>();

        for(int i = 0; i < speakers_keys.size(); i ++){
            PieSpeaker pieSpeaker = new PieSpeaker();
            pieSpeaker.setMeeting_id(meetingID);
            pieSpeaker.setSpeaker(speakers_keys.get(i));
            pieSpeaker.setSpeaker_time(speakers_time.get(i));
            pieSpeaker.setSpeaker_time_rate(speakers_rate.get(i));
            pieSpeaker.setNegative(bar_speakers.get(i).get(0));
            pieSpeaker.setNeutral(bar_speakers.get(i).get(1));
            pieSpeaker.setPositive(bar_speakers.get(i).get(2));
            pieSpeakers.add(pieSpeaker);
        }


        for(int i = 0; i < emotions_keys.size(); i++){
            PieEmotion pieEmotion = new PieEmotion();
            pieEmotion.setMeeting_id(meetingID);
            pieEmotion.setEmotion(emotions_keys.get(i));
            pieEmotion.setEmotion_time(emotions_time.get(i));
            pieEmotion.setEmotion_time_rate(emotions_rate.get(i));
            pieEmotions.add(pieEmotion);
        }


        for(int i = 0; i< acts_keys.size(); i++){
            PieAct pieAct = new PieAct();
            pieAct.setMeeting_id(meetingID);
            pieAct.setAct(acts_keys.get(i));
            pieAct.setAct_time(acts_time.get(i));
            pieAct.setAct_time_rate(acts_rate.get(i));
            pieActs.add(pieAct);
        }


        pieSpeakerService.removeByMap(deleteMap);
        pieEmotionService.removeByMap(deleteMap);
        pieActService.removeByMap(deleteMap);
        pieSpeakerService.insertPie(pieSpeakers);
        pieEmotionService.insertPie(pieEmotions);
        pieActService.insertPie(pieActs);

        List<BarEmotion> barEmotions = new ArrayList<>();
        for(int i = 0; i < emotions_keys.size(); i++ ){
            for(int j = 0; j < speakers_keys.size(); j++){
                BarEmotion barEmotion = new BarEmotion();
                barEmotion.setMeeting_id(meetingID);
                barEmotion.setEmotion(emotions_keys.get(i));
                barEmotion.setUsers(speakers_keys.get(j));
                barEmotion.setScore(bar_emotions.get(i).get(j));
                barEmotions.add(barEmotion);
            }
        }

        barEmotionService.removeByMap(deleteMap);
        barEmotionService.insertBar(barEmotions);

//        radar部分处理
        //TODO: 处理所有的数据
        List<Double> radar_chart_list = new ArrayList<>();
        List<String> r_keys = new ArrayList<>();
        List<Radar> radars = new ArrayList<>();

        NlpUtil.get_radar_components(speakers_time, total_time.get(0), acts_time, emotions_time, sentences_array, radar_chart_list, r_keys, userList);

        LambdaQueryWrapper<MeetingTable> meetingTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingTableLambdaQueryWrapper.eq(MeetingTable::getMeeting_id, meetingID);
        MeetingTable meetingTable = meetingService.getOne(meetingTableLambdaQueryWrapper);

        for(int i = 0; i < radar_chart_list.size(); i++){
            if ("Trust and Psychological Safety".equals(r_keys.get(i))
                    && meetingTable != null && meetingTable.getBehavior_score() != null && meetingTable.getBody_score() != null) {
                Double value = radar_chart_list.get(i);
                Double behaviourScore = meetingTable.getBehavior_score();
                Double bodyScore = meetingTable.getBody_score();
                Double rateTrust = behaviourScore / (behaviourScore + bodyScore);
                if (rateTrust > 0.6) rateTrust = 0.6;
                if (rateTrust < 0.4) rateTrust = 0.4;
                Double ratePsy = 1 - rateTrust;
                radars.add(new Radar(meetingID, "Trust", rateTrust * value * 2));
                radars.add(new Radar(meetingID, "Psychological Safety", ratePsy * value * 2));
            } else {
                Radar r = new Radar();
                r.setMeeting_id(meetingID);
                r.setK(r_keys.get(i));
                r.setV(radar_chart_list.get(i));
                radars.add(r);
            }
        }

        log.info("[NlpServiceImpl][handleNlp] meetingID :{}, radars:{}", meetingID, radars);
        radarService.removeByMap(deleteMap);
        Set<Radar> radarSet = new HashSet<>(radars);
        List<Radar> uniqueRadars = new ArrayList<>(radarSet);
        log.info("[NlpServiceImpl][handleNlp] meetingID :{}, uniqueRadars:{}", meetingID, uniqueRadars);
        radarService.insertRadar(uniqueRadars);

        meetingService.updateNlpHandle(meetingID);

        LambdaQueryWrapper<MeetingTable> syncMomentQueryWrapper = new LambdaQueryWrapper<>();
        syncMomentQueryWrapper.eq(MeetingTable::getMeeting_id, meetingID);
        List<MeetingTable> tables = meetingService.list(syncMomentQueryWrapper);

        if(BooleanUtils.isTrue(meetingService.checkCVHandle(meetingID))){
            meetingService.updateDataHandle(meetingID);

            if(tables != null && tables.size() > 0) {
                synchronyMomentService.removeByMap(deleteMap);
                synchronyMomentService.saveSmallest3(meetingID);
                UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meeting_id",meetingID)
                        .set("synchrony_moment_handle",SYNCHRONY_MOMENT_HANDLE);
                meetingService.update(null,updateWrapper);
            }
            return RestResult.success().data("All data handled");
        }
        return RestResult.success().data("Nlp data handling succeed");
    }

    @Override
    public RestResult handleWordInfo(Long meetingId) throws IOException {
        log.info("[NlpServiceImpl][handleWordInfo] meetingID :{}{}", meetingId);
        HashMap<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id", meetingId);

        List<String> userList = new ArrayList<>();
        String fileName = "nlp_result.txt";
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp(fileName, Long.toString(meetingId), userList);
        List<WordRate> wordRates = processWordRate(nlp_data, meetingId);

        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingId, nlp_data);

        HashMap<String, Double> map = computeSpeakerTime(nlpTables);
        Map<String, Double> team = wordRates.stream()
                .filter(entry -> !entry.getName().equals("team"))
                .collect(Collectors.groupingBy(WordRate::getName,
                        Collectors.averagingDouble(WordRate::getRate)));
        List<WordInfo> wordInfoList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String speaker = entry.getKey();
            Double rate = entry.getValue();
            if (!team.containsKey(speaker)) {
                continue;
            }
            WordInfo wordInfo = new WordInfo(meetingId, speaker, team.get(speaker) * 60, rate * 100);
            wordInfoList.add(wordInfo);
        }
        wordInfoService.removeByMap(deleteMap);
        wordInfoService.saveBatch(wordInfoList);

        return RestResult.success();
    }
    @Async("taskExecutor")
    @Override
    public CompletableFuture<String> sendPostRequestAsync(String url, Long meetingId) {
        try {
            log.info("[NlpServiceImpl][sendPostRequestAsync] meetingID :{}, url :{}", meetingId, url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestBody = "{\"meeting_id\":\"" + meetingId + "\"}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            // 发送POST请求
            RestTemplate restTemplate = SSLUtilities.createRestTemplateWithDisabledSSL();
            String response = restTemplate.postForObject(url, request, String.class);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    private HashMap<String, Double> computeSpeakerTime(List<NlpTable> nlpTables){
        Double totalTime = 0.0d;
        HashMap<String, Double> map = new HashMap<>();
        for (NlpTable nlpTable : nlpTables) {
            Double delay = nlpTable.getEnds() - nlpTable.getStarts();
            map.put(nlpTable.getSpeaker(), map.getOrDefault(nlpTable.getSpeaker(), 0.0d) + delay);
            totalTime += delay;
        }
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            Double value = entry.getValue();
            entry.setValue(value / totalTime);
        }
        return map;
    }
    /*private void handleSummary(List<NlpTable> nlpTables, Long meetingID, HashMap<String, Object> deleteMap) {
       try {
           //TODO: summary
           List<String> summary = new ArrayList<>();
           List<String> text = new ArrayList<>();
           for (NlpTable nlpTable : nlpTables) {
               text.add(nlpTable.getSentence());
           }

           // 分组并拼接字符串
           List<String> result = new ArrayList<>();
           int size = text.size();
           int fullPartsCount = size / 40;
           for (int i = 0; i < fullPartsCount; i++) {
               String part = text.subList(i * 40, (i + 1) * 40).stream().collect(Collectors.joining());
               result.add(part);
           }

           // 处理最后不足100的部分，如果有的话
           if (size % 40 != 0) {
               int startIndex = fullPartsCount * 40;
               String lastPart = text.subList(startIndex, size).stream().collect(Collectors.joining());
               if (fullPartsCount > 0) {
                   result.set(fullPartsCount - 1, result.get(fullPartsCount - 1) + lastPart);
               } else {
                   result.add(lastPart);
               }
           }

           for (int i = 0; i < result.size(); i++) {
               String question = "Hi,GPT.Help me summarize the following conversation." + result.get(i);
               if (question.length() > 400) {
//                   ChatRequest chatRequest = new ChatRequest("text-davinci-003",question,2048,1.0,0.9);
                   ChatRequest chatRequest = new ChatRequest("gpt-3.5-turbo",question,2048,1.0,0.9);
                   ChatResponse chatResponse = chatgptService.sendChatRequest(chatRequest);
                   List<Choice> choices = chatResponse.getChoices();
                   summary.add(choices.get(0).getText());
               }
           }

           summaryMapper.deleteByMap(deleteMap);
           for (int i = 0; i < summary.size(); i++) {
               summaryMapper.insert(new SummaryTable(meetingID, i, summary.get(i)));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }*/

/*    private void handleParagraphOneByOne(List<String[]> nlp_data,List<NlpTable> nlpTables, Long meetingID, HashMap<String, Object> deleteMap) {
        try {
            List<String> summary = new ArrayList<>();
            List<String> text = new ArrayList<>();
            for (String[] nlp_datum : nlp_data) {
                StringBuilder nlp = new StringBuilder();
                for (String n : nlp_datum) {
                    nlp.append(n).append(" ");
                }
                text.add(nlp.toString());
            }

            // 分组并拼接字符串
            List<String> result = new ArrayList<>();
            int size = text.size();
            int fullPartsCount = size / 40;
            for (int i = 0; i < fullPartsCount; i++) {
                String part = text.subList(i * 40, (i + 1) * 40).stream().collect(Collectors.joining());
                result.add(part);
            }

            // 处理最后不足100的部分，如果有的话
            if (size % 40 != 0) {
                int startIndex = fullPartsCount * 40;
                String lastPart = text.subList(startIndex, size).stream().collect(Collectors.joining());
                if (fullPartsCount > 0) {
                    result.set(fullPartsCount - 1, result.get(fullPartsCount - 1) + lastPart);
                } else {
                    result.add(lastPart);
                }
            }

            String systemPrompt = "Your identity:\n" +
                    "A highly skilled artificial intelligence trained in language comprehension and summarization.\n" +
                    "\n" +
                    "The text I will submit:\n" +
                    "I'm about to submit a text of the conversation. Each line represents a conversation record, the format is:\n" +
                    "<role>\\t<start time>\\t<end time>\\t<sentence>\\t<emotion>\\t<dialogue act>\n" +
                    "\n" +
                    "Where:\n" +
                    "<role> indicates the speaker's role.\n" +
                    "<begin time> is the start time of the sentence.\n" +
                    "<End time> is the end time of the sentence.\n" +
                    "<sentence> is the specific content of the sentence.\n" +
                    "<emotion> describes the emotion conveyed in the sentence.\n" +
                    "<Dialogue behavior> refers to the type or action of dialogue.\n" +
                    "\n" +
                    "Processing procedures:\n" +
                    "Submission method:I will submit a dialogue text for a paragraph.\n" +
                    "\n" +
                    "Paragraphs and summaries: Based on the context of the text, split the text into a logical paragraph. Paragraphs should revolve around a theme or idea, double-checking the data structure to make sure it is handled correctly. Summarize the paragraph and extract its core ideas or information. Ensure that the summary captures the topic and content of the passage very accurately. Based on the summary, try to merge multiple related summaries into a higher-level summary. Finally, Try to keep the summary to no more than 100 words.\n" +
                    "\n" +
                    "Time Range: Provide start and end timestamps for summary. \n" +
                    "\n" +
                    "Emphasis: Don’t show me the processing flow on the page, only show me the final result paragraph on the page.\n" +
                    "\n" +
                    "Please answer me using the following template:\n" +
                    "(0:00-300:00)The first topic of conversation in the weekly product marketing meeting is about training and cross mobility within marketing. The idea is to promote cross mobility by implementing a training curriculum.";

            for (int i = 0; i < result.size(); i++) {
                RestTemplate restTemplate = new RestTemplate();

                // Set headers
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + API_KEY);

                Message m = new Message("user", result.get(i));
                Message system = new Message("system", systemPrompt);
                List<Message> list = new ArrayList<>();
                list.add(system);
                list.add(m);
                RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
                org.springframework.http.HttpEntity<RequestMessage> request =
                        new org.springframework.http.HttpEntity<>(r, headers);

                ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(URI, request, ChatCompletion.class);
                System.out.println("debug");
                ChatCompletion body = response.getBody();
                ChatCompletion.Choice choice = body.getChoices()[0];
                String content = choice.getMessage().getContent();
                System.out.println(content);
                summary.add(i + content);
            }

            summaryMapper.deleteByMap(deleteMap);
            for (int i = 0; i < summary.size(); i++) {
                summaryMapper.insert(new SummaryTable(meetingID, i, summary.get(i)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    //TODO: 改写成注解重试
//    @Async("taskExecutor")
//    @Retryable(
//            value = { Exception.class }, // 指定触发重试的异常类型
//            maxAttempts = MAX_RETRIES, // 最大重试次数
//            backoff = @Backoff(delay = 1000) // 重试间隔
//    )
    public void summaryNlpText(List<String[]> nlp_data,List<NlpTable> nlpTables, Long meetingID, HashMap<String, Object> deleteMap) {
        int maxRetries = 1; // 最大重试次数
        int retryCount = 0; // 当前重试次数计数器
//        while (true) {
            try {
                StringBuilder content = new StringBuilder();
                for (String[] nlp_datum : nlp_data) {
                    StringBuilder nlp = new StringBuilder();
                    for (String n : nlp_datum) {
                        nlp.append(n).append(" ");
                    }
                    content.append(nlp).append("\n");
                }

                //start
                // 拼接文件内容和消息
                String finalMessage = content.toString();
                System.out.println(finalMessage);

                RestTemplate restTemplate = new RestTemplate();

                // Set headers
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + apiKey);

                String systemPrompt = "Your identity:\n" +
                        "A highly skilled artificial intelligence trained in language comprehension and summarization.\n" +
                        "\n" +
                        "The text I will submit:\n" +
                        "I'm about to submit a lengthy text of the conversation. Each line represents a conversation record, the format is:\n" +
                        "<role>\\t<start time>\\t<end time>\\t<sentence>\\t<emotion>\\t<dialogue act>\n" +
                        "\n" +
                        "Where:\n" +
                        "<role> indicates the speaker's role.\n" +
                        "<begin time> is the start time of the sentence.\n" +
                        "<End time> is the end time of the sentence.\n" +
                        "<sentence> is the specific content of the sentence.\n" +
                        "<emotion> describes the emotion conveyed in the sentence.\n" +
                        "<Dialogue behavior> refers to the type or action of dialogue.\n" +
                        "\n" +
                        "Processing procedures:\n" +
                        "Submission method: I will submit the entire conversation text in one go.\n" +
                        "\n" +
                        "Paragraphs and summaries: Based on the context of the text, split the text into logical paragraphs. Paragraphs should revolve around a theme or idea, double-checking the data structure to make sure it is handled correctly. Summarize each paragraph and extract its core ideas or information. Ensure that the summary captures the topic and content of the passage very accurately. Based on the summary, try to merge multiple related summaries into a higher-level summary. Make sure there is no duplication of information between each paragraph and remove redundant details. Do not exceed five paragraphs. Then, Try to keep each summary to no more than 100 words.Finally, label each segment with a serial number.\n" +
                        "\n" +
                        "Time Range: Provides start and end timestamps for each paragraph. The timestamps before and after each paragraph are consecutive.\n" +
                        "\n" +
                        "Emphasis: Don’t show me the processing flow on the page, only show me the final result paragraph on the page.\n" +
                        "\n" +
                        "\n" +
                        "Example paragraph template for summary:\n" +
                        "1(0:00-300:00)The first topic of conversation in the weekly product marketing meeting is about training and cross mobility within marketing. The idea is to promote cross mobility by implementing a training curriculum. \n" +
                        "\n\n" +
                        "2(301:00-700:00) The second topic revolves around improving the solutions page on the website. The speaker discusses the current disorganized state of the solutions page and suggests rationalizing the content and categorizing it properly.\n";
                Message m = new Message("user", finalMessage);
                Message system = new Message("system", systemPrompt);
                List<Message> list = new ArrayList<>();
                list.add(system);
                list.add(m);
                RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
                // Create and send the request
                org.springframework.http.HttpEntity<RequestMessage> request =
                        new org.springframework.http.HttpEntity<>(r, headers);

                ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
                System.out.println("debug");
                ChatCompletion body = response.getBody();
                ChatCompletion.Choice choice = body.getChoices()[0];
                String content1 = choice.getMessage().getContent();
                String[] summary = content1.split("\n");
                System.out.println(content1);
                summaryMapper.deleteByMap(deleteMap);
                int index = 0;
                for (String s : summary) {
                    if(!s.trim().isEmpty() && Character.isDigit(s.charAt(0))) {
                        summaryMapper.insert(new SummaryTable(meetingID, index, s));
                        index++;
                    }
                }
//                break;
            } catch (Exception e) {
                if (retryCount < maxRetries) {
                    // 如果尚未达到最大重试次数，增加重试计数器
                    retryCount++;
                    System.out.println("执行失败，正在重试第 " + retryCount + " 次...");
                } else {
                    // 达到最大重试次数，调用备用方法
                    System.out.println("重试失败，执行备用方法...");
//                    summaryNlpText(nlp_data, nlpTables, meetingID, deleteMap);
//                    break; // 退出循环
                }
            }
//        }

    }

    @Async("taskExecutor")
    @Retryable(
            value = { Exception.class }, // 指定触发重试的异常类型
            maxAttempts = MAX_RETRIES, // 最大重试次数
            backoff = @Backoff(delay = 1000) // 重试间隔
    )
    public List<NlpSummary> summaryNlp(List<String[]> nlp_data, Long meetingID, HashMap<String, Object> deleteMap) {
        List<NlpSummary> nlpSummaryList = new ArrayList<>();
        StringBuilder contents = new StringBuilder();
        List<String> contentList = new ArrayList<>();
        int count = 0;
        for (String[] nlp_datum : nlp_data) {
            StringBuilder nlp = new StringBuilder();
            for (String n : nlp_datum) {
                nlp.append(n).append(" ");
            }
            contents.append(nlp).append("\n");
            count++;

            if (count == 300) {
                contentList.add(contents.toString());
                contents = new StringBuilder(); // Reset the content StringBuilder
                count = 0; // Reset the counter
            }
        }
        if (count > 20) {
            contentList.add(contents.toString());
        }

        for (String content : contentList) {
            //start
            // 拼接文件内容和消息
            String finalMessage = content.toString();
            System.out.println(finalMessage);

            RestTemplate restTemplate = new RestTemplate();

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            String systemPrompt = "Your identity:\n" +
                    "A highly skilled artificial intelligence trained in language comprehension and summarization.\n" +
                    "\n" +
                    "I will submit dialogue text for analysis, where each line of the text follows this format:\n" +
                    "\n" +
                    "\n" +
                    "Role  Start Time  End Time  Sentence  Emotion  Dialogue Act\n" +
                    "\n" +
                    "\n" +
                    "Specifically:\n" +
                    "Role: Identifies the specific identity or name of the speaker.\n" +
                    "Start Time: Marks the starting time point of the sentence spoken.\n" +
                    "End Time: Marks the ending time point of the sentence spoken.\n" +
                    "Sentence: Records the specific dialogue content of the speaker.\n" +
                    "Emotion: Describes the emotional state conveyed in the sentence.\n" +
                    "Dialogue Act: Defines the nature of the dialogue or the speaker's behavior pattern.\n" +
                    "All of the above information should be provided in the form of text strings.\n" +
                    "The text processing program will operate as follows:\n" +
                    "\n" +
                    "\n" +
                    "Submission Method: Submit the complete dialogue text all at once.\n" +
                    "\n" +
                    "\n" +
                    "Paragraphs and Summary: Analyze the text context to identify paragraphs that exhibit attitudes of trust, which may be manifested through secondary subdivision attitudes. Then, divide these into appropriately sized small paragraphs, each ideally lasting about ten to twenty seconds.\n" +
                    "\n" +
                    "\n" +
                    "Secondary subdivision attitudes include:\n" +
                    "\n" +
                    "\n" +
                    "Trust(Consistency & Reliability)\n" +
                    "Trust(Open Honest Communication)\n" +
                    "Trust(Demonstrating Competence and Expertise)\n" +
                    "Trust(Show Empathy and Understanding)\n" +
                    "Trust(Accountability)\n" +
                    "Trust(Consistency with Values)\n" +
                    "Trust(Confidentiality and Discretion)\n" +
                    "Trust(Support and Collaboration)\n" +
                    "Trust(Conflict Resolution)\n" +
                    "Trust(Consistent Feedback and Recognition)\n" +
                    "Active Listening(Attentiveness)\n" +
                    "Active Listening(Clear Mind)\n" +
                    "Active Listening(Empathy)\n" +
                    "Active Listening(Nonverbal Communication)\n" +
                    "Active Listening(Minimal Encouragers)\n" +
                    "Active Listening(Paraphrasing)\n" +
                    "Active Listening(Reflective Listening)\n" +
                    "Active Listening(Open-Ended Questions)\n" +
                    "Active Listening(Avoid Interrupting)\n" +
                    "Active Listening(Avoid Judging)\n" +
                    "Active Listening(Feedback)\n" +
                    "Psychological Safety(Leadership Support and Modeling)\n" +
                    "Psychological Safety(Open and Non-Judgmental Communication)\n" +
                    "Psychological Safety(Trust Building)\n" +
                    "Psychological Safety(Active Listening)\n" +
                    "Psychological Safety(Feedback and Recognition)\n" +
                    "Psychological Safety(Conflict Resolution Skills)\n" +
                    "Psychological Safety(Empathy and Understanding)\n" +
                    "Psychological Safety(Inclusive Decision Making)\n" +
                    "Psychological Safety(Clear Team Goals and Expectations)\n" +
                    "Psychological Safety(Continuous Learning and Improvement)\n" +
                    "Leadership Modeling(Inspiring Vision)\n" +
                    "Leadership Modeling(Integrity and Ethics)\n" +
                    "Leadership Modeling(Vulnerability)\n" +
                    "Leadership Modeling(Innovation Mindset)\n" +
                    "Leadership Modeling(Curiosity)\n" +
                    "Leadership Modeling(Empathy)\n" +
                    "Leadership Modeling(Inclusion)\n" +
                    "Leadership Modeling(Change Advocacy)\n" +
                    "Leadership Modeling(Collaboration)\n" +
                    "Leadership Modeling(Accountability)\n" +
                    "Leadership Modeling(Adaptability)\n" +
                    "Leadership Modeling(Active Listening)\n" +
                    "Leadership Modeling(Decision Courage)\n" +
                    "Leadership Modeling(Conflict Resolution)\n" +
                    "Leadership Modeling(Opportunity Framing)\n" +
                    "Please categorize the identified attitudes of 'trust', 'active listening', and 'leadership modeling' into the provided secondary attitude categories.\n" +
                    "\n" +
                    "\n" +
                    "Provide Timestamps: Record the timestamps associated with the respective attitudes, including both start and end times, to clearly indicate the exact duration of each small paragraph, allowing users to track the exact location of each concept in the text.\n" +
                    "\n" +
                    "\n" +
                    "Time Range: Clearly provide the start and end timestamps of each small paragraph to clearly indicate its exact duration.\n" +
                    "\n" +
                    "\n" +
                    "Interface Emphasis: The processing is not displayed on the interface; users can only see the final result.\n" +
                    "\n" +
                    "\n" +
                    "Final Output Display: Only the secondary subdivision attitude terms, speakers, and all corresponding start and end timestamps should be displayed. Do not include any other extraneous information!\n" +
                    "\n" +
                    "\n" +
                    "Like this:\n" +
                    "speaker01, 13.00-20.00, Trust(Consistency & Reliability)\n" +
                    "speaker00, 94.00-97.00, Trust(Accountability)\n" +
                    "speaker01, 120.00-123.00, Active Listening(Minimal Encouragers)\n" +
                    "speaker01, 209.00-212.00, Leadership Modeling(Change Advocacy)\n" +
                    "speaker00, 220.00-223.00, Leadership Modeling(Change Advocacy)\n" +
                    "speaker00, 225.00-231.00, Leadership Modeling(Change Advocacy)\n" +
                    "speaker01, 220.00-223.00, Psychological Safety(Conflict Resolution Skills)\n" +
                    "speaker00, 111.00-112.00, Psychological Safety(Conflict Resolution Skills)\n" +
                    "speaker01, 74.00-75.00, Psychological Safety(Conflict Resolution Skills)\n" +
                    "speaker01, 75.00-76.00, Psychological Safety(Clear Team Goals and Expectations)\n" +
                    "speaker00, 94.00-97.00, Psychological Safety(Clear Team Goals and Expectations)\n" +
                    "speaker00, 101.00-105.50, Psychological Safety(Open and Non-Judgmental Communication)\n" +
                    "speaker01, 106.40-107.50, Psychological Safety(Open and Non-Judgmental Communication)\n";
            Message m = new Message("user", finalMessage);
            Message system = new Message("system", systemPrompt);
            List<Message> list = new ArrayList<>();
            list.add(system);
            list.add(m);
            RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
            // Create and send the request
            org.springframework.http.HttpEntity<RequestMessage> request =
                    new org.springframework.http.HttpEntity<>(r, headers);

            ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
            System.out.println("debug");
            ChatCompletion body = response.getBody();
            ChatCompletion.Choice choice = body.getChoices()[0];
            String content1 = choice.getMessage().getContent();
//                String[] summary = content1.split("\n");
//                System.out.println(content1);
            parseNlpSummary(content1, meetingID, nlpSummaryList);
        }



        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("meeting_id", meetingID);
        nlpSummaryService.removeByMap(deleteMap);
        nlpSummaryService.saveBatch(nlpSummaryList);
        return  nlpSummaryList;

    }

    @Async("taskExecutor")
    @Retryable(
            value = { Exception.class }, // 指定触发重试的异常类型
            maxAttempts = MAX_RETRIES, // 最大重试次数
            backoff = @Backoff(delay = 1000) // 重试间隔
    )
    public List<NlpSummary> summaryRadarNlp(List<String[]> nlp_data, Long meetingID, HashMap<String, Object> deleteMap) {
        List<NlpSummary> nlpSummaryList = new ArrayList<>();
        List<String> contentList = new ArrayList<>();
        StringBuilder contents = new StringBuilder();
        int count = 0;
        for (String[] nlp_datum : nlp_data) {
            StringBuilder nlp = new StringBuilder();
            for (String n : nlp_datum) {
                nlp.append(n).append(" ");
            }
            contents.append(nlp).append("\n");
            count++;
            if (count == 300) {
                contentList.add(contents.toString());
                contents = new StringBuilder(); // Reset the content StringBuilder
                count = 0; // Reset the counter
            }
        }
        if (count > 20) {
            contentList.add(contents.toString());
        }

        for (String content : contentList) {
            //start
            // 拼接文件内容和消息
            String finalMessage = content.toString();
            System.out.println(finalMessage);

            RestTemplate restTemplate = new RestTemplate();

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            String systemPrompt = "Your identity:A highly skilled artificial intelligence trained in language comprehension and summarization.\n" +
                    "\n" +
                    "I will submit dialogue text for analysis, where each line of the text follows this format:\n" +
                    "\n" +
                    "Role  Start Time  End Time  Sentence  Emotion  Dialogue Act\n" +
                    "\n" +
                    "Specifically:\n" +
                    "Role: Identifies the specific identity or name of the speaker.\n" +
                    "Start Time: Marks the starting time point of the sentence spoken.\n" +
                    "End Time: Marks the ending time point of the sentence spoken.\n" +
                    "Sentence: Records the specific dialogue content of the speaker.\n" +
                    "Emotion: Describes the emotional state conveyed in the sentence.\n" +
                    "Dialogue Act: Defines the nature of the dialogue or the speaker's behavior pattern.\n" +
                    "\n" +
                    "You need to identify ALL examples of these concepts:\n" +
                    "Trust：Trust is the belief that team members can rely on each other.\n" +
                    "Psychological Safety：Psychological Safety is the feeling that one can express ideas and take risks without fear of retribution.\n" +
                    "Synchrony：Synchrony is the ability of a team to work together seamlessly.\n" +
                    "Alignment：Alignment is ensuring that all team efforts are directed toward a common purpose.\n" +
                    "Shared Goals：Shared goals are clear objectives that everyone is committed to achieving.\n" +
                    "Feedback：Feedback is the process of providing constructive information to improve performance.\n" +
                    "Communication：Communication is the exchange of thoughts, ideas, and information among team members.\n" +
                    "Engagement：Engagement is the level of enthusiasm and commitment team members have toward their work and team goals.\n" +
                    "Enjoyment：Enjoyment at work refers to the pleasure and satisfaction team members derive from their tasks and interactions.\n" +
                    "\n" +
                    "Please categorize the attitudes into 'Trust', 'Psychological Safety','Synchrony','Alignment','Shared Goals','Feedback','Communication', 'Engagement' and 'Enjoyment'.\n" +
                    "\n" +
                    "Provide Timestamps: Record the timestamps associated with the respective attitudes, including both start and end times, to clearly indicate the exact duration of each small paragraph, allowing users to track the exact location of each concept in the text.\n" +
                    "\n" +
                    "Time Range: Clearly provide the start and end timestamps of each small paragraph to clearly indicate its exact duration.\n" +
                    "\n" +
                    "Interface Emphasis: The processing is not displayed on the interface; users can only see the final result.\n" +
                    "\n" +
                    "Final Output Display: Only the secondary subdivision attitude terms, speakers, and all corresponding start and end timestamps should be displayed. Do not include any other extraneous information!\n" +
                    "\n" +
                    "Like this:\n" +
                    "speaker01, 13.00-20.00, Trust \n" +
                    "speaker00, 94.00-97.00, Trust\n" +
                    "speaker01, 120.00-123.00, Synchrony\n" +
                    "speaker01, 209.00-212.00, Alignment\n" +
                    "speaker00, 220.00-223.00, Shared Goals\n" +
                    "speaker00, 225.00-231.00, Feedback\n" +
                    "speaker01, 220.00-223.00, Communication\n" +
                    "speaker00, 111.00-112.00, Engagement\n" +
                    "speaker01, 74.00-75.00, Enjoyment\n" +
                    "speaker01, 75.00-76.00, Enjoyment\n" +
                    "speaker00, 94.00-97.00, Engagement\n" +
                    "speaker00, 101.00-105.50, Psychological Safety\n" +
                    "speaker01, 106.40-107.50, Feedback";
            Message m = new Message("user", finalMessage);
            Message system = new Message("system", systemPrompt);
            List<Message> list = new ArrayList<>();
            list.add(system);
            list.add(m);
            RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
            // Create and send the request
            org.springframework.http.HttpEntity<RequestMessage> request =
                    new org.springframework.http.HttpEntity<>(r, headers);

            ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
            System.out.println("debug");
            ChatCompletion body = response.getBody();
            ChatCompletion.Choice choice = body.getChoices()[0];
            String content1 = choice.getMessage().getContent();
//                String[] summary = content1.split("\n");
//                System.out.println(content1);
            parseRadarNlpSummary(content1, meetingID, nlpSummaryList);
        }

        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("meeting_id", meetingID);
        delMap.put("type", "Tiny Habit");
        nlpSummaryService.removeByMap(deleteMap);
        nlpSummaryService.saveBatch(nlpSummaryList);
        return  nlpSummaryList;

    }

    private List<NlpSummary> parseNlpSummary(String input, Long meetingId, List<NlpSummary> nlpSummaryList) {
        //List<NlpSummary> nlpSummaryList = new ArrayList<>();
        String[] lines = input.split("\n"); // 按行分割
        for (String line : lines) {
            line = line.trim(); // 去除行首行尾的空格
            String[] parts = line.split(","); // 使用逗号分割

            if (parts.length == 3) {
                String speaker = parts[0].trim();
                String[] timeRange = parts[1].trim().split("-");
                double start = Double.parseDouble(timeRange[0].trim());
                double end = Double.parseDouble(timeRange[1].trim());
                String allTypes = parts[2].trim();
                int i = allTypes.lastIndexOf('(');
                if (i == -1) {
                    continue;
                }
                String type = allTypes.substring(0, i);
                String subtype = allTypes.substring(i + 1, allTypes.length() - 1);

                NlpSummary nlpSummary = new NlpSummary();
                nlpSummary.setSpeakers(speaker);
                nlpSummary.setStarts(start);
                nlpSummary.setEnds(end);
                nlpSummary.setType(type);
                nlpSummary.setSubtype(subtype);
                nlpSummary.setMeeting_id(meetingId);

                nlpSummaryList.add(nlpSummary);
            }
        }
        return nlpSummaryList;
    }

    private List<NlpSummary> parseRadarNlpSummary(String input, Long meetingId, List<NlpSummary> nlpSummaryList) {
        String[] lines = input.split("\n"); // 按行分割
        for (String line : lines) {
            line = line.trim(); // 去除行首行尾的空格
            String[] parts = line.split(","); // 使用逗号分割

            if (parts.length == 3) {
                String speaker = parts[0].trim();
                String[] timeRange = parts[1].trim().split("-");
                double start = Double.parseDouble(timeRange[0].trim());
                double end = Double.parseDouble(timeRange[1].trim());
                String subtype = parts[2].trim();

                NlpSummary nlpSummary = new NlpSummary();
                nlpSummary.setSpeakers(speaker);
                nlpSummary.setStarts(start);
                nlpSummary.setEnds(end);
                nlpSummary.setType("Tiny Habit");
                nlpSummary.setSubtype(subtype);
                nlpSummary.setMeeting_id(meetingId);

                nlpSummaryList.add(nlpSummary);
            }
        }
        return nlpSummaryList;
    }

    @Override
    public RestResult getNlpDataByMeetingID(Long meetingID) {
        LambdaQueryWrapper<NlpTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(NlpTable::getMeeting_id,meetingID).orderByAsc(NlpTable::getStarts);
        List<NlpTable> nlpMetaData = list(lambdaQueryWrapper);
        List<NlpDataVO> nlpData = new ArrayList<>();
        for (NlpTable nlp : nlpMetaData) {
            NlpDataVO nlpDataVO = new NlpDataVO();
            BeanUtils.copyProperties(nlp,nlpDataVO);
            nlpData.add(nlpDataVO);
        }
        return RestResult.success().data(nlpData);
    }

    @Override
    public RestResult handlePartNlp(Long meetingID, Integer partIndex) throws IOException {
//        //先判断nlp是否匹配
//        boolean isMatch = meetingService.checkMatch(meetingID);
//        if(!isMatch){
//            return RestResult.fail().message("Nlp is not match");
//        }
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);
        //HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingID);
        List<String> userList = new ArrayList<>();
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_results.txt", Long.toString(meetingID), userList);

        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingID, nlp_data);
        if (partIndex == 1) {
            //统计30s的说话量
            Map<String, Map<Integer, Integer>> wordCount = NlpUtil.wordCount(nlpTables);
            nlpWordCountService.removeByMap(deleteMap);
            removeByMap(deleteMap);
            nlpWordCountService.addOneByOne(wordCount,meetingID);
            insertNlp(nlpTables);
            return RestResult.success().message("WorkCount and txt data add success");
        }

        if (partIndex == 2) {
            //TODO: summary
            List<String> summary = new ArrayList<>();
            List<String> text = new ArrayList<>();
            for (NlpTable nlpTable : nlpTables) {
                text.add(nlpTable.getSentence());
            }

            // 分组并拼接字符串
            List<String> result = new ArrayList<>();
            int size = text.size();
            int fullPartsCount = size / 40;
            for (int i = 0; i < fullPartsCount; i++) {
                String part = text.subList(i * 40, (i + 1) * 40).stream().collect(Collectors.joining());
                result.add(part);
            }

            // 处理最后不足100的部分，如果有的话
            if (size % 40 != 0) {
                int startIndex = fullPartsCount * 40;
                String lastPart = text.subList(startIndex, size).stream().collect(Collectors.joining());
                if (fullPartsCount > 0) {
                    result.set(fullPartsCount - 1, result.get(fullPartsCount - 1) + lastPart);
                } else {
                    result.add(lastPart);
                }
            }

            for (int i = 0; i < result.size(); i++) {
                String question = "Hi,GPT.Help me summarize the following conversation." + result.get(i);
                ChatRequest chatRequest = new ChatRequest("text-davinci-003",question,2048,1.0,0.9);
                ChatResponse chatResponse = chatgptService.sendChatRequest(chatRequest);
                List<Choice> choices = chatResponse.getChoices();
                summary.add(choices.get(0).getText());
            }
            summaryMapper.deleteByMap(deleteMap);
            for (int i = 0; i < summary.size(); i++) {
                summaryMapper.insert(new SummaryTable(meetingID,i,summary.get(i)));
            }
            return RestResult.success().message("Summary data add success");
        }

        List<String> speakers_keys = new ArrayList<>();
        List<Double> speakers_time = new ArrayList<>();
        List<Double> speakers_rate = new ArrayList<>();

        List<String> emotions_keys = new ArrayList<>();
        List<Double> emotions_time = new ArrayList<>();
        List<Double> emotions_rate = new ArrayList<>();

        List<String> acts_keys = new ArrayList<>();
        List<Double> acts_time = new ArrayList<>();
        List<Double> acts_rate = new ArrayList<>();

        List<List<Double>> bar_speakers = new ArrayList<>();
        List<List<Double>> bar_emotions = new ArrayList<>();

        List<Double> total_time = new ArrayList<>();
        List<String> sentences_array = new ArrayList<String>();


        NlpUtil.get_pie_and_bar(nlp_data,speakers_keys,speakers_time,speakers_rate,
                emotions_keys,emotions_time,emotions_rate,
                acts_keys,acts_time,acts_rate,bar_speakers,
                bar_emotions,total_time,sentences_array,userList);

        List<PieSpeaker> pieSpeakers = new ArrayList<>();
        List<PieEmotion> pieEmotions = new ArrayList<>();
        List<PieAct> pieActs = new ArrayList<>();

        for(int i = 0; i < speakers_keys.size(); i ++){
            PieSpeaker pieSpeaker = new PieSpeaker();
            pieSpeaker.setMeeting_id(meetingID);
            pieSpeaker.setSpeaker(speakers_keys.get(i));
            pieSpeaker.setSpeaker_time(speakers_time.get(i));
            pieSpeaker.setSpeaker_time_rate(speakers_rate.get(i));
            pieSpeaker.setNegative(bar_speakers.get(i).get(0));
            pieSpeaker.setNeutral(bar_speakers.get(i).get(1));
            pieSpeaker.setPositive(bar_speakers.get(i).get(2));
            pieSpeakers.add(pieSpeaker);
        }

        for(int i = 0; i < emotions_keys.size(); i++){
            PieEmotion pieEmotion = new PieEmotion();
            pieEmotion.setMeeting_id(meetingID);
            pieEmotion.setEmotion(emotions_keys.get(i));
            pieEmotion.setEmotion_time(emotions_time.get(i));
            pieEmotion.setEmotion_time_rate(emotions_rate.get(i));
            pieEmotions.add(pieEmotion);
        }

        for(int i = 0; i< acts_keys.size(); i++){
            PieAct pieAct = new PieAct();
            pieAct.setMeeting_id(meetingID);
            pieAct.setAct(acts_keys.get(i));
            pieAct.setAct_time(acts_time.get(i));
            pieAct.setAct_time_rate(acts_rate.get(i));
            pieActs.add(pieAct);
        }

        if (partIndex == 3) {
            pieSpeakerService.removeByMap(deleteMap);
            pieEmotionService.removeByMap(deleteMap);
            pieActService.removeByMap(deleteMap);

            pieSpeakerService.insertPie(pieSpeakers);
            pieEmotionService.insertPie(pieEmotions);
            pieActService.insertPie(pieActs);

            return RestResult.success().message("Pie data add success");
        }

        if (partIndex == 4) {
            List<BarEmotion> barEmotions = new ArrayList<>();
            for(int i = 0; i < emotions_keys.size(); i++ ){
                for(int j = 0; j < speakers_keys.size(); j++){
                    BarEmotion barEmotion = new BarEmotion();
                    barEmotion.setMeeting_id(meetingID);
                    barEmotion.setEmotion(emotions_keys.get(i));
                    barEmotion.setUsers(speakers_keys.get(j));
                    barEmotion.setScore(bar_emotions.get(i).get(j));
                    barEmotions.add(barEmotion);
                }
            }
            barEmotionService.removeByMap(deleteMap);
            barEmotionService.insertBar(barEmotions);

            return RestResult.success().message("Bar data add success");
        }

        if (partIndex == 5) {
            //radar部分处理
            List<Double> radar_chart_list = new ArrayList<>();
            List<String> r_keys = new ArrayList<>();
            List<Radar> radars = new ArrayList<>();
            NlpUtil.get_radar_components(speakers_time,total_time.get(0),acts_time,emotions_time,sentences_array,radar_chart_list,r_keys,userList);

            for(int i = 0; i < radar_chart_list.size(); i++){
                Radar r = new Radar();
                r.setMeeting_id(meetingID);
                r.setK(r_keys.get(i));
                r.setV(radar_chart_list.get(i));
                radars.add(r);
            }
//            radarService.removeByMap(deleteMap);
//            radarService.insertRadar(radars);
            return RestResult.success().message("Radar data add success");
        }
        meetingService.updateNlpHandle(meetingID);

        LambdaQueryWrapper<MeetingTable> syncMomentQueryWrapper = new LambdaQueryWrapper<>();
        syncMomentQueryWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        List<MeetingTable> tables = meetingService.list(syncMomentQueryWrapper);


        if(BooleanUtils.isTrue(meetingService.checkCVHandle(meetingID)) && meetingService.checkMatch(meetingID) ){
            meetingService.updateDataHandle(meetingID);
            if(tables != null && tables.size() > 0 && tables.get(0).getSynchrony_moment_handle() == 0){
                synchronyMomentService.saveSmallest3(meetingID);
                UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meeting_id",meetingID)
                        .set("synchrony_moment_handle",SYNCHRONY_MOMENT_HANDLE);
                meetingService.update(null,updateWrapper);
            }
            return RestResult.success().data("All data handled");
        }
        return RestResult.success().data("Nlp data handling succeed");
    }

    @Override
    public RestResult handleNplSummary(Long meetingId) throws IOException {
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingId);

        HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingId);
        List<String> userList = new ArrayList<>();
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_result.txt", Long.toString(meetingId),userList);
        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingId, nlp_data);
        summaryNlpText(nlp_data, nlpTables, meetingId, deleteMap);
        return RestResult.success();
    }

    @Override
    public RestResult nlpSummary(Long meetingId) throws IOException {
        HashMap<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingId);

        HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingId);
        List<String> userList = new ArrayList<>();
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_result.txt", Long.toString(meetingId), userList);
        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingId, nlp_data);
        List<NlpSummary> nlpSummaryList = summaryNlp(nlp_data, meetingId, deleteMap);
        return RestResult.success().data(nlpSummaryList);
    }

    @Override
    public RestResult nlpClassification(Long meetingId) throws IOException {
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingId);

        HashMap<String, List<String>> speakerMap = speakerUserService.getSpeakerMap(meetingId);
        List<String> userList = new ArrayList<>();
        //nlp data handle
        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_result.txt", Long.toString(meetingId),userList);
        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingId, nlp_data);
        List<NlpSummary> nlpSummaryList = summaryRadarNlp(nlp_data, meetingId, deleteMap);
        return RestResult.success().data(nlpSummaryList);
    }

    @Override
    @Async("taskExecutor")
    @Retryable(
            value = { Exception.class }, // 指定触发重试的异常类型
            maxAttempts = MAX_RETRIES, // 最大重试次数
            backoff = @Backoff(delay = 1000) // 重试间隔
    )
    public void handleDetectionNlp(Long meetingId) throws IOException {
        log.info("[NlpServiceImpl][handleDetectionNlp] meetingId :{}", meetingId);
        List<String> userList = new ArrayList<>();
        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_result.txt", Long.toString(meetingId), userList);
//        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_results.txt", Long.toString(meeting_id), userList);
        List<String> contentList = new ArrayList<>();
        StringBuilder contents = new StringBuilder();
        int count = 0;
        for (String[] nlp_datum : nlp_data) {
            StringBuilder nlp = new StringBuilder();
            for (String n : nlp_datum) {
                nlp.append(n).append(" ");
            }
            contents.append(nlp).append("\n");
            count++;
            if (count == 400) {
                contentList.add(contents.toString());
                contents = new StringBuilder(); // Reset the content StringBuilder
                count = 0; // Reset the counter
            }
        }
        if (count > 20) {
            contentList.add(contents.toString());
        }

        List<DetectionNLP> detectionNLPList = new ArrayList<>();
        for (String content : contentList) {
            //start
            // 拼接文件内容和消息
            String finalMessage = content;

            RestTemplate restTemplate = new RestTemplate();

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

//            String systemPrompt = "Based on the provided dialogue text, conduct a thorough analysis and categorization covering all dialogue segments that align with specific communication attitudes such as Positive Language, Engage Language, and Patience. Perform a comprehensive review of the entire document to identify all instances that fit these categories. Provide starting and ending timestamps for each identified paragraph and categorize the results according to these terms. Each term may correspond to a wide range of paragraphs, from none to several dozen. Warning: The final output should be formatted as follows: [Term] [Speaker] [Start and End Timestamps].\n" +
//                    "Positive Language refers to the use of positive and enthusiastic language in communication, such as \"excited,\" \"happy,\" or \"looking forward to,\" indicating a high level of engagement. Frequent laughter and the use of humor during the call signify a positive and enjoyable atmosphere. This approach to language and behavior is aimed at enhancing the positivity of communication, promoting positive emotional responses, thereby creating a more open and positive communication environment.\n" +
//                    "Engage Language is reflected in the frequent and positive interactions among team members, such as supporting each other's ideas or building upon them, demonstrating the team's spirit of collaboration and high level of engagement. Giving and receiving positive feedback on work or ideas further indicates a supportive and pleasant team dynamic. This mode of communication promotes effective collaboration and active participation among team members, creating an environment that encourages innovation and personal contribution.\n" +
//                    "Patience refers to the attitude displayed during communication of being willing to wait, listen to, and understand others' viewpoints rather than rushing to express one's own opinions or interrupting others. Patience is manifested in providing ample time for others to express their thoughts and feelings and offering timely feedback or suggestions when necessary, rather than pushing the conversation forward hastily. This attitude helps create a respectful and understanding communication environment where everyone's voice is heard and valued, fostering deeper communication and understanding.\n" +
//                    "\uFEFF\n" +
//                    "The output sample is listed below:\n" +
//                    "speaker01,PositiveLanguage,13.00-20.00\n" +
//                    "speaker00,Patience,94.00-97.00\n" +
//                    "speaker01,EngageLanguage,120.00-123.00\n" +
//                    "speaker01,Patience,209.00-212.00\n" +
//                    "speaker00,PositiveLanguage,220.00-223.00";
            String systemPrompt = "***Role:***\n" +
                    "Dialogue Analysis Expert\n" +
                    "\n" +
                    "***Background:***\n" +
                    "The user needs to analyze a text dialogue, identifying and categorizing segments that display specific communication attitudes. There are three categories:\n" +
                    "Positive Language,\n" +
                    "Engage Language, \n" +
                    "Patience.\n" +
                    "\n" +
                    "***Definitions:***\n" +
                    "Positive Language: Includes the use of positive and enthusiastic expressions like \"excited,\" \"happy,\" or phrases that indicate anticipation. It also encompasses laughter and humor, which contribute to a positive and engaging atmosphere.\n" +
                    "Engage Language: Reflected through frequent and constructive interactions among team members, such as supporting or adding to others' ideas, and providing positive feedback. This style indicates a collaborative spirit and high engagement level within the team.\n" +
                    "Patience: Demonstrated by a willingness to listen and allow ample time for others to express their thoughts without rushing or interrupting. It includes moments where speakers provide space for others to speak and offer thoughtful feedback, fostering a respectful and understanding communication environment.\n" +
                    "\n" +
                    "***Profile:***\n" +
                    "You are a professional dialogue analysis expert capable of deeply analyzing and categorizing text, with the ability to accurately identify and differentiate various communication attitudes.\n" +
                    "\n" +
                    "***Goals:***\n" +
                    "Conduct a thorough analysis and categorization of all dialogue segments that align with specific communication attitudes such as Positive Language, Engage Language, and Patience. Perform a comprehensive review of the entire document to identify all instances that fit these categories. Provide starting and ending timestamps for each identified paragraph and categorize the results according to these terms. Each term may correspond to a wide range of paragraphs, from none to several dozen.\n" +
                    "\n" +
                    "***Constraints:***\n" +
                    "The analysis must be accurate, the recording of timestamps must be precise, and the categorization must be clear.\n" +
                    "\n" +
                    "***Workflow:***\n" +
                    "1 Read and understand the dialogue text.\n" +
                    "2 Identify paragraphs in the dialogue that exhibit specific communication attitudes.\n" +
                    "3 Record the starting and ending timestamps for each paragraph.\n" +
                    "4 Categorize the identified paragraphs according to the communication attitudes.\n" +
                    "\n" +
                    "***Output Format:***\n" +
                    "The output must only list the speaker, identified communication attitude, and the corresponding start and end timestamps in the format, and the output must be in the exact format below, no additional outputs:\n" +
                    "speaker01,PositiveLanguage,13.00-20.00\n" +
                    "speaker00,Patience,30.00-65.00\n" +
                    "speaker01,EngageLanguage,70.00-103.00\n" +
                    "speaker00,Patience,110.00-123.00\n" +
                    "speaker01,PositiveLanguage,130.00-133.00\n" +
                    "speaker02,EngageLanguage,140.00-145.00\n" ;
            Message m = new Message("user", finalMessage);
            Message system = new Message("system", systemPrompt);
            List<Message> list = new ArrayList<>();
            list.add(system);
            list.add(m);
            RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
            // Create and send the request
            org.springframework.http.HttpEntity<RequestMessage> request =
                    new org.springframework.http.HttpEntity<>(r, headers);

            ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
            ChatCompletion body = response.getBody();
            ChatCompletion.Choice choice = body.getChoices()[0];
            String content1 = choice.getMessage().getContent();
            log.info("[NlpServiceImpl][handleDetectionNlp] meetingId :{}, content :{}", meetingId, content1);


            parseDetectionNlp(content1, meetingId, detectionNLPList);
        }

        HashMap<String, Object> delMap = new HashMap<>();
        delMap.put("meeting_id", meetingId);
        log.info("[NlpServiceImpl][handleDetectionNlp] end!");
        detectionNLPService.removeByMap(delMap);
        detectionNLPService.saveBatch(detectionNLPList);
    }

    private void parseDetectionNlp(String input, Long meeting_id, List<DetectionNLP> detectionNLPList) {
        String[] lines = input.split("\n"); // 按行分割
        for (String line : lines) {
            line = line.trim(); // 去除行首行尾的空格
            String[] parts = line.split(","); // 使用逗号分割
            if (parts.length == 1) {
                parts = line.split("\\s+");
            }
            if (parts.length == 3) {
                String speaker = parts[0].trim();
                String emotion = parts[1].trim();
                if ("Patience".equalsIgnoreCase(emotion)) {
                    emotion = "NotInterrupting";
                }
                String[] timeRange = parts[2].trim().split("-");
                double start = Double.parseDouble(timeRange[0].trim());
                double end = Double.parseDouble(timeRange[1].trim());

                DetectionNLP detectionNLP = new DetectionNLP();
                detectionNLP.setUsers(speaker);
                detectionNLP.setMeetingId(meeting_id);
                detectionNLP.setKeyword(emotion);
                detectionNLP.setStarts(start);
                detectionNLP.setEnds(end);
                detectionNLPList.add(detectionNLP);
            }
        }
    }

    @Override
    public List<WordRate> processWordRate(List<String[]> data, Long meetingId) {
        List<Integer> timeLine = new ArrayList<>();
        List<Map<String, Double>> maps = countWordRate(data, timeLine);

        List<Map<String, Double>> ans = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            if (i != 0) {
                Map<String, Double> pre = maps.get(i - 1);
                Set<String> speaker = pre.keySet();
                Map<String, Double> cur = maps.get(i);
                for (String s : speaker) {
                    if (!cur.containsKey(s)) {
                        cur.put(s, pre.get(s));
                    }
                }
                double ave = cur.values().stream()
                        .filter(value -> value != 0)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);
                cur.put("team", ave);
                ans.add(cur);
            } else {
                Map<String, Double> map = maps.get(0);
                double ave = map.values().stream()
                        .filter(value -> value != 0.0)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);
                map.put("team", ave);
                ans.add(map);
            }
        }

        List<WordRate> wordRateList = new ArrayList<>();
        for (int i = 0; i < ans.size(); i++) {
            Map<String, Double> map = ans.get(i);
            Integer start = timeLine.get(i);
            map.entrySet().forEach(
                    entry -> {
                        String name = entry.getKey();
                        Double rate = entry.getValue();
                        WordRate wordRate = new WordRate(meetingId, name, start, rate);
                        wordRateList.add(wordRate);
                    }
            );
        }
        return wordRateList;
    }

    private List<Map<String, Double>> countWordRate(List<String[]> data, List<Integer> timeLine) {
        List<Map<String, Double>> speakingRatesByInterval = new ArrayList<>();
        // 创建用于存储每个说话者在每个时间段内的单词总数和说话时间的映射
        Map<Integer, Map<String, Integer>> wordCountsByInterval = new HashMap<>();
        Map<Integer, Map<String, Double>> speakingDurationsByInterval = new HashMap<>();

        // 初始化起始时间和结束时间
        double startTime = 0.0;
        double endTime = 30.0;

        // 循环遍历数据，并在每个30秒的时间段内统计每个说话者的单词总数和说话时间
        for (String[] entry : data) {
            String speaker = entry[0];
            double start = Double.parseDouble(entry[1]);
            double end = Double.parseDouble(entry[2]);
            String text = entry[3].trim();

            // 计算对话时长
            double duration = end - start;
            int wordCount = text.split("\\s+").length;
            if (wordCount < 4) {
                continue;
            }
            // 检查该对话是否在当前30秒的时间段内
//            if (start >= startTime && end <= endTime) {
            if (start <= endTime) {
//                int wordCount = text.split("\\s+").length;
                wordCountsByInterval.computeIfAbsent((int) startTime, k -> new HashMap<>()).merge(speaker, wordCount, Integer::sum);
                speakingDurationsByInterval.computeIfAbsent((int) startTime, k -> new HashMap<>()).merge(speaker, duration, Double::sum);
            } else {
                Map<String, Double> speakingRates = new HashMap<>();
                calculateSpeakingRates(speakingRates, wordCountsByInterval, speakingDurationsByInterval, startTime);
                if (!speakingRates.isEmpty()) {
                    speakingRatesByInterval.add(speakingRates);
                    timeLine.add((int) startTime);
                }
                // 重置为下一个时间段
                startTime = (int)(start / 30) * 30;
                endTime += 30.0;

                // 处理当前数据点
//                int wordCount = text.split("\\s+").length;
                wordCountsByInterval.computeIfAbsent((int) startTime, k -> new HashMap<>()).merge(speaker, wordCount, Integer::sum);
                speakingDurationsByInterval.computeIfAbsent((int) startTime, k -> new HashMap<>()).merge(speaker, duration, Double::sum);
            }
        }
        Map<String, Double> speakingRates = new HashMap<>();
        // 计算最后一个30秒时间段的说话速率
        calculateSpeakingRates(speakingRates, wordCountsByInterval, speakingDurationsByInterval, startTime);
        if (!speakingRates.isEmpty()) {
            timeLine.add((int) startTime);
            speakingRatesByInterval.add(speakingRates);
        }
        return speakingRatesByInterval;
    }

    private void calculateSpeakingRates(Map<String, Double> speakingRates, Map<Integer, Map<String, Integer>> wordCountsByInterval,
                                               Map<Integer, Map<String, Double>> speakingDurationsByInterval, double startTime) {
        Map<String, Integer> wordCounts = wordCountsByInterval.getOrDefault((int) startTime, new HashMap<>());
        Map<String, Double> speakingDurations = speakingDurationsByInterval.getOrDefault((int) startTime, new HashMap<>());

        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            String speaker = entry.getKey();
            int wordCount = entry.getValue();
            double duration = speakingDurations.get(speaker);
            double speakingRate = wordCount / duration;
            speakingRates.put(speaker, speakingRate);
        }
    }
}
