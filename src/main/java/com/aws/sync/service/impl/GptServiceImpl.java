package com.aws.sync.service.impl;


import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.entity.*;
import com.aws.sync.entity.gpt.ChatCompletion;
import com.aws.sync.entity.gpt.Message;
import com.aws.sync.entity.gpt.RequestMessage;
import com.aws.sync.mapper.GptSummaryMapper;
import com.aws.sync.mapper.RadarMapper;
import com.aws.sync.mapper.SectionMapper;
import com.aws.sync.mapper.SummaryMapper;
import com.aws.sync.service.*;
import com.aws.sync.vo.*;
import com.aws.sync.vo.csv.HeatmapVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
public class GptServiceImpl extends ServiceImpl<GptSummaryMapper, GptSummary> implements GptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.url}")
    private String url;

    private static final int MAX_RETRIES = 3; // 设置最大重试次数

    private static final String GLOBAL_TEAM_PROMPT = "You are an expert in team performance, organizational psychology, and the neuroscience of collaboration. Your task is to analyze the provided data and transcript from the team meeting to generate key observations and high-level takeaways about the team and its performance.\n";


    private static final String PROMPT_PRE = "Your Role: \n" +
            "As a highly skilled artificial intelligence expert trained in language comprehension and summarization, you need to fully read and understand a long text of dialogues. \n" +
            "\n" +
            "You need to divide the text I give you into 1 to 2 logical chapters, summarize and provide 3 bullet points for each chapter.\n" +
            "\n" +
            "What I give you is a text of conversation.\n" +
            "Each line represents a dialogue record formatted as: <Speaker>\\t<Start>\\t<End>\\t<Sentence> \n" +
            "Where: \n" +
            "<Speaker> indicates the speaker id. \n" +
            "<Start Time> is the starting time of the sentence. \n" +
            "<End Time> is the ending time of the sentence. \n" +
            "<Sentence> is the specific content of the sentence. \n" +
            "\n" +
            "For each chapter, strictly follow the template format below:\n" +
            "\"\"\"Chapter 1: Introduction to Neuroscience Learnings and Organizational Development (433.12 - 2350.67) \n" +
            "1. Erwin Valencia shares the purpose of understanding neuroscience and its application in organizational development. \n" +
            "2. Discussion on creating a leadership coaching program around the idea of flow for remote and hybrid teams. \n" +
            "3. Explanation of a platform that analyzes meeting recordings for team synchronization and behavioral recommendations. \"\"\"\n" +
            "\n" +
            "Emphasis: Only output 1 to 2 chapters. Make sure that the start time is before the end time for each chapter. Do not output intermediate processes, only the final output format. Strictly follow the template format for output. Do not output any notes or comments. Ensure that each chapter lists only 3 bullet points.\n" +
            "\n" +
            "Details of the process instruction are as follows:\n" +
            "1.Divide the text into logical paragraphs as 1 to 2 chapters. Each chapter is based on consecutive lines in the text. Chapters should revolve around themes or ideas. Ensure there is no repetition between chapters and remove unnecessary details. The chapters should cover all the time, and the time needs to be evenly distributed in each chapter. \n" +
            "2.Output the start and end times for each chapter using the time mentioned in the text. The start time for each chapter is the <Start Time> of the first line included in the chapter, and the end time for each chapter is the <End Time> of the last line included in the chapter. The final output should show time ranges like this: \"(start time of the chapter - end time of chapter)\". Ensure that the time range falls within the <Start Time> of the first line and the <End Time> of the last line included in the text.\n" +
            "3.Summarize each chapter and extract its core ideas or information in one sentence. Ensure the summaries very accurately capture the main themes and content of the text. Each chapter's summarization is like \"Chapter 1: Introduction to Neuroscience Learnings and Organizational Development\". \n" +
            "4. For each chapter, list 3 bullet points in the order of 1, 2, 3, with each bullet point beginning with 1, 2, or 3, summarizing the main topics and key messages of the discussion.\n" +
            "\n" +
            "Output template:\n" +
//            "\"\"\"" +
            "Chapter 1: Introduction to Neuroscience Learnings and Organizational Development (433.12 - 2350.67) \n" +
            "1. Erwin Valencia shares the purpose of understanding neuroscience and its application in organizational development. \n" +
            "2. Discussion on creating a leadership coaching program around the idea of flow for remote and hybrid teams. \n" +
            "3. Explanation of a platform that analyzes meeting recordings for team synchronization and behavioral recommendations. \n" +
            "Chapter 2: Team Synchrony Analysis and Feedback Recommendations (2353.46 - 3152.35) \n" +
            "1. Details on how the platform extracts learnings from meeting recordings and offers suggestions for enhancing team dynamics. \n" +
            "2. Implementation of feedback mechanisms based on cognitive synchrony and behavioral patterns. \n" +
            "3. Introduction of a data analysis pathway to track team synchrony and connect it to key performance indicators over time.";
