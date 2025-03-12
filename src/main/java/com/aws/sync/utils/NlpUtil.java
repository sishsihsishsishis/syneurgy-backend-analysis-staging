package com.aws.sync.utils;

import com.aws.sync.entity.NlpTable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class NlpUtil {

    public static List<NlpTable> read_nlp(Long meetingID, List<String[]> data) throws IOException {
        List<NlpTable> res = new ArrayList<>();
        for(int i = 0; i <data.size(); i++){
            NlpTable nlpTable =new NlpTable();
            nlpTable.setMeeting_id(meetingID);
            nlpTable.setSpeaker(data.get(i)[0]);
            nlpTable.setStarts(Double.parseDouble(data.get(i)[1]));
            nlpTable.setEnds(Double.parseDouble(data.get(i)[2]));
            nlpTable.setSentence(data.get(i)[3]);
            nlpTable.setEmotion(data.get(i)[4]);
            nlpTable.setDialogue(data.get(i)[5]);
            res.add(nlpTable);
        }
        return res;
    }

    public static void get_pie_and_bar(List<String[]> data,
                                       List<String> s_keys, List<Double> s_time, List<Double> s_rate,
                                       List<String> e_keys, List<Double> e_time, List<Double> e_rate,
                                       List<String> a_keys, List<Double> a_time, List<Double> a_rate,
                                       List<List<Double>> bar_speakers, List<List<Double>> bar_emotions,
                                       List<Double> total, List<String> sentences_array, List<String> users) throws IOException {

        log.info("[NlpUtil][get_pie_and_bar][start] data:{}\n" +
                        "speaker_keys:{}\n" +
                        "speaker_time:{}\n" +
                        "speaker_rate:{}\n" +
                        "emotions_keys:{}\n" +
                        "emotions_time:{}\n" +
                        "emotions_rate:{}\n" +
                        "acts_keys:{}\n" +
                        "acts_time:{}\n" +
                        "acts_rate:{}\n" +
                        "bar_speakers:{}\n" +
                        "bar_emotions:{}\n" +
                        "total:{}\n" +
                        "sentences_array:{}\n" +
                        "users:{}\n"
                ,data, s_keys, s_time, s_rate, e_keys, e_time, e_rate, a_keys,
                a_time, a_rate, bar_speakers, bar_emotions, total, sentences_array, users);
        Integer size = users.size();
        double[] speakers_time = new double[size];
        for (int i = 0; i < size; i++) {
            speakers_time[i] = 0.0;
        }

        double[][] speakers_time_sep_by_emotions = new double[size][3];
        for(int i = 0; i < size; i++){
            speakers_time_sep_by_emotions[i][0] = 0.0;
            speakers_time_sep_by_emotions[i][1] = 0.0;
            speakers_time_sep_by_emotions[i][2] = 0.0;
        }

        double[][] emotions_time_sep_by_speakers = new double[3][size];
        for (int i = 0; i < size; i++) {
            emotions_time_sep_by_speakers[0][i] = 0.0;
            emotions_time_sep_by_speakers[1][i] = 0.0;
            emotions_time_sep_by_speakers[2][i] = 0.0;
        }

        double[] emotions_time = {0.0,0.0,0.0};
        double[] acts_time = {0.0,0.0,0.0,0.0,0.0,0.0,0.0};

        Map<String,Integer> speakers_ind = new LinkedHashMap<>();
        for (int i = 0; i < users.size(); i++) {
            speakers_ind.put(users.get(i), Integer.valueOf(i));
        }

        Map<String, Integer> emotions_ind = new LinkedHashMap<>();
        emotions_ind.put("negative", 0);
        emotions_ind.put("neutral", 1);
        emotions_ind.put("positive", 2);
        Map<String, Integer> acts_ind = new LinkedHashMap<>();
        acts_ind.put("Statement-non-opinion", 0);
        acts_ind.put("Statement-opinion", 1);
        acts_ind.put("Collaborative Completion", 2);
        acts_ind.put("Abandoned or Turn-Exit", 3);
        acts_ind.put("Uninterpretable", 4);
        acts_ind.put("Yes-No-Question", 5);
        acts_ind.put("Others", 6);

        for (int i = 0; i < data.size(); i++) {
            String speaker = data.get(i)[0];
            String emotion = data.get(i)[4];
            String act = data.get(i)[5];
            String start_time = data.get(i)[1];
            String end_time = data.get(i)[2];
            String sentence = data.get(i)[3];
            boolean isExist = false;
            Set<String> keySet = acts_ind.keySet();
            for (String key:keySet){
                if (act.equals(key)){
                    isExist = true;
                    break;
                }
            }
            if (!isExist) act = "Others";
            double delta_time = Double.parseDouble(end_time) - Double.parseDouble(start_time);

            double speakers_time_pre = speakers_time[speakers_ind.get(speaker)];
            speakers_time[speakers_ind.get(speaker)] = speakers_time_pre + delta_time;

            double emotions_time_pre = emotions_time[emotions_ind.get(emotion)];
            emotions_time[emotions_ind.get(emotion)] = emotions_time_pre + delta_time;

            double acts_time_pre = acts_time[acts_ind.get(act)];
            acts_time[acts_ind.get(act)] = acts_time_pre + delta_time;

            double speakers_time_sep_by_emotions_pre = speakers_time_sep_by_emotions[speakers_ind.get(speaker)][emotions_ind.get(emotion)];
            speakers_time_sep_by_emotions[speakers_ind.get(speaker)][emotions_ind.get(emotion)] = speakers_time_sep_by_emotions_pre + delta_time;

            double emotions_time_sep_by_speakers_pre = emotions_time_sep_by_speakers[emotions_ind.get(emotion)][speakers_ind.get(speaker)];
            emotions_time_sep_by_speakers[emotions_ind.get(emotion)][speakers_ind.get(speaker)] = emotions_time_sep_by_speakers_pre + delta_time;

            sentences_array.add(sentence);
        }
        Double total_time = 0d;
        for (Double t : speakers_time) {
            total_time += t;
        }
        total.add(total_time);
        speakers_ind.forEach((key,value)->{
            s_keys.add(key);
        });

        for(int i = 0; i < speakers_time.length; i++){
            BigDecimal t = new BigDecimal(speakers_time[i]);
            BigDecimal r = new BigDecimal(speakers_time[i]/total_time);
            s_time.add(t.setScale(4, RoundingMode.HALF_UP).doubleValue());
            s_rate.add((r.setScale(4,RoundingMode.HALF_UP).doubleValue()));
        }

        emotions_ind.forEach((key,value)->{
            e_keys.add(key);
        });

        for(int i = 0; i < emotions_time.length; i++){
            BigDecimal t = new BigDecimal(emotions_time[i]);
            BigDecimal r = new BigDecimal(emotions_time[i]/total_time);
            e_time.add(t.setScale(3,RoundingMode.HALF_UP).doubleValue());
            e_rate.add((r.setScale(3,RoundingMode.HALF_UP).doubleValue()));
        }

        acts_ind.forEach((key,value)->{
            a_keys.add(key);
        });

        for(int i = 0; i < acts_time.length; i++){
            BigDecimal t = new BigDecimal(acts_time[i]);
            BigDecimal r = new BigDecimal(acts_time[i]/total_time);
            a_time.add(t.setScale(3,RoundingMode.HALF_UP).doubleValue());
            a_rate.add((r.setScale(3,RoundingMode.HALF_UP).doubleValue()));
        }

        for(int i = 0 ; i < speakers_time_sep_by_emotions.length; i++) {
            List<Double> t = new ArrayList<>();
            for (int j = 0; j < speakers_time_sep_by_emotions[0].length; j++) {
                BigDecimal b = new BigDecimal(speakers_time_sep_by_emotions[i][j]);
                t.add(b.setScale(3,RoundingMode.HALF_UP).doubleValue());
            }
            bar_speakers.add(t);
        }

        for(int i = 0 ; i < emotions_time_sep_by_speakers.length; i++) {
            List<Double> t = new ArrayList<>();
            for (int j = 0; j < emotions_time_sep_by_speakers[0].length; j++) {
                BigDecimal b = new BigDecimal(emotions_time_sep_by_speakers[i][j]);
                t.add(b.setScale(3,RoundingMode.HALF_UP).doubleValue());
            }
            bar_emotions.add(t);
        }
        log.info("[NlpUtil][get_pie_and_bar][end] data:{}\n" +
                        "speaker_keys:{}\n" +
                        "speaker_time:{}\n" +
                        "speaker_rate:{}\n" +
                        "emotions_keys:{}\n" +
                        "emotions_time:{}\n" +
                        "emotions_rate:{}\n" +
                        "acts_keys:{}\n" +
                        "acts_time:{}\n" +
                        "acts_rate:{}\n" +
                        "bar_speakers:{}\n" +
                        "bar_emotions:{}\n" +
                        "total:{}\n" +
                        "sentences_array:{}\n" +
                        "users:{}\n"
                ,data, s_keys, s_time, s_rate, e_keys, e_time, e_rate, a_keys,
                a_time, a_rate, bar_speakers, bar_emotions, total, sentences_array, users);
    }

    public static void get_radar_components(List<Double> speakers_time, Double total_time, List<Double> acts_time,
                                            List<Double> emotions_time, List<String> sentences_array,
                                            List<Double> radar_chart_list, List<String> r_keys, List<String> users) {
        log.info("[NlpUtil][get_radar_components][start] speakers_time:{}\n" +
                "total_time:{}\n" +
                "acts_time:{}\n" +
                        "emotions_time:{}\n" +
                        "sentences_array:{}\n" +
                        "radar_chart_list:{}\n" +
                        "r_keys:{}\n" +
                        "users:{}\n"
        ,speakers_time, total_time, acts_time, emotions_time, sentences_array, radar_chart_list, r_keys, users);
        Map<String,Integer> radar_chart_ind = new LinkedHashMap<>();
        radar_chart_ind.put("Equal Participation",0);
        radar_chart_ind.put("Enjoyment",1);
        radar_chart_ind.put("Shared Goal Commitment",2);
        radar_chart_ind.put("Absorption or Task Engagement",3);
        radar_chart_ind.put("Trust and Psychological Safety",4);
        Double radar_chart_array[] = {0.0, 0.0, 0.0, 0.0, 0.0};
//         --- 0. Equal Participation ---
        Map<String,Integer> acts_ind = new LinkedHashMap<>();
        acts_ind.put("Statement-non-opinion",0);
        acts_ind.put("Statement-opinion",1);
        acts_ind.put("Collaborative Completion",2);
        acts_ind.put("Abandoned or Turn-Exit",3);
        acts_ind.put("Uninterpretable",4);
        acts_ind.put("Yes-No-Question",5);
        acts_ind.put("Others",6);
        int num_users = users.size();
        Double max_entropy = -Math.log(1.0 / num_users)/Math.log(2);
        List<Double> speakers_time_rate = new ArrayList<>();
        for(int i = 0; i < speakers_time.size(); i++){
            speakers_time_rate.add(speakers_time.get(i) / total_time);
        }
        Double entropy = 0d;
        for(int i = 0; i < speakers_time_rate.size(); i++){
            entropy -= (speakers_time_rate.get(i) * (Math.log(speakers_time_rate.get(i))/Math.log(2)));
        }
        Double equal_participation = entropy / max_entropy;
        if (Double.isNaN(equal_participation) || Double.isInfinite(equal_participation)) {
            equal_participation = 0.1;
        }
        if (equal_participation > 0.618) {
            equal_participation = 0.5 + 1 / (1 + Math.pow(Math.E, -15 * (equal_participation - 1)));
        }
        radar_chart_array[0] = equal_participation;

//        --- 4. Trust and Psychological Safety ---
        Double opinion_time = acts_time.get(acts_ind.get("Statement-opinion"));
        Double non_opinion_time = acts_time.get(acts_ind.get("Statement-non-opinion"));
        Double op_rate = opinion_time / (opinion_time + non_opinion_time);
        List<Double> t = new ArrayList<>();
        t.add(equal_participation);
        t.add(op_rate);
        Double trust_psychological_safety = CsvUtil.softmax_weights_output(t);
        if (Double.isNaN(trust_psychological_safety) || Double.isInfinite(trust_psychological_safety)) {
            trust_psychological_safety = 0.1;
        }
        radar_chart_array[4] = trust_psychological_safety;

//      # --- 1. Enjoyment ---
//      # -- nlp emotion
        Map<String,Integer> emotions_ind = new LinkedHashMap<>();
        emotions_ind.put("Negative",0);
        emotions_ind.put("Neutral",1);
        emotions_ind.put("Positive",2);
        Double positive_time = emotions_time.get(emotions_ind.get("Positive"));
        Double negative_time = emotions_time.get(emotions_ind.get("Negative"));
        Double nlp_enjoyment = positive_time / (positive_time + negative_time);
        if (Double.isNaN(nlp_enjoyment)) {
            //Default 0.1
            nlp_enjoyment = 0.1;
        }
        if (nlp_enjoyment > 0.95) {
            nlp_enjoyment = 0.95;
        }
        if (nlp_enjoyment > 0.618) {
            nlp_enjoyment = 0.5 + 1 / (1 + Math.pow(Math.E, -15 * (nlp_enjoyment - 1)));
        }
        if (nlp_enjoyment < 0.1) nlp_enjoyment = 0.1;

//        # -- va emotion
//        List<Double> data_v_mean = new ArrayList<>();
//        int num_v_negative = 0;
//        for (int i = 1; i < data_v.size(); i++) {  // 从第一行开始，跳过标题行
//            data_v_mean.add("".equals(data_v.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(data_v.get(i)[1]));
//        }
//        for(Double d:data_v_mean){
//            if(d < 0) num_v_negative ++;
//        }
//        int num_v_positive = data_v_mean.size() - num_v_negative;
//        Double v_enjoyment = num_v_positive * 1.0 / data_v_mean.size();
//        List<Double> e = new ArrayList<>();
//        e.add(nlp_enjoyment);
//        e.add(v_enjoyment);
//        double enjoyment = CsvUtil.softmax_weights_output(e);
//        radar_chart_array[1] = enjoyment;

        radar_chart_array[1] = nlp_enjoyment;
//        --- 2. Shared Goal Commitment ---
        int i_we_num[] = {0,0};
        for(String s : sentences_array){
            String[] sentence = s.split("\\s+");
            for (String word : sentence) {
                // word.startsWith("i'") || word.startsWith("I'")
                if ("i".equals(word) || "I".equals(word) || word.startsWith("i'") || word.startsWith("I'")) {
                    i_we_num[0]++;
                } else if ("we".equals(word) || "We".equals(word) || word.startsWith("we'") || word.startsWith("We'")){
                    i_we_num[1] ++;
                }
            }
        }
        Double shared_goal_commitment = i_we_num[1] * 1.0 / (i_we_num[0] + i_we_num[1]);
        shared_goal_commitment = shared_goal_commitment * 1.5 > 8.5 ? 8.5 : shared_goal_commitment * 1.5;
        radar_chart_array[2] = shared_goal_commitment;

//        --- 3. Absorption or Task Engagement ---
        Double act_time_abandoned_rate = acts_time.get(acts_ind.get("Abandoned or Turn-Exit")) / total_time;
        Double act_time_not_abandoned_rate = 1.0 - act_time_abandoned_rate;
        Double pn_rate = (positive_time + negative_time) / total_time;
        List<Double> ap = new ArrayList<>();
        ap.add(act_time_not_abandoned_rate);
        ap.add(pn_rate);
        Double absorption_or_task_engagement = CsvUtil.softmax_weights_output(ap);
        if (Double.isNaN(absorption_or_task_engagement) || Double.isInfinite(absorption_or_task_engagement)) {
            absorption_or_task_engagement = 0.1;
        }
        radar_chart_array[3] = absorption_or_task_engagement;

        for(Double d : radar_chart_array){
            if (d != null && !Double.isInfinite(d) && !Double.isNaN(d)) {
                BigDecimal b = new BigDecimal(d);
                radar_chart_list.add(b.setScale(3,RoundingMode.HALF_UP).doubleValue());
            } else {
                //默认0.1
                radar_chart_list.add(0.1);
            }
        }

        radar_chart_ind.forEach((key, value)->{
            r_keys.add(key);
        });
        log.info("[NlpUtil][get_radar_components][end] speakers_time:{}\n" +
                        "total_time:{}\n" +
                        "acts_time:{}\n" +
                        "emotions_time:{}\n" +
                        "sentences_array:{}\n" +
                        "radar_chart_list:{}\n" +
                        "r_keys:{}\n" +
                        "users:{}\n"
                ,speakers_time, total_time, acts_time, emotions_time, sentences_array, radar_chart_list, r_keys, users);
    }

    public static Double get_time(List<String[]> data, List<Double> s_time, List<String> users) {
        Integer size = users.size();
        double[] speakers_time = new double[size];
        for (int i = 0; i < size; i++) {
            speakers_time[i] = 0.0;
        }
        Map<String,Integer> speakers_ind = new LinkedHashMap<>();
        for (int i = 0; i < users.size(); i++) {
            speakers_ind.put(users.get(i), Integer.valueOf(i));
        }

        Double total_time = 0d;
        for (int i = 0; i < data.size(); i++) {
            String speaker = data.get(i)[0];
            String start_time = data.get(i)[1];
            String end_time = data.get(i)[2];
            double delta_time = Double.parseDouble(end_time) - Double.parseDouble(start_time);
            total_time += delta_time;
            double speakers_time_pre = speakers_time[speakers_ind.get(speaker)];
            speakers_time[speakers_ind.get(speaker)] = speakers_time_pre + delta_time;
        }
        for(int i = 0; i < speakers_time.length; i++){
            BigDecimal t = new BigDecimal(speakers_time[i]);
            s_time.add(t.setScale(4, RoundingMode.HALF_UP).doubleValue());
        }
        return total_time;
    }
    public static Double get_equal_participation (List<Double> speakers_time,Double total_time,List<String> users) {
        // 1.Equal Participation
        int num_users = users.size();
        Double max_entropy = -Math.log(1.0 / num_users) / Math.log(2);
        List<Double> speakers_time_rate = new ArrayList<>();
        for(int i = 0; i < speakers_time.size(); i++){
            speakers_time_rate.add(speakers_time.get(i) / total_time);
        }
        Double entropy = 0d;
        for(int i = 0; i < speakers_time_rate.size(); i++){
            entropy -= (speakers_time_rate.get(i) * (Math.log(speakers_time_rate.get(i)) / Math.log(2)));
        }
        Double equal_participation = entropy / max_entropy;
        if (equal_participation == null || Double.isInfinite(equal_participation) || Double.isNaN(equal_participation)) {
            equal_participation = 0.5;
        }
        return equal_participation;
    }

    public static Map<String, Map<Integer, Integer>> wordCount(List<NlpTable> nlpdata) {
        // 创建一个 Map 来保存每个Speaker在每个时间段的单词数量
        Map<String, Map<Integer, Integer>> wordCount = new HashMap<>();

        for (NlpTable nlpTable : nlpdata) {
            double startTime = nlpTable.getStarts();
            double endTime = nlpTable.getEnds();
            // 计算平均时间，并将其转化为对应的30秒时间段
            int timeSlot = ((int)((startTime + endTime) / 2)) / 30 * 30 * 1000;
            String speaker = nlpTable.getSpeaker();
            String sentence = nlpTable.getSentence();
            // 分割句子成为单词
            String[] words = sentence.split(" ");
            // 在 Map 中添加或更新发言者的单词数量
            Map<Integer, Integer> speakerWordCount = wordCount.getOrDefault(speaker, new HashMap<>());
            speakerWordCount.put(timeSlot, speakerWordCount.getOrDefault(timeSlot, 0) + words.length);
            wordCount.put(speaker, speakerWordCount);
        }

        for (Map.Entry<String, Map<Integer, Integer>> speakerEntry : wordCount.entrySet()) {
            String speaker = speakerEntry.getKey();
            Map<Integer, Integer> timeSlotMap = speakerEntry.getValue();

            System.out.println("Speaker: " + speaker);
            for (Map.Entry<Integer, Integer> timeSlotEntry : timeSlotMap.entrySet()) {
                Integer timeSlot = timeSlotEntry.getKey();
                Integer wordCountInSlot = timeSlotEntry.getValue();
                System.out.println("    Time Slot: " + timeSlot + " ms, Word Count: " + wordCountInSlot);
            }
        }
        return wordCount;
    }

}
