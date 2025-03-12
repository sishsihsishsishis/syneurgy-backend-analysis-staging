package com.aws.sync.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.dto.ReportDataDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.service.GptService;
import com.aws.sync.service.MeetingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/gpt")
public class GptController {

    @Autowired
    GptService gptService;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private MeetingService meetingService;

    @ApiOperation("1_global_team_synchrony")
    @GetMapping("/global-team-synchrony/{teamId}")
    public RestResult getGlobalTeamSynchrony(@PathVariable("teamId") Long teamId) {
        return gptService.queryByTeamIdAndLabel(teamId, 1);
    }

    @ApiOperation("2_global_team_metrics")
    @GetMapping("/global-team-metrics/{teamId}")
    public RestResult getGlobalTeamMetrics(@PathVariable("teamId") Long teamId) {
        return gptService.queryByTeamIdAndLabel(teamId, 2);
    }

    @ApiOperation("3_global_team_performance")
    @GetMapping("/global-team-performance/{teamId}")
    public RestResult getGlobalTeamPerformance(@PathVariable("teamId") Long teamId) {
        return gptService.queryByTeamIdAndLabel(teamId, 3);
    }

    @ApiOperation("4_global_team_progress")
    @GetMapping("/global-team-progress/{teamId}")
    public RestResult getGlobalTeamProgress(@PathVariable("teamId") Long teamId) {
        return gptService.queryByTeamIdAndLabel(teamId, 4);
    }


    @ApiOperation("6_global_team_performance_for_meeting")
    @GetMapping("/global-team-performance-for-meeting/{teamId}")
    public RestResult getGlobalTeamPerformanceForMeeting(@PathVariable("teamId") Long teamId) {
        return gptService.queryByTeamIdAndLabel(teamId, 6);
    }

    @ApiOperation("7_team_sentiment")
    @GetMapping("/team_sentiment/{meetingId}")
    public RestResult getTeamSentiment(@PathVariable("meetingId") Long meetingId) {
        return gptService.queryByMeetingIdAndLabel(meetingId, 7);
    }

    @ApiOperation("8_team_dimensions")
    @GetMapping("/team_dimensions/{meetingId}")
    public RestResult getTeamDimensions(@PathVariable("meetingId") Long meetingId) {
        return gptService.queryByMeetingIdAndLabel(meetingId, 8);
    }

    @ApiOperation("9_team_emotion/excitement_heatmap")
    @GetMapping("/team_heatmap/{meetingId}")
    public RestResult getTeamHeatmap(@PathVariable("meetingId") Long meetingId) {
        return gptService.queryByMeetingIdAndLabel(meetingId, 9);
    }