//            +" \"\"\"";

    private static final String PROMPT_SUF = "Your Role: \n" +
            "As a highly skilled artificial intelligence expert trained in language comprehension and summarization, you need to comprehensively read and understand several chapters of an ordered summary of an online meeting content. Then, consolidate them into 3 or 4 higher-level chapters.\n" +
            "\n" +
            "You will need to understand and integrate everything I give you into 3 or 4 higher-level chapters, summarize these higher-level chapters, and provide 3 bullet points for each higher-level chapter.\n" +
            "\n" +
            "For each output chapter, strictly follow the template format below:\n" +
            "\"\"\"Chapter 1: Introduction to Neuroscience Learnings and Organizational Development (433.12 - 2350.67) \n" +
            "1. Erwin Valencia shares the purpose of understanding neuroscience and its application in organizational development. \n" +
            "2. Discussion on creating a leadership coaching program around the idea of flow for remote and hybrid teams. \n" +
            "3. Explanation of a platform that analyzes meeting recordings for team synchronization and behavioral recommendations. \"\"\"\n" +
            "\n" +
            "Emphasis: Only output 3 to 4 chapters across the whole range. Make sure that the start time is before the end time for each output chapter. Do not output intermediate processes, only the final output format. Strictly follow the template format for output. Do not output any notes or comments. Ensure that each output chapter lists only 3 bullet points.\n" +
            "\n" +
            "Details of the process instruction are as follows:\n" +
            "1.Further combine the chapters into 3 to 4 higher level chapters. The new chapters should revolve around themes or ideas. Ensure there is no repetition between chapters and remove unnecessary details. The chapters should cover all the time, and the time needs to be evenly distributed in each chapter. \n" +
            "2.Output the start and end times for each new chapter based on the old chapters, covering all the time ranges. Try to spread out the time for each chapter as evenly as possible. The final output should show time ranges like this: \"(start time of the new chapter - end time of the new chapter)\".\n" +
            "3.Summarize each chapter and extract its core ideas or information in one sentence. Ensure the summaries very accurately capture the main themes and content of the text. Each chapter's summarization is like \"Chapter 1: Introduction to Neuroscience Learnings and Organizational Development\". \n" +
            "4. For each chapter, list 3 bullet points in the order of 1, 2, 3, with each bullet point beginning with 1, 2, or 3, summarizing the main topics and key messages of the discussion.\n" +
            "\n" +
            "Output Template: \n" +
//            "\"\"\"" +
            "Chapter 1: Introduction to Neuroscience Learnings and Organizational Development (433.12 - 2350.67) \n" +
            "1. Erwin Valencia shares the purpose of understanding neuroscience and its application in organizational development. \n" +
            "2. Discussion on creating a leadership coaching program around the idea of flow for remote and hybrid teams. \n" +
            "3. Explanation of a platform that analyzes meeting recordings for team synchronization and behavioral recommendations. \n\n" +
            "Chapter 2: Team Synchrony Analysis and Feedback Recommendations (2353.46 - 3152.35) \n" +
            "1. Details on how the platform extracts learnings from meeting recordings and offers suggestions for enhancing team dynamics. \n" +
            "2. Implementation of feedback mechanisms based on cognitive synchrony and behavioral patterns. \n" +
            "3. Introduction of a data analysis pathway to track team synchrony and connect it to key performance indicators over time." ;
//            " \"\"\"";

    private String PROMPT_TEAM_HIGHLIGHT = "role\n" +
            "You, as a highly skilled AI expert trained in language comprehension and summarization, are able to fully read and understand and accurately analyze long segments of conversational text.\n" +
            "\n" +
            "objective\n" +
            "After fully comprehending the text of the conversation, select three highlights of about 30 seconds each, known as team highlights, and output the start and end times, as well as a short description of each highlight.\n" +
            "\n" +
            "style\n" +
            "Must be accurate and robust, no matter how many times you output it, the result will be the same.\n" +
            "\n" +
            "tone\n" +
            "Accurate and persuasive\n" +
            "\n" +
            "audience\n" +
            "People who are interested in the team's online communication\n" +
            "\n" +
            "response\n" +
            "Include only the start time, the end time and a description of the clip. Ensure the timestamps are not too concentrated.\n" +
            "The template is as follows:\n" +
            "startTime:46.92\n" +
            "endTime:76.92\n" +
            "DESCRIPTION: snippet introduces the product's current popularity in the market and emphasizes the future development strategy.\n" +
            "\n" +
            "startTime:187.72\n" +
            "endTime:217.54\n" +
            "DESCRIPTION: this section, team members discuss the challenges faced during the latest project and propose innovative solutions.\n" +
            "\n" +
            "startTime:413.0\n" +
            "endTime:443.3\n" +
            "DESCRIPTION: conversation here focuses on the team's recent achievements, highlighting key milestones and future goals.";
//    private String PROMPT_TEAM_HIGHLIGHT = "# role #\n" +
//            "You, as a highly skilled AI expert trained in language comprehension and summarization, are able to fully read and understand and accurately analyze long segments of conversational text.\n" +
//            "\n" +
//            "# objective #\n" +
//            "After fully comprehending the text of the conversation, select three highlights of about 30 seconds each, known as team highlights, and output the start and end times, as well as a short description of each highlight.\n" +
//            "\n" +
//            "# style #\n" +
//            "Must be accurate and robust, no matter how many times you output it, the result will be the same.\n" +
//            "\n" +
//            "# tone #\n" +
//            "Accurate and persuasive\n" +
//            "\n" +
//            "# audience #\n" +
//            "People who are interested in the team's online communication\n" +
//            "\n" +
//            "# response #\n" +
//            "Include only the start time, the end time and a description of the clip\n" +
//            "The template is as follows:\n" +
//            "startTime:25.23\n" +
//            "endTime:55.16\n" +
//            "DESCRIPTION:This snippet introduces the product's current popularity in the market and emphasizes the future development strategy.";

    private String PROMPT_USER_HIGHLIGHT = "# role #\n" +
            "You, as a highly skilled AI expert trained in language comprehension and summarization, are able to fully read and understand and accurately analyze long segments of conversational text.\n" +
            "\n" +
            "# objective #\n" +
            "After fully understanding and comprehending the dialog text, select three highlights of about 30 seconds each for each speaker from all the information, not just the first half, and if a speaker doesn't have a highlight, select the better 30 seconds of what he said and output it, outputting the speaker, the start time, and the end time, and providing a short Description.\n" +
            "\n" +
            "# style #\n" +
            "Must be accurate and robust, no matter how many times you output this type of result.\n" +
            "\n" +
            "# tone #\n" +
            "Accurate and persuasive\n" +
            "\n" +
            "# audience #\n" +
            "People who are interested in the team's online communication\n" +
            "\n" +
            "# response #\n" +
            "Containing only the SPEAKER number, start time, end time, and a description of the segment, to be output for all attendees, the specific template for one of the attendees is as follows:\n" +
            "speaker01\n" +
            "start time: 25.23\n" +
            "end time: 55.16\n" +
            "Description: This clip focuses on the current hotness of the product in the market and emphasizes the future development strategy.\n" +
            "\n" +
            "speaker01\n" +
            "start time：598.78\n" +
            "end time: 628.16\n" +
            "Description: This clip describes the recent problems in the personnel department and proposes solutions to them.\n" +
            "\n" +
            "speaker01\n" +
            "start time：1945.45\n" +
            "end time:1975.26\n" +
            "Description: This clip summarizes the meeting and presents the next steps.\n" +
            "\n" +
            "speaker02\n" +
            "start time: 25.23\n" +
            "end time: 55.16\n" +
            "Description: This clip focuses on the current hotness of the product in the market and emphasizes the future development strategy.\n" +
            "\n" +
            "speaker02\n" +
            "start time：598.78\n" +
            "end time: 628.16\n" +
            "Description: This clip describes the recent problems in the personnel department and proposes solutions to them.\n" +
            "\n" +
            "speaker02\n" +
            "start time：1945.45\n" +
            "end time:1975.26\n" +
            "Description: This clip summarizes the meeting and presents the next steps.";

    private String RADAR_TRUST_PROMPT = "Role: Trust Assessment Expert\n" +
            "Background: Your task is to evaluate the trust levels among speakers in a meeting transcript to quantify interpersonal trust.\n" +
            "Profile: You are a professional assessment expert proficient in analyzing dialogue content, identifying key details, and assessing trust dynamics.\n" +
            "Skills: Dialogue text analysis, key information extraction, trust assessment.\n" +
            "Goals: Develop a method to assess trust levels in meeting transcripts and deliver a rating ranging from 0 to 100.\n" +
            "Constraints: Ratings must be grounded in the significant information derived from the meeting dialogue.\n" +
            "Output Format: Trust rating only, on a scale from 0 to 100.\n" +
            "Workflow:\n" +
            "\n" +
            "Read and comprehend the entire meeting transcript.\n" +
            "Identify crucial information and arguments present in the transcript.\n" +
            "Analyze the pertinent information gleaned from the meeting dialogue.\n" +
            "Based on the analysis, provide a trust rating.\n" +
            "Initialization: Welcome to the Trust Assessment Service. Please upload the meeting transcript you wish to evaluate for trust levels. I will proceed to provide you with a professional trust rating based on the analysis.\n" +
            "Please upload the meeting transcript to initiate the trust assessment.\n" +
            "\n" +
            "The output sample is listed below:\n" +
            "Trust Rating:XX(XX is a number between 0 and 100)";

    @Autowired
    NlpSummaryService nlpSummaryService;

    @Autowired
    SummaryMapper summaryMapper;

    @Autowired
    SectionMapper sectionMapper;

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    RadarMapper radarMapper;

    @Autowired
    MeetingService meetingService;

    @Autowired
    PieEmotionService pieEmotionService;

    @Autowired
    RadarService radarService;

    @Autowired
    WordInfoService wordInfoService;

    @Autowired
    HeatmapService heatmapService;

    @Async("taskExecutor")
    @Retryable(
            value = { Exception.class }, // 指定触发重试的异常类型
            maxAttempts = MAX_RETRIES, // 最大重试次数
            backoff = @Backoff(delay = 1000) // 重试间隔
    )
    @Override
    public List<NlpSummary> summaryNlp(List<String[]> nlpData, Long meetingID, HashMap<String, Object> deleteMap) {
        List<NlpSummary> nlpSummaryList = new ArrayList<>();
        StringBuilder contents = new StringBuilder();
        List<String> contentList = new ArrayList<>();
        int count = 0;
        for (String[] nlpDatum : nlpData) {
            StringBuilder nlp = new StringBuilder();
            for (String n : nlpDatum) {
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
    public void summaryNlpText(List<String[]> nlpData, List<NlpTable> nlpTables, Long meetingID, HashMap<String, Object> deleteMap) {
        int maxRetries = 1; // 最大重试次数
        int retryCount = 0; // 当前重试次数计数器
//        while (true) {
        try {
            StringBuilder content = new StringBuilder();
            for (String[] nlpDatum : nlpData) {
                StringBuilder nlp = new StringBuilder();
                for (String n : nlpDatum) {
                    nlp.append(n).append(" ");
                }
                content.append(nlp).append("\n");
            }

            //start
            // 拼接文件内容和消息
            String finalMessage = content.toString();

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

    @Override
    public double processRadarTrust(Long meetingID) throws IOException {
        log.info("[GptServiceImpl][processRadarTrust] meetingID :{}", meetingID);
        String fileName = "nlp_result.txt";
        List<String[]> nlpData = amazonUploadService.readNlpLine(fileName, Long.toString(meetingID));
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Speaker\tStart\tEnd\tSentence\tEmotion\n");
        int count = 0;
        for (int i = 0; i < nlpData.size(); i++) {
            String[] entry = nlpData.get(i);
            if (entry.length > 4) {
                sb.append(entry[0])
                        .append("\t")
                        .append(entry[1])
                        .append("\t")
                        .append(entry[2])
                        .append("\t")
                        .append(entry[3])
                        .append("\t")
                        .append(entry[4])
                        .append("\n");
                count++;
            }

            // 每80行或者在最后一次循环时处理
            if (count == 80 || (i == nlpData.size() - 1 && count > 40)) {
                result.add(sb.toString());
                sb = new StringBuilder("Speaker\tStart\tEnd\tSentence\tEmotion\n"); // 重置StringBuilder
                count = 0; // 重置计数器
            }
        }

        // 只有一条数据
        if (result.size() == 0) {
            result.add(sb.toString());
        }

        for (int i = 0; i < nlpData.size(); i++) {
            String[] entry = nlpData.get(i);
            for (String s : entry) {
                sb.append(s).append("\t");
            }
            sb.append("\n");
        }

        // 只有一条
        if (result.size() == 0) {
            result.add(sb.toString());
        }

        List<Double> radarScore = new ArrayList<>();
        for (String s : result) {
            String message = sendMessageToGpt(s, RADAR_TRUST_PROMPT);
            message = message.split("\n")[0];
            Double trustScore = Double.valueOf(message.split(":")[1].trim());

            radarScore.add(trustScore / 100.0);
        }

        double score = radarScore.stream()
                .mapToDouble(Double::doubleValue) // 转换为DoubleStream
                .average()                         // 计算平均值
                .orElse(0.0);
        LambdaUpdateWrapper<Radar> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Radar::getMeeting_id, meetingID)
                .eq(Radar::getK, "Trust")
                .set(Radar::getV, score);
        radarMapper.update(null, updateWrapper);
        return score;
    }

    @Override
    @Async
    public List<String> processNLPData(List<String[]> nlpData, Long meetingID) {
        log.info("[GptServiceImpl][processNLPData] meetingID :{}", meetingID);
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < nlpData.size(); i++) {
            String[] entry = nlpData.get(i);
            if (entry.length > 4) {
                sb.append(entry[0])
                        .append("\t")
                        .append(entry[1])
                        .append("\t")
                        .append(entry[2])
                        .append("\t")
                        .append(entry[3])
                        .append("\n");
                count++;
            }

            // 每100行或者在最后一次循环时处理
            if (count == 100 || (i == nlpData.size() - 1 && count > 40)) {
                result.add(sb.toString());
                sb = new StringBuilder(); // 重置StringBuilder
                count = 0; // 重置计数器
            }
        }

        // 只有一条
        if (result.size() == 0) {
            result.add(sb.toString());
        }
        List<String> summary = new ArrayList<>();
        for (String s : result) {
            String messageP = sendMessageToGpt(s, PROMPT_PRE);
            String[] split = messageP.split("\n\n");
            for (String content : split) {
                if (content.startsWith("Chapter")) {
                    summary.add(content + "\n");
                }
            }
        }
        LambdaQueryWrapper<SummaryTable> queryWrapper = new LambdaQueryWrapper<SummaryTable>()
                .eq(SummaryTable::getMeeting_id, meetingID);
        if (summary.size() > 1) {
            StringBuilder messageS = new StringBuilder();
            for (String s : summary) {
                messageS.append(s).append("\n");
            }
            String message = sendMessageToGpt(messageS.toString(), PROMPT_SUF);
            String[] split = message.split("\n\n");
            int ind = 0;
            //TODO:优化配置
            summaryMapper.delete(queryWrapper);
            for (int i = 0; i < split.length; i++) {
                if (split[i].startsWith("Chapter")) {
                    SummaryTable summaryTable = new SummaryTable(meetingID, ind, split[ind++]);
                    summaryMapper.insert(summaryTable);
                }
            }
            log.info("[GptServiceImpl][processNLPData] message :{}", message);
        } else {
            summaryMapper.delete(queryWrapper);
            for (int i = 0; i < summary.size(); i++) {
                SummaryTable summaryTable = new SummaryTable(meetingID, i, summary.get(i));
                summaryMapper.insert(summaryTable);
            }
        }
        return result;
    }

    @Override
    @Async
    public void processAllHighlight(List<String[]> nlpData, Long meetingID) {
        log.info("[GptServiceImpl][processAllHighlight] meetingID :{}", meetingID);
        processTeamHighlight(nlpData, meetingID);
        processUserHighlight(nlpData, meetingID);

    }

    private void processTeamHighlight(List<String[]> nlpData, Long meetingID) {
        log.info("[GptServiceImpl][processTeamHighlight] meetingID :{}", meetingID);
        List<String> data = concatenateNLPData(nlpData, 120);
        List<String> result = new ArrayList<>();
        for (String datum : data) {
            String message = sendMessageToGpt(datum, PROMPT_TEAM_HIGHLIGHT);
            result.add(message);
        }
        List<Section> sectionList = parse3TeamHighlight(result, meetingID);
        LambdaQueryWrapper<Section> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Section::getMeeting_id, meetingID)
                        .eq(Section::getUsers, CsvConstants.USER_TEAM);
        sectionMapper.delete(queryWrapper);
        sectionList.forEach(
                section -> sectionMapper.insert(section)
        );
    }

    private void processUserHighlight(List<String[]> nlpData, Long meetingID) {
        log.info("[GptServiceImpl][processUserHighlight] meetingID :{}", meetingID);
        List<String> data = concatenateNLPData(nlpData, 120);
        List<String> result = new ArrayList<>();
        for (String datum : data) {
            String message = sendMessageToGpt(datum, PROMPT_USER_HIGHLIGHT);
            result.add(message);
        }
        List<Section> sectionList = parse3UserHighlight(result, meetingID);
        LambdaQueryWrapper<Section> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Section::getMeeting_id, meetingID)
                .ne(Section::getUsers, CsvConstants.USER_TEAM);
        sectionMapper.delete(queryWrapper);
        sectionList.forEach(
                section -> sectionMapper.insert(section)
        );
    }

    private List<Section> parse3UserHighlight(List<String> result, Long meetingID) {
        log.info("[GptServiceImpl][parse3UserHighlight] meetingID :{}", meetingID);
        List<Section> sectionList = new ArrayList<>();
        HashMap<String, List<Section>> map = new HashMap<>();
        for (String highlight : result) {
            String[] split = highlight.split("\n\n");
            for (String content : split) {
                String[] lines = content.split("\n");
                boolean valid = lines.length == 4 &&
                        lines[1].split(":").length == 2 &&
                        lines[2].split(":").length == 2 &&
                        lines[3].split(":").length == 2;
                if (valid) {
                    String speaker = lines[0];
                    double startTime = Double.parseDouble(lines[1].split(":")[1].trim());
                    double endTime = Double.parseDouble(lines[2].split(":")[1].trim());
                    String description = lines[3].split(":")[1].trim();
                    Section section = new Section(meetingID, startTime, endTime, speaker, description);
                    map.computeIfAbsent(speaker, k -> new ArrayList<>()).add(section);
                }
            }
        }
        for (Map.Entry<String, List<Section>> entry : map.entrySet()) {
            List<Section> value = entry.getValue();
            List<Section> collect = value.stream()
                    .sorted((a, b) -> Math.random() > 0.5 ? 1 : -1) // 随机打乱顺序
                    .limit(3) // 选择前三个元素
                    .collect(Collectors.toList());
            sectionList.addAll(collect);
        }
        return sectionList;

    }

    private List<Section> parse3TeamHighlight(List<String> result, Long meetingID) {
        log.info("[GptServiceImpl][parse3TeamHighlight] meetingID :{}", meetingID);
        List<Section> sectionList = new ArrayList<>();
        for (String highlight : result) {
            String[] split = highlight.split("\n\n");
            for (String content : split) {
                String[] lines = content.split("\n");
                if (lines.length == 3) {
                    if (lines[0].split(":").length == 2
                    && lines[1].split(":").length == 2
                    && lines[2].split(":").length == 2) {
                        double startTime = Double.parseDouble(lines[0].split(":")[1].trim());
                        double endTime = Double.parseDouble(lines[1].split(":")[1].trim());
                        String description = lines[2].split(":")[1].trim();
                        Section section = new Section(meetingID, startTime, endTime, CsvConstants.USER_TEAM, description);
                        sectionList.add(section);
                    }
                }
            }
        }
        return sectionList.stream()
                .sorted((a, b) -> Math.random() > 0.5 ? 1 : -1) // 随机打乱顺序
                .limit(3) // 选择前三个元素
                .collect(Collectors.toList());

    }

    @Override
    public String sendMessageToGpt(String content, String systemPrompt) {
        log.info("[GptServiceImpl][sendMessageToGpt] start!");
        //start
        // 拼接文件内容和消息
        System.out.println(content);

        RestTemplate restTemplate = new RestTemplate();
        // Set headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        Message m = new Message("user", content);
        Message system = new Message("system", systemPrompt);
        List<Message> list = new ArrayList<>();
        list.add(system);
        list.add(m);
//        RequestMessage r = new RequestMessage("gpt-3.5-turbo-16k", list);
        RequestMessage r = new RequestMessage("ft:gpt-4o-2024-08-06:syneurgy::ARIkwfO2", list);
        // Create and send the request
        org.springframework.http.HttpEntity<RequestMessage> request =
                new org.springframework.http.HttpEntity<>(r, headers);

        ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
        ChatCompletion body = response.getBody();
        ChatCompletion.Choice choice = body.getChoices()[0];
        String result = choice.getMessage().getContent();
        return result;
    }

    @Override
    public String sendMessageToGpt4(String content, String systemPrompt) {
        log.info("[GptServiceImpl][sendMessageToGpt4] start!");
        //start
        RestTemplate restTemplate = new RestTemplate();
        // Set headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        Message m = new Message("user", content);
        Message system = new Message("system", systemPrompt);
        List<Message> list = new ArrayList<>();
        list.add(system);
        list.add(m);
        RequestMessage r = new RequestMessage("gpt-4o-2024-08-06", list, 0);
//        RequestMessage r = new RequestMessage("ft:gpt-4o-2024-08-06:syneurgy::ARIkwfO2", list, 0);
        // Create and send the request
        org.springframework.http.HttpEntity<RequestMessage> request =
                new org.springframework.http.HttpEntity<>(r, headers);

        ResponseEntity<ChatCompletion> response = restTemplate.postForEntity(url, request, ChatCompletion.class);
        ChatCompletion body = response.getBody();
        ChatCompletion.Choice choice = body.getChoices()[0];
        String result = choice.getMessage().getContent();
        return result;
    }

    @Override
    public RestResult queryByTeamIdAndLabel(Long teamId, int label) {
        LambdaQueryWrapper<GptSummary> queryWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, teamId)
                        .eq(GptSummary::getLabel, label);
        GptSummary summary = getOne(queryWrapper);
        GptResultVO gptResultVO = new GptResultVO();
        if (summary != null) {
            gptResultVO.setTeamId(teamId);
            gptResultVO.setResult(summary.getV());
        }
        return RestResult.success().data(gptResultVO);
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

    /**
     * 拼接NlpData
     *
     * @param nlpData 原始数据列表
     * @param n       每n个String[]拼接成一个String
     * @return 拼接后的String列表
     */
    private List<String> concatenateNLPData(List<String[]> nlpData, int n) {
        List<String> concatenatedData = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String[] dataArray : nlpData) {
            // 拼接前四个元素
            for (int i = 0; i < 4; i++) {
                sb.append(dataArray[i]);
                if (i < 3) {
                    sb.append("\t");
                }
            }
            count++;
            if (count % n == 0) {
                // 达到每n个String[]拼接成一个String的条件，添加到结果列表中
                concatenatedData.add(sb.toString());
                // 清空StringBuilder以准备下一组拼接
                sb.setLength(0);
            } else {
                // 没有达到拼接条件，继续拼接
                sb.append("\n");
            }
        }

        // 大约25行
        if (concatenatedData.size() == 0 || sb.length() > 3000) {
            concatenatedData.add(sb.toString());
        }

        return concatenatedData;
    }

    @Override
    public void processTeamAndMeetingGptData(GptRequestInfoVO info) {
        log.info("[GptServiceImpl][processTeamAndMeetingGptData] info:{}", info);
        HashMap<String, Double> score = meetingService.getAverageLatestFiveScore(info.getTeamId());
        //part1、3
        info.setSynchronyScore(score.get("synchronyScore"));
        info.setBehavior(score.get("behavior"));
        info.setBody(score.get("body"));
        info.setBrain(score.get("brain"));
        processGlobalTeamSynchrony(info);
        processGlobalTeamPerformance(info);

        //part2
        MeetingSummaryVO globalTeamMetricsInfo = meetingService.getGlobalTeamMetricsInfo(info.getTeamId());
        info.setMeetingSummaryVO(globalTeamMetricsInfo);
        processGlobalTeamMetrics(info);

        //part4
        List<MeetingTable> allDateMeeting = meetingService.getLatestFiveMeetingByTeam(info.getTeamId(), "AllDates");
        List<Double> scoreList = allDateMeeting.stream()
                .map(MeetingTable::getTotal_score)
                .filter(s -> s != null)
                .collect(Collectors.toList());
        info.setProgressData(scoreList);
        processGlobalTeamProgress(info);

        //part6
        MeetingTable meetingTable = meetingService.getByMeetingId(info.getMeetingId());
        info.setMeetingScore(meetingTable);
        processGlobalTeamPerformanceForMeeting(info);

        //part7
        List<PieEmotionVO> emotionList = pieEmotionService.findEmotion(info.getMeetingId());
        Double positive = 1.0;
        Double neutral = 98.0;
        Double negative = 1.0;
        for (PieEmotionVO emotionVO : emotionList) {
            String emotion = emotionVO.getEmotion();
            if (emotion.equals("neutral")) {
                neutral = emotionVO.getEmotion_time_rate() * 100;
            } else if (emotion.equals("positive")) {
                positive = emotionVO.getEmotion_time_rate() * 100;
            } else if (emotion.equals("negative")) {
                negative = emotionVO.getEmotion_time_rate() * 100;
            }
        }
        info.setPositive(positive);
        info.setNegative(negative);
        info.setNeutral(neutral);
        processTeamSentiment(info);

        //part8
        List<RadarVO> radarList = radarService.findKV(info.getMeetingId());
        Map<String, Double> radarMap = radarList.stream()
                .collect(Collectors.toMap(RadarVO::getK, RadarVO::getV));
        info.setDimensions(radarMap);
        processTeamDimensions(info);

        //part9
        List<HeatmapVO> heatmapVOList = heatmapService.findHeatmap(info.getMeetingId());
        int maxImg = heatmapVOList.stream().mapToInt(HeatmapVO::getImg).max().orElse(0);
        List<HeatmapVO> maxPoints = new ArrayList<>();
        for (HeatmapVO vo : heatmapVOList) {
            if (vo.getImg() == maxImg) {
                maxPoints.add(vo);
            }
        }

        // 计算平均x和y
        int avgX = (int)maxPoints.stream().mapToInt(HeatmapVO::getX).average().orElse(150);
        int avgY = (int)maxPoints.stream().mapToInt(HeatmapVO::getY).average().orElse(150);

        int xMax = heatmapVOList.stream().mapToInt(HeatmapVO::getX).max().orElse(150);
        int xMin = heatmapVOList.stream().mapToInt(HeatmapVO::getX).min().orElse(150);
        int yMin = heatmapVOList.stream().mapToInt(HeatmapVO::getY).min().orElse(150);
        int yMax = heatmapVOList.stream().mapToInt(HeatmapVO::getY).max().orElse(150);
        info.setLeft(yMin + 150 - avgY);
        info.setRight(yMax + 150 - avgY);
        info.setTop(xMin + 150 - avgX);
        info.setBottom(xMax + 150 - avgX);
        processTeamHeatMap(info);

        //part11
        LambdaQueryWrapper<WordInfo> queryWrapper = new LambdaQueryWrapper<WordInfo>()
                .eq(WordInfo::getMeetingId, info.getMeetingId());
        List<WordInfo> wordInfoList = wordInfoService.list(queryWrapper);
        info.setWordInfoList(wordInfoList);
        processWordsPerMinute(info);
    }

    @Override
    public RestResult queryByMeetingIdAndLabel(Long meetingId, int label) {
        LambdaQueryWrapper<GptSummary> queryWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getMeetingId, meetingId)
                .eq(GptSummary::getLabel, label);
        GptSummary summary = getOne(queryWrapper);
        GptResultVO gptResultVO = new GptResultVO();
        if (summary != null) {
            gptResultVO.setMeetingId(meetingId);
            gptResultVO.setResult(summary.getV());
        }
        return RestResult.success().data(gptResultVO);
    }

    private void processGlobalTeamSynchrony(GptRequestInfoVO info) {
        log.info("[GptServiceImpl][processGlobalTeamSynchrony] info:{}", info);
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "Global Team Synchrony Score: [" + info.getSynchronyScore() + "]\n" +
                "This global team synchrony score is indicative of how much synchrony the team has been exhibiting meeting to meeting. It is a combination of behavioral synchrony, cognitive synchrony, emotional synchrony, and linguistic synchrony, that is, brain, body, and behavioral alignment. Here are the ranges of the global team synchrony score data.\n" +
                "1. Low Global Team Synchrony (0-33)\n" +
                "Description:\n" +
                "A low synchrony score may indicate significant misalignment within the team. This can manifest as poor communication, low engagement, conflicting goals, and lack of cooperation. However, while low team synchrony typically indicates misalignment and potential dysfunction, it can sometimes have positive aspects, particularly in fostering diversity of thought and innovation.\n" +
                "2. Medium Global Team Synchrony (34-66)\n" +
                "Description:\n" +
                "A medium synchrony score indicates moderate alignment and cooperation within the team. There are areas of strength, but also some aspects that need improvement.\n" +
                "3. High Global Team Synchrony (67-100)\n" +
                "Description:\n" +
                "A high synchrony score indicates strong alignment and cooperation within the team. Communication is clear, engagement is high, and team members are working harmoniously towards shared goals. While high team synchrony generally indicates effective collaboration and alignment, it can sometimes lead to complacency and groupthink if not managed carefully.\n" +
                "Objective: Using the provided transcript and this global team synchrony data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processGlobalTeamSynchrony] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Global Team Synchrony", message, 1);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, info.getTeamId())
                .eq(GptSummary::getLabel, 1);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processGlobalTeamMetrics(GptRequestInfoVO info) {
        MeetingSummaryVO meetingSummaryVO = info.getMeetingSummaryVO();
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Global Team Metrics\n" +
                "       Engagement: [" + meetingSummaryVO.getEngagement() + "]\n" +
                "       Alignment: [" + meetingSummaryVO.getAlignment() + "]\n" +
                "       Agency: [" + meetingSummaryVO.getAgency() + "]\n" +
                "       Burnout: [" + meetingSummaryVO.getBurnout() + "]\n" +
                "       Stress: [" + meetingSummaryVO.getStress() + "]\n" +
                "Objective: Using the provided transcript and this global team metrics data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";

        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processGlobalTeamMetrics] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Global Team Metrics", message, 2);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, info.getTeamId())
                .eq(GptSummary::getLabel, 2);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processGlobalTeamPerformance(GptRequestInfoVO info) {
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Global Team Performance (Synchrony Scores):\n" +
                "       Overall Synchrony: [" + info.getSynchronyScore() + "]\n" +
                "       Brain (Cognitive Alignment): [" + info.getBrain() + "]\n" +
                "       Body (Physiological and Emotional Alignment): [" + info.getBody() + "]\n" +
                "       Behavior (Gestural and Language Alignment): [" + info.getBehavior() + "]\n" +
                "\n" +
                "Objective: Using the provided transcript and this global team performance data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processGlobalTeamPerformance] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Global Team Performance", message, 3);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, info.getTeamId())
                .eq(GptSummary::getLabel, 3);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processGlobalTeamProgress(GptRequestInfoVO info) {
        List<Double> progressData = info.getProgressData();
        String result  = progressData.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Global Team Progress Data: [" + result + "]\n" +
                "\n" +
                "Objective: Using the provided transcript and this global team progress data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics). "
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processGlobalTeamProgress] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Global Team Progress", message, 4);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, info.getTeamId())
                .eq(GptSummary::getLabel, 4);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processGlobalTeamPerformanceForMeeting(GptRequestInfoVO info) {
        MeetingTable meetingScore = info.getMeetingScore();
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Team Performance (Synchrony Scores):\n" +
                "       Overall Synchrony: [" + meetingScore.getTotal_score() + "]\n" +
                "       Brain (Cognitive Alignment): [" + meetingScore.getBrain_score() + "]\n" +
                "       Body (Physiological and Emotional Alignment): [" + meetingScore.getBody_score() + "]\n" +
                "       Behavior (Gestural and Language Alignment): [" + meetingScore.getBehavior_score() + "]\n" +
                "\n" +
                "Objective: Using the provided transcript and this team performance data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processGlobalTeamPerformanceForMeeting] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Global Team Performance for Meeting", message, 6);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getTeam_id, info.getTeamId())
                .eq(GptSummary::getLabel, 6);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processTeamSentiment(GptRequestInfoVO info) {
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Team Sentiment:\n" +
                "       Positive: [" + info.getPositive() + "%]\n" +
                "       Neutral: [" + info.getNeutral() + "%]\n" +
                "       Negative: [" + info.getNegative() + "%]\n" +
                "\n" +
                "Objective: Using the provided transcript and this team sentiment data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processTeamSentiment] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Team Sentiment", message, 7);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getMeetingId, info.getMeetingId())
                .eq(GptSummary::getLabel, 7);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processTeamDimensions(GptRequestInfoVO info) {
        Map<String, Double> dimensions = info.getDimensions();
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Dimensions:\n" +
                "       Psychological Safety: [" + dimensions.get("Psychological Safety") + "]\n" +
                "       Trust: [" + dimensions.get("Trust") + "]\n" +
                "       Equal Participation: [" + dimensions.get("Equal Participation") + "]\n" +
                "       Task Engagement: [" + dimensions.get("Absorption or Task Engagement") + "]\n" +
                "       Shared Goal Commitment: [" + dimensions.get("Shared Goal Commitment")+ "]\n" +
                "       Enjoyment: [" + dimensions.get("Enjoyment") + "]\n" +
                "\n" +
                "Objective: Using the provided transcript and this team dimensions data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form. \n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics). "
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processTeamDimensions] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Team Dimensions", message, 8);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getMeetingId, info.getMeetingId())
                .eq(GptSummary::getLabel, 8);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processTeamHeatMap(GptRequestInfoVO info) {
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   Heatmap range:\n" +
                "       Most left point: [" + info.getLeft() + "]\n" +
                "       Most right point: [" + info.getRight() + "]\n" +
                "       Most top point: [" + info.getTop() + "]\n" +
                "       Most bottom point: [" + info.getBottom() + "]\n" +
                "\n" +
                "Objective: In a heatmap coordinate system (left to right are from 0 to 300, top to bottom are from 0 to 300), the top right corner means positive and excited, indicating states of high engagement and positive emotions, such as feeling inspired and energized; the top left corner means negative and excited, reflecting negative emotions combined with high excitement, such as feeling anxious or stressed; the bottom right corner means positive and calm representing states of positive emotions but lower excitement levels, such as feeling content and harmonious; the bottom left corner means negative and calm, depicting negative emotions with low excitement, such as feeling disengaged or apathetic.\n" +
                "\n" +
                "Using the provided transcript and this team heatmap data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics). "
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processTeamHeatMap] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Team HeatMap", message, 9);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getMeetingId, info.getMeetingId())
                .eq(GptSummary::getLabel, 9);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private void processWordsPerMinute(GptRequestInfoVO info) {
        List<WordInfo> wordInfoList = info.getWordInfoList();
        String rates = wordInfoList.stream()
                .map(wi -> String.format("%.2f%%", wi.getRate()))
                .collect(Collectors.joining(", ", "[", "]"));

        String speeds = wordInfoList.stream()
                .map(wi -> wi.getSpeed().intValue())
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
        double averageSpeed = wordInfoList.stream()
                .mapToDouble(WordInfo::getSpeed)
                .average().orElse(180);
        String result = rates + ", " + speeds;
        String content = "Transcript: " + info.getTranscript() + "\n\n" +
                "Data:\n" +
                "   1.Participation and Words Per Minute (WPM):\n" +
                "       Team Average: [" + averageSpeed + "] WPM\n" +
                "       Individual Participation and WPM: " + result + " \n\n" +
                "Objective: Using the provided transcript and this words per minute data, identify specific key observations and generate general key takeaways about the team's performance. Consider the implications of the metrics on team dynamics, communication, engagement, and overall effectiveness. The team member should be able to understand what all of this data (from transcripts and the metrics provided) implies for the current status of the team, as well as making improvements. Write a single sentence that will be the headline for this key observations and high-level takeaways, ensuring that each word in the headline starts with a capital letter. Then, write 3-5 sentences of key observations and high-level takeaways, in bullet point form.\n\n" +
                "Additionally, suggest three behavior recommendations (ways to improve these particular metrics) for individuals, and three behavioral recommendations for the team (that is, ways to improve these metrics)."
                +"\nFinally, please provide me with the results in JSON format."
                +"\nThe JSON format of the output is as follows:"
                +"\n{\n" +
                "    “Headline”: “”,\n" +
                "    “Key observations and high-level takeaways”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Individual behavior recommendations to improve synchrony metrics”:\n" +
                "    [\n" +
                "    ],\n" +
                "    “Team Behavioral Recommendations”:\n" +
                "    [\n" +
                "    ]\n" +
                "}";
        String message = sendMessageToGpt(content, GLOBAL_TEAM_PROMPT);
        message  = processResultMessage(message);
        log.info("[GptServiceImpl][processWordsPerMinute] message:{}", message);
        GptSummary gptSummary = new GptSummary(info.getMeetingId(), info.getTeamId(), "Words Per Minute", message, 11);
        LambdaQueryWrapper<GptSummary> deleteWrapper = new LambdaQueryWrapper<GptSummary>()
                .eq(GptSummary::getMeetingId, info.getMeetingId())
                .eq(GptSummary::getLabel, 11);
        remove(deleteWrapper);
        save(gptSummary);
    }

    private String processResultMessage(String inputText) {
        // 正则表达式来匹配从 \n\n 到 :\n 之间的任何内容
        Pattern pattern = Pattern.compile("(\\n\\n)(.*?)(:\\n)");
        Matcher matcher = pattern.matcher(inputText);

        StringBuffer result = new StringBuffer();

        // 查找所有匹配并进行处理
        while (matcher.find()) {
            String capturedText = matcher.group(2); // 提取中间内容
            if (capturedText.toLowerCase().contains("behavior")
                    && capturedText.toLowerCase().contains("recommendations")
                    && capturedText.toLowerCase().contains("individuals")) {
                matcher.appendReplacement(result, matcher.group(1) + "Individual Behavior Recommendations" + matcher.group(3));
            } else if (capturedText.toLowerCase().contains("behavior")
                    && capturedText.toLowerCase().contains("recommendations")
                    && capturedText.toLowerCase().contains("team")) {
                matcher.appendReplacement(result, matcher.group(1) + "Team Behavioral Recommendations" + matcher.group(3));
            } else {
                // 不需要替换，继续使用原文
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result); // 添加最后一部分未处理的文本
        return result.toString();
    }
}