    @ApiOperation("11_words_per_minute")
    @GetMapping("/words-per-minute/{meetingId}")
    public RestResult getWordsPerMinute(@PathVariable("meetingId") Long meetingId) {
        return gptService.queryByMeetingIdAndLabel(meetingId, 11);
    }

/*    @PostMapping("/save")
    public RestResult sendGptData(@RequestBody GptDataDTO data,
                                  @RequestParam("meetingId") Long meetingId,
                                  @RequestParam("data") MultipartFile file) {
        String PROMPT = "Please create an Executive Summary of this report.\n" +
                " You must include high level observations and thoughts on trends and qualitative assessments of the team and team dynamics, based on the data in the report.\n" +
                "The executive summary must be divided into two parts:\n" +
                "Part 1: Global Summary Data: Summarize the high-level observations, trends, and qualitative assessments based on the following subsections of the report.\n" +
                "The subsections are: Global Team Synchrony Global Team Metrics Global Team Performance Global Team Progress\n" +
                "Part 2: Most Recent Meeting Data: Provide a summary based on the specific data from the single meeting detailed in the following subsections.\n" +
                "The subsections are: Meeting Details and Highlights Meeting Team Synchrony Performance Team Sentiment Team Dimensions Team Emotion/Excitement Heatmap Participation Words Per Minute (WPM) Language Use\n" +
                "As an example, for each subsection please utilize this exact format given these section subheadings:\n" +
                "Global Summary Data:\n" +
                "Global Team Synchrony;\n" +
                "Global Team Metrics;\n" +
                "Global Team Performance;\n" +
                "Global Team Progress;\n" +
                "Most Recent Meeting Data:\n" +
                "Meeting Detail and Highlights;\n" +
                "Meeting Team Synchrony Performance;\n" +
                "Team Sentiment;\n" +
                "Team Dimensions;\n" +
                "Team Emotion / Excitement Heatmap;\n" +
                "Participation and Words Per Minute WPM;\n" +
                "Language Use\n" +
                "Please follow the following format for each of the section subheadings noted above.\n" +
                "Use this as an example of the format for each subheading:\n" +
                "Subheading Title\n" +
                "2-3 sentences that describe the results of the subsection given the parameters and guidelines noted in the rest of this prompt\n" +
                "High-Level Description: One sentence to provide a high level description of what the metric is telling the user and team\n" +
                "Significance for the Team: One sentence to provide a description of why this metric and this subheading is significant to the user and the team\n" +
                "Importance: One sentence to describe why improving this metric is important to various objective measures of team performance and interactions and kpi’s\n" +
                "Recommendations: 2-3 specific recommendations that the user and team can practice and improve this subheading topic in the next meeting or throughout the next week in the format:\n" +
                "When… (trigger event to practice recommendation), (behavior to address improve or practice recommendation)\n" +
                "Bullet point description of the second recommendation\n" +
                "Bullet point description of the third recommendation\n" +
                "Here is an example:\n" +
                "Global Team Synchrony\n" +
                " The global data indicates a baseline of moderate synchrony across cognitive, emotional, and behavioral dimensions, suggesting an established but improvable level of cooperation and alignment. This sets the stage for targeted interventions aimed at elevating these metrics to higher synchrony levels.\n" +
                "High-Level Description: This metric evaluates the overall alignment and cooperative dynamics within the team.\n" +
                "Significance for the Team: Currently, the team exhibits moderate synchrony, suggesting areas of strength alongside significant improvement opportunities.\n" +
                "Importance: Understanding this metric is crucial as it reflects the team's ability to operate cohesively, which directly impacts productivity and innovation.\n" +
                "Recommendations:\n" +
                "When the team is discussing a topic, explore and discuss differing viewpoints to surface blindspots\n" +
                "When the team is weighing options, allow for divergent thinking and discussion\n" +
                "When team is converging or deciding on alignment, be sure to survey each team member and allow for open and frank discussion before seeking alignment\n" +
                "Make sure the executive summary contains the following elements:\n" +
                "1. Purpose of the Report Clearly state why the report was created. This includes the objectives, the problem it addresses, or the issue it aims to resolve.\n" +
                "2. Key Findings or Conclusions Summarize the most critical findings or conclusions drawn from the report. This should be a high-level overview without diving into too much detail.\n" +
                "3. Actions and Recommendations Highlight any key recommendations or action steps based on the report's findings. These should be practical and directly address the report's conclusions.\n" +
                "4. Significance or Impact Explain the significance of the findings. How do they impact the business, stakeholders, or relevant parties? This helps to emphasize the report’s value and relevance.\n" +
                "5. Summary of Data or Statistics If the report includes important data or statistics, offer a high-level summary of the most relevant data points that support the conclusions.\n" +
                "6. Call to Action What steps should be taken based on the insights in the report?\n" +
                "8. Clarity: written in a clear, straightforward style, avoiding jargon and unnecessary detail.\n" +
                "9. Global vs. Specific Data If the report covers both high-level (global) and specific meeting or project data, as in your case, separate these into distinct sections within the executive summary to offer clarity on both the broader and more targeted insights.\n" +
                "That is, make two sections, one for the global team metrics, and one for the individual meeting metrics. Finally, write a conclusion and general recommendation section at the end.\n" +
                "And, Ensure that each part clearly reflects the distinct sections of the report, offering a comprehensive overview of both the global trends and the detailed analysis of the single meeting.\n" +
                "Make sure all recommendations given are concrete, specific, and actionable.\n" +
                "For all of the recommendations, remove any suggestions with phrasing of \"team-building activities\" and instead be more specific with the output of all the recommendations in the report.\n" +
                "For example: \"When trust issues are evident, organize team-building activities that focus on transparency and reliability.\"\n" +
                "instead becomes: \"When trust issues are evident, focus on transparency and reliability by (suggest and easy-to-do behavior or habit or practice for user and team that can be done in a meeting or during the week that does not require additional planning or organization).\"\n" +
                "Also in each and every recommendation, and especially when using terms like: \"emotional and cognitive support strategies\"; \"physiological and psychological health.\"; \"energizing activities\"; and when using terms that have a higher variability of understanding by business people unfamiliar with organizational development or organizational psychology or emotional intelligence terminology,  please give concise examples or easily actionable behaviors or practices.";
        String key = "test/meeting" + data.getMeetingId() + "/gpt.txt";
        //s3Client.putObject(S3Prefix.VIDEO_BUCKET_NAME, key, data.getContent());
        //TODO:修改数据化标记位
        String message = gptService.sendMessageToGpt(data.getText(), PROMPT);
        GptSummary gptSummary = new GptSummary(data.getMeetingId(), null, "Report", message, 12);
        gptService.save(gptSummary);
        return RestResult.success().data(message);
    }*/

    @PostMapping("/process-report")
    public RestResult processReport(
            @RequestBody ReportDataDTO data) throws IOException {
        Long meetingId = data.getMeetingId();
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        if (meetingTable.getGpt_report_handle() == 1) {
            String key = "test/meeting" + meetingId + "/report.txt";
            S3Object o = s3Client.getObject(S3Prefix.VIDEO_BUCKET_NAME, key);
            InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return RestResult.success().data(content);
        }
        String PROMPT = "Please create an Executive Summary of this report.\n" +
                    " You must include high level observations and thoughts on trends and qualitative assessments of the team and team dynamics, based on the data in the report.\n" +
                    "The executive summary must be divided into two parts:\n" +
                    "Part 1: Global Summary Data: Summarize the high-level observations, trends, and qualitative assessments based on the following subsections of the report.\n" +
                    "The subsections are: Global Team Synchrony Global Team Metrics Global Team Performance Global Team Progress\n" +
                    "Part 2: Most Recent Meeting Data: Provide a summary based on the specific data from the single meeting detailed in the following subsections.\n" +
                    "The subsections are: Meeting Details and Highlights Meeting Team Synchrony Performance Team Sentiment Team Dimensions Team Emotion/Excitement Heatmap Participation Words Per Minute (WPM) Language Use\n" +
                    "As an example, for each subsection please utilize this exact format given these section subheadings:\n" +
                    "Global Summary Data:\n" +
                    "Global Team Synchrony;\n" +
                    "Global Team Metrics;\n" +
                    "Global Team Performance;\n" +
                    "Global Team Progress;\n" +
                    "Most Recent Meeting Data:\n" +
                    "Meeting Detail and Highlights;\n" +
                    "Meeting Team Synchrony Performance;\n" +
                    "Team Sentiment;\n" +
                    "Team Dimensions;\n" +
                    "Team Emotion / Excitement Heatmap;\n" +
                    "Participation and Words Per Minute WPM;\n" +
                    "Language Use\n" +
                    "Please follow the following format for each of the section subheadings noted above.\n" +
                    "Use this as an example of the format for each subheading:\n" +
                    "Subheading Title\n" +
                    "2-3 sentences that describe the results of the subsection given the parameters and guidelines noted in the rest of this prompt\n" +
                    "High-Level Description: One sentence to provide a high level description of what the metric is telling the user and team\n" +
                    "Significance for the Team: One sentence to provide a description of why this metric and this subheading is significant to the user and the team\n" +
                    "Importance: One sentence to describe why improving this metric is important to various objective measures of team performance and interactions and kpi’s\n" +
                    "Recommendations: 2-3 specific recommendations that the user and team can practice and improve this subheading topic in the next meeting or throughout the next week in the format:\n" +
                    "When… (trigger event to practice recommendation), (behavior to address improve or practice recommendation)\n" +
                    "Bullet point description of the second recommendation\n" +
                    "Bullet point description of the third recommendation\n" +
                    "Here is an example:\n" +
                    "Global Team Synchrony\n" +
                    " The global data indicates a baseline of moderate synchrony across cognitive, emotional, and behavioral dimensions, suggesting an established but improvable level of cooperation and alignment. This sets the stage for targeted interventions aimed at elevating these metrics to higher synchrony levels.\n" +
                    "High-Level Description: This metric evaluates the overall alignment and cooperative dynamics within the team.\n" +
                    "Significance for the Team: Currently, the team exhibits moderate synchrony, suggesting areas of strength alongside significant improvement opportunities.\n" +
                    "Importance: Understanding this metric is crucial as it reflects the team's ability to operate cohesively, which directly impacts productivity and innovation.\n" +
                    "Recommendations:\n" +
                    "When the team is discussing a topic, explore and discuss differing viewpoints to surface blindspots\n" +
                    "When the team is weighing options, allow for divergent thinking and discussion\n" +
                    "When team is converging or deciding on alignment, be sure to survey each team member and allow for open and frank discussion before seeking alignment\n" +
                    "Make sure the executive summary contains the following elements:\n" +
                    "1. Purpose of the Report Clearly state why the report was created. This includes the objectives, the problem it addresses, or the issue it aims to resolve.\n" +
                    "2. Key Findings or Conclusions Summarize the most critical findings or conclusions drawn from the report. This should be a high-level overview without diving into too much detail.\n" +
                    "3. Actions and Recommendations Highlight any key recommendations or action steps based on the report's findings. These should be practical and directly address the report's conclusions.\n" +
                    "4. Significance or Impact Explain the significance of the findings. How do they impact the business, stakeholders, or relevant parties? This helps to emphasize the report’s value and relevance.\n" +
                    "5. Summary of Data or Statistics If the report includes important data or statistics, offer a high-level summary of the most relevant data points that support the conclusions.\n" +
                    "6. Call to Action What steps should be taken based on the insights in the report?\n" +
                    "8. Clarity: written in a clear, straightforward style, avoiding jargon and unnecessary detail.\n" +
                    "9. Global vs. Specific Data If the report covers both high-level (global) and specific meeting or project data, as in your case, separate these into distinct sections within the executive summary to offer clarity on both the broader and more targeted insights.\n" +
                    "That is, make two sections, one for the global team metrics, and one for the individual meeting metrics. Finally, write a conclusion and general recommendation section at the end.\n" +
                    "And, Ensure that each part clearly reflects the distinct sections of the report, offering a comprehensive overview of both the global trends and the detailed analysis of the single meeting.\n" +
                    "Make sure all recommendations given are concrete, specific, and actionable.\n" +
                    "For all of the recommendations, remove any suggestions with phrasing of \"team-building activities\" and instead be more specific with the output of all the recommendations in the report.\n" +
                    "For example: \"When trust issues are evident, organize team-building activities that focus on transparency and reliability.\"\n" +
                    "instead becomes: \"When trust issues are evident, focus on transparency and reliability by (suggest and easy-to-do behavior or habit or practice for user and team that can be done in a meeting or during the week that does not require additional planning or organization).\"\n" +
                    "Also in each and every recommendation, and especially when using terms like: \"emotional and cognitive support strategies\"; \"physiological and psychological health.\"; \"energizing activities\"; and when using terms that have a higher variability of understanding by business people unfamiliar with organizational development or organizational psychology or emotional intelligence terminology,  please give concise examples or easily actionable behaviors or practices."
                    +"\nFinally, please provide me with the results in JSON format."
                    +"\nThe JSON format of the output is as follows:"
//                    + "{\n" +
//                    "  “ExecutiveSummary”: {\n" +
//                    "    “PurposeOfTheReport”: “This report was created to analyze team synchrony and performance metrics both on a global scale and within a specific recent meeting context. It aims to identify areas of strength and opportunities for improvement to enhance overall team dynamics and performance.”\n" +
//                    "  },\n" +
//                    "  “GlobalSummaryData”: {\n" +
//                    "    “GlobalTeamSynchrony”: {\n" +
//                    "      “Description”: “The team exhibits medium synchrony, indicating moderate alignment and cooperation across cognitive, emotional, and behavioral dimensions. Although the foundation for teamwork is present, there is significant room for improvement in fostering consistent communication and goal alignment.“,\n" +
//                    "      “HighLevelDescription”: “This metric evaluates team dynamics’ cohesiveness on different dimensions.“,\n" +
//                    "      “SignificanceForTheTeam”: “The team has partial alignment, leading to opportunities for enhancing communication effectiveness and trust.“,\n" +
//                    "      “Importance”: “Improving synchrony is crucial as it impacts productivity, engagement, and objective alignment.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “When engaging in discussions, facilitate open dialogue to bring hidden viewpoints to the surface.“,\n" +
//                    "        “Implement structured formats like round-robin discussions to ensure balanced participation.“,\n" +
//                    "        “Regularly revisit team objectives to ensure continuous alignment and clarity.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “GlobalTeamMetrics”: {\n" +
//                    "      “Description”: “Moderate scores in engagement, alignment, agency, and stress, coupled with low burnout, reflect a moderately participative and empowered team dynamic. However, moderate stress levels indicate a need for better stress management strategies.“,\n" +
//                    "      “HighLevelDescription”: “These metrics measure various aspects influencing team engagement and effectiveness.“,\n" +
//                    "      “SignificanceForTheTeam”: “The metrics point to a stable yet improvable team dynamic where stress and occasional disengagement are notable.“,\n" +
//                    "      “Importance”: “These insights help in tailoring strategies for enhancing team performance and well-being.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Clarify and reiterate team objectives regularly to ensure alignment.“,\n" +
//                    "        “Encourage feedback loops to address participation disparities.“,\n" +
//                    "        “Support members in stress management through practical strategies like workload adjustment.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “GlobalTeamPerformance”: {\n" +
//                    "      “Description”: “Scores in cognitive, emotional, and behavioral alignment reflect variability, with emotional alignment being relatively higher.“,\n" +
//                    "      “HighLevelDescription”: “Performance metrics relate to alignment and functioning in mental, emotional, and behavioral domains.“,\n" +
//                    "      “SignificanceForTheTeam”: “A focus on cognitive engagement can balance the variably aligned domains.“,\n" +
//                    "      “Importance”: “Enhancing these dimensions directly correlates with increased cohesion and collective efficacy.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Facilitate opportunities for collaborative learning to boost mental and cognitive alignment.“,\n" +
//                    "        “Address emotional engagement by fostering open emotional communication.“,\n" +
//                    "        “Solicit regular feedback on non-verbal communication effectiveness.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “GlobalTeamProgress”: {\n" +
//                    "      “Description”: “Team synchrony variability is moderate, indicating a potential for more adaptive communication and group dynamics if managed properly.“,\n" +
//                    "      “HighLevelDescription”: “This reflects how alignment fluctuations allow for diversity and innovation while preventing groupthink.“,\n" +
//                    "      “SignificanceForTheTeam”: “Recognizing synchrony patterns helps in balancing collaboration and independent thought.“,\n" +
//                    "      “Importance”: “An optimal synchrony level fosters dynamic teamwork capable of adapting to various challenges.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Maintain a balance between high and low synchronization for adaptability.“,\n" +
//                    "        “Encourage feedback that aids in synchrony level adjustment.“,\n" +
//                    "        “Celebrate successes to reinforce team morale and cohesion.”\n" +
//                    "      ]\n" +
//                    "    }\n" +
//                    "  },\n" +
//                    "  “MostRecentMeetingData”: {\n" +
//                    "    “MeetingDetailsAndHighlights”: {\n" +
//                    "      “Description”: “The meeting emphasized organizational changes, technical discussions, and team dynamics with critical updates and proposals.“,\n" +
//                    "      “HighLevelDescription”: “Key focus on updates and future team structuring.“,\n" +
//                    "      “SignificanceForTheTeam”: “Provides insights into pivotal organizational transitions and their effect on team cohesion.“,\n" +
//                    "      “Importance”: “Understanding these changes is vital for strategic alignment and minimizing uncertainty.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Facilitate clear communication of changes and their impact.“,\n" +
//                    "        “Invite feedback on organizational changes to assist with transitions.“,\n" +
//                    "        “Recognize and celebrate adaptive responses to these organizational updates.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “MeetingTeamSynchronyPerformance”: {\n" +
//                    "      “Description”: “The synchrony indicates moderate collaborative efficiency during the meeting across mental, emotional, and behavioral facets.“,\n" +
//                    "      “HighLevelDescription”: “Measures team harmony during interactions focusing on mental, emotional, and physical engagement.“,\n" +
//                    "      “SignificanceForTheTeam”: “Moderate performance suggests potential to raise collective interaction efficiency.“,\n" +
//                    "      “Importance”: “Enhanced synchrony improves meeting productivity and satisfaction.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “When presenting information, ensure clarity to enhance cognitive engagement.“,\n" +
//                    "        “Encourage stress management tactics during meetings to sustain emotional alignment.“,\n" +
//                    "        “Facilitate feedback on verbal and non-verbal communication for ongoing improvement.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “TeamSentiment”: {\n" +
//                    "      “Description”: “A largely neutral sentiment underscores the need for enhancing the emotional engagement and capturing a more positive collective mood.“,\n" +
//                    "      “HighLevelDescription”: “Measures team morale and emotional tone during the meeting.“,\n" +
//                    "      “SignificanceForTheTeam”: “Neutral sentiment flags potential for fostering more engaging, positive meetings.“,\n" +
//                    "      “Importance”: “Improving sentiment can elevate overall team morale and performance output.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Implement recognition programs to enhance positive sentiment.“,\n" +
//                    "        “Use ice-breakers or mood-setting activities at the meeting start.“,\n" +
//                    "        “Continually assess sentiment to catch emerging issues early.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “TeamDimensions”: {\n" +
//                    "      “Description”: “High trust and task engagement stand out, but there’s a notable gap in psychological safety and participation equality.“,\n" +
//                    "      “HighLevelDescription”: “Shows key performance indicators within the team dynamic landscape.“,\n" +
//                    "      “SignificanceForTheTeam”: “Trust and task engagement are strengths, yet psychological safety requires enhancement.“,\n" +
//                    "      “Importance”: “Encouraging psychological safety and balanced participation fosters inclusivity and innovation.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Identify team feedback channels to enhance psychological safety.“,\n" +
//                    "        “Promote role rotation for diverse participation insights.“,\n" +
//                    "        “Highlight team achievements to reinforce trust and engagement.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “MeetingTeamEmotionExcitementHeatmap”: {\n" +
//                    "      “Description”: “Graphical representation shows fluctuations in excitement and emotion, indicating active engagement yet an opportunity for more calmness.“,\n" +
//                    "      “HighLevelDescription”: “Assesses emotional engagement and excitement levels during meeting phases.“,\n" +
//                    "      “SignificanceForTheTeam”: “High energy and engagement suggest vibrancy, but excessive variance may need calming strategies.“,\n" +
//                    "      “Importance”: “Balanced emotional states enhance productivity and teamwork harmony.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Encourage stress management tactics to mitigate negative excitement.“,\n" +
//                    "        “Hold regular roundtables to ensure a calm, productive atmosphere.“,\n" +
//                    "        “Utilize positive reinforcements to maintain high engagement.”\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    “MeetingParticipationAndWPM”: {\n" +
//                    "      “Description”: “There are both fast and varied speaking rates, relating to engagement and understanding disparities.“,\n" +
//                    "      “HighLevelDescription”: “Quantifies team contribution speed and engagement in meeting communication.“,\n" +
//                    "      “SignificanceForTheTeam”: “Speaking disparities may lead to communication friction or misunderstandings.“,\n" +
//                    "      “Importance”: “Balanced speaking rates facilitate clarity and mutual comprehensibility in meetings.“,\n" +
//                    "      “Recommendations”: [\n" +
//                    "        “Encourage a moderated speaking pace to boost comprehension.“,\n" +
//                    "        “Use structured turns to equalize speaking opportunities.“,\n" +
//                    "        “Provide training on communication dynamics to enhance team interactions.”\n" +
//                    "      ]\n" +
//                    "    }\n" +
//                    "  },\n" +
//                    "  “ConclusionAndGeneralRecommendations”: {\n" +
//                    "    “Description”: “To align the team’s potential with consistent performance, individual and collective efforts must underscore consistent communication, balanced engagement, and stress management. Emphasizing psychological safety and incremental improvements in alignment are crucial for stronger synchrony and overall enhanced team dynamics. Regular monitoring, feedback, and adaptable strategies will drive these improvements forward, ultimately enriching team cohesion and operational success.”\n" +
//                    "  }\n" +
//                    "}"
                    + "\n{\n" +
                    "  “Executive Summary”: {\n" +
                    "      “Purpose of the Report”: “”,\n" +
                    "      “Global Summary Data”: {\n" +
                    "        “Global Team Synchrony”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Global Team Metrics”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "            “”,\n" +
                    "            “”,\n" +
                    "            “”\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Global Team Performance”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Global Team Progress”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "            “”,\n" +
                    "            “”,\n" +
                    "            “”\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      },\n" +
                    "      “Most Recent Meeting Data”: {\n" +
                    "        “Meeting Detail and Highlights”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Meeting Team Synchrony Performance”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Team Sentiment”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Team Dimensions”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Team Emotion / Excitement Heatmap”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Participation and Words Per Minute WPM”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        “Language Use”: {\n" +
                    "          “Description”: “”,\n" +
                    "          “High-Level Description”: “”,\n" +
                    "          “Significance for the Team”: “”,\n" +
                    "          “Importance”: “”,\n" +
                    "          “Recommendations”: [\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      },\n" +
                    "      “Conclusion”: {\n" +
                    "        “General Recommendations”: [\n" +
                    "        ],\n" +
                    "        “Call to Action”: “”\n" +
                    "      },\n" +
                    "      “Significance or Impact”: “”\n" +
                    "    }\n" +
                    "  }"
                    ;

            String message = gptService.sendMessageToGpt4(data.getText(), PROMPT);
            message = message.replaceAll("\\*", "")
                .replaceAll("#", "")
                .replaceAll("```", "")
                .replaceAll("json", "");
            String key = "test/meeting" + meetingId + "/report.txt";
            s3Client.putObject(S3Prefix.VIDEO_BUCKET_NAME, key, message);
            LambdaUpdateWrapper<MeetingTable> updateWrapper = new LambdaUpdateWrapper<MeetingTable>()
                .eq(MeetingTable::getMeeting_id, meetingId)
                        .set(MeetingTable::getGpt_report_handle, 1);
            meetingService.update(null, updateWrapper);
            return RestResult.success().data(message);

    }
    @GetMapping("/report/{meetingId}")
    public RestResult queryReportData(@PathVariable("meetingId") Long meetingId) throws IOException {
//        LambdaQueryWrapper<GptSummary> queryWrapper = new LambdaQueryWrapper<GptSummary>()
//                .eq(GptSummary::getMeetingId, meetingId)
//                .eq(GptSummary::getLabel, 12);
//        GptSummary one = gptService.getOne(queryWrapper);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingId);
        HashMap<String, String> map = new HashMap<>();
        map.put("handle", "0");
        map.put("data", "");
        if (meetingTable != null && meetingTable.getGpt_report_handle() == 1) {
            map.put("handle", "1");
            String key = "test/meeting" + meetingId + "/report.txt";
            S3Object o = s3Client.getObject(S3Prefix.VIDEO_BUCKET_NAME, key);
            InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            map.put("data", content.toString());
        }
        return RestResult.success().data(map);
    }
}
