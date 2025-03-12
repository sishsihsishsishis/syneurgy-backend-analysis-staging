//package com.aws.sync;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.aws.sync.entity.*;
//import com.aws.sync.mapper.AResultMapper;
//import com.aws.sync.mapper.ASyncMapper;
//import com.aws.sync.mapper.VResultMapper;
//import com.aws.sync.service.*;
//import com.aws.sync.utils.CsvUtil;
//import com.aws.sync.utils.NlpUtil;
//
//import com.aws.sync.vo.EmailSendVO;
//import io.github.flashvayne.chatgpt.service.ChatgptService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//import reactor.core.publisher.Mono;
//
//import javax.annotation.Resource;
//import javax.annotation.WillClose;
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//import static com.aws.sync.utils.CsvUtil.norm_min_max;
//
//@SpringBootTest
//class SyncGaApplicationTests {
//    @Resource
//    AResultMapper aResultMapper;
//
//    @Resource
//    VResultService vResultService;
//
//    @Resource
//    AResultService aResultService;
//
//    @Resource
//    RResultService rResultService;
//
//    @Resource
//    ASyncService aSyncService;
//
//    @Resource
//    VSyncService vSyncService;
//
//    @Resource
//    RSyncService rSyncService;
//
//
//    @Resource
//    AmazonUploadService amazonUploadService;
//
//    @Resource
//    VResultMapper vResultMapper;
//
//    @Resource
//    ASyncMapper aSyncMapper;
//
//    @Resource
//    NlpService nlpService;
//
//    @Autowired
//    IndividualAService individualAService;
//
//    @Autowired
//    IndividualVService individualVService;
//
//    @Autowired
//    IndividualRService individualRService;
//
//    @Autowired
//    AveSyncService aveSyncService;
//
//    @Autowired
//    private ChatgptService chatgptService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
////    @Test
////    void testChatGpt(String[] args) {
////            //国内访问需要做代理，国外服务器不需要
////            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
////            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
////            //！！！！千万别再生产或者测试环境打开BODY级别日志！！！！
////            //！！！生产或者测试环境建议设置为这三种级别：NONE,BASIC,HEADERS,！！！
////            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
////            OkHttpClient okHttpClient = new OkHttpClient
////                    .Builder()
////                    .proxy(proxy)//自定义代理
////                    .addInterceptor(httpLoggingInterceptor)//自定义日志
////                    .connectTimeout(30, TimeUnit.SECONDS)//自定义超时时间
////                    .writeTimeout(30, TimeUnit.SECONDS)//自定义超时时间
////                    .readTimeout(30, TimeUnit.SECONDS)//自定义超时时间
////                    .build();
////            OpenAiStreamClient client = OpenAiStreamClient.builder()
////                    .apiKey(Arrays.asList("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"))
////                    //自定义key的获取策略：默认KeyRandomStrategy
////                    //.keyStrategy(new KeyRandomStrategy())
//////                    .keyStrategy(new FirstKeyStrategy())
////                    .okHttpClient(okHttpClient)
////                    //自己做了代理就传代理地址，没有可不不传
//////                .apiHost("https://自己代理的服务器地址/")
////                    .build();
////
////    }
//    @Test
////    public void callApi() {
////        String queryUrl = "http://47.102.118.168:8080/meeting/emailsend";
////        String postUrl = "http://47.102.118.168:8080/meeting/emailsend/";
////
////        //Send a request and receive a response
////        ResponseEntity<SendEmailResponse> response = restTemplate.exchange(queryUrl, HttpMethod.GET,null,SendEmailResponse.class);
////        SendEmailResponse restResult = response.getBody();
////        if(restResult == null || !restResult.getSuccess() || restResult.getData()== null || restResult.getData().isEmpty()){
////            //No data processing
////            return ;
////        }
////
////        List<EmailSendVO> data = response.getBody().getData();
////
////        for (EmailSendVO e : data) {
////            //TODO:  send Email
////            // if success
////            restTemplate.exchange(postUrl + e.getMeeting_id(),HttpMethod.POST,null,String.class);
////        }
////
////    }
//
////    @Test
//    void TestRedisPut(){
//        redisTemplate.opsForZSet().add("meeting:video:63","test/video/meeting1.mp4",63);
//    }
//
//
//
////    @Test
//    void test1(){
//        String responseMessage = chatgptService.sendMessage("how are you");
//        System.out.print(responseMessage);
//    }
////    @Test
////    void contextLoads() throws IOException {
//////        List<AMean> mean = aResultMapper.findMean(1L);
//////        List<AUser> mean1 = aResultMapper.findUser(1L);
//////        List<Map<Long, Double>> aMean = aResultMapper.findAMean(1L);
//////        System.out.println(aMean.toString());
//////        System.out.println(aSyncMapper.findSync(1L));
//////        amazonUploadService.readCSV("a_results.csv","1");
////        List<String[]> data = amazonUploadService.readNlp("nlp_results.txt", "1");
////        List<String> speakers_keys = new ArrayList<>();
////        List<Double> speakers_time = new ArrayList<>();
////        List<Double> speakers_rate = new ArrayList<>();
////
////        List<String> emotions_keys = new ArrayList<>();
////        List<Double> emotions_time = new ArrayList<>();
////        List<Double> emotions_rate = new ArrayList<>();
////
////        List<String> acts_keys = new ArrayList<>();
////        List<Double> acts_time = new ArrayList<>();
////        List<Double> acts_rate = new ArrayList<>();
////
////        List<List<Double>> bar_speakers =new ArrayList<>();
////        List<List<Double>> bar_emotions =new ArrayList<>();
////
////        List<Double> total_time = new ArrayList<>();
////        List<String> sentences_array = new ArrayList<String>();
////        List<String> users = Arrays.asList("user00","user01","user02","user03","user04","user05","user06"
////                ,"user07","user08","user09");
////        NlpUtil.get_pie_and_bar(data,speakers_keys,speakers_time,speakers_rate,
////                                emotions_keys,emotions_time,emotions_rate,
////                                acts_keys,acts_time,acts_rate,bar_speakers,bar_emotions,
////                                total_time,sentences_array,users);
////        System.out.println("speakers_key: " + speakers_keys.toString());
////        System.out.println("speakers_time: " + speakers_time.toString());
////        System.out.println("speakers_rate: " + speakers_rate.toString());
////
////        System.out.println("emotions_key: " + emotions_keys.toString());
////        System.out.println("emotions_time: " + emotions_time.toString());
////        System.out.println("emotions_rate: " + emotions_rate.toString());
////
////        System.out.println("acts_key: " + acts_keys.toString());
////        System.out.println("acts_time: " + acts_time.toString());
////        System.out.println("acts_rate: " + acts_rate.toString());
////
////    }
//
////    @Test
//    void test2(){
////        List<AMean> mean = aResultMapper.findMean(1L);
//////        for(AMean a : mean){
//////            System.out.println(a.toString());
//////        }
//////        File file = new File("syne1/a_sync.csv");
//////        File file1 = new File("syne1/rppg_sync.csv");
//////        File file2 = new File("syne1/v_sync.csv");
//////        File file3 = new File("syne1");
//////        file.delete();
//////        file1.delete();
//////        file2.delete();
//////        file3.delete();
////        String meeting = "14";
////        File a = new File("syne"+ meeting + "/" + "a_sync.csv");
////        File v = new File("syne"+ meeting + "/" + "v_sync.csv");
////        File rppg = new File("syne"+ meeting + "/" + "rppg_sync.csv");
////        File d = new File("syne"+meeting);
////        a.delete();
////        v.delete();
////        rppg.delete();
////        d.delete();
//
//        int c = 1;
//        Double b = 2.5;
//        List<? extends Number> numbers = Arrays.asList(c, b);
//        System.out.println(numbers.toString());
//    }
//
////    @Test
//    void test3(){
////        CsvUtil.createFile(new File("imgs/a"));
//        Date d =new Date();
//        System.out.println(System.currentTimeMillis());
//        System.out.println(d.getTime());
//    }
//
////    @Test
////    void testNlp() throws IOException {
////        List<String> speakers_keys = new ArrayList<>();
////        List<Double> speakers_time = new ArrayList<>();
////        List<Double> speakers_rate = new ArrayList<>();
////
////        List<String> emotions_keys = new ArrayList<>();
////        List<Double> emotions_time = new ArrayList<>();
////        List<Double> emotions_rate = new ArrayList<>();
////
////        List<String> acts_keys = new ArrayList<>();
////        List<Double> acts_time = new ArrayList<>();
////        List<Double> acts_rate = new ArrayList<>();
////
////        List<List<Double>> bar_speakers =new ArrayList<>();
////        List<List<Double>> bar_emotions =new ArrayList<>();
////
////        List<Double> total_time = new ArrayList<>();
////        List<String> sentences_array = new ArrayList<String>();
////        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_results.txt", "2");
////        List<NlpTable> nlpTables = NlpUtil.read_nlp(2L, nlp_data);
////        List<String> users = Arrays.asList("user00","user01","user02","user03","user04","user05","user06"
////                ,"user07","user08","user09");
////        NlpUtil.get_pie_and_bar(nlp_data,speakers_keys,speakers_time,speakers_rate,
////                emotions_keys,emotions_time,emotions_rate,
////                acts_keys,acts_time,acts_rate,bar_speakers,bar_emotions,
////                total_time,sentences_array,users);
////
////        int a = 0;
////    }
//
////    @Test
////    public  void testReadCSV() throws Exception {
/////*        List<String[]> strings = amazonUploadService.readCSV("v_results1.csv", "2");
//////        List<VResult> vResults = CsvUtil.read_v(2L, strings);
////        long start = System.currentTimeMillis();
//////        vResultService.addOneByOne(vResults);
////        List<String[]> sync_v = new ArrayList<>();
////        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(30000, "v",strings,sync_v,2L);
////        System.out.println(System.currentTimeMillis()-start);*/
////        Long meetingID = 1L;
////        String meeting = "1";
////        List<String[]> dataA = amazonUploadService.readCSV("a_results.csv", meeting);
////        List<String[]> dataV = amazonUploadService.readCSV("v_results.csv", meeting);
////        List<String[]> dataR = amazonUploadService.readCSV("rppg_results.csv", meeting);
////
////        List<String> userList = new ArrayList<>();
////        List<AResult> listA = CsvUtil.read_a(meetingID,dataA,userList);
////        List<VResult> listV = CsvUtil.read_v(meetingID,dataV);
////        List<RResult> listR = CsvUtil.read_r(meetingID,dataR);
//////        aResultService.insertA(listA);
//////        vResultService.insertV(listV);
//////        rResultService.insertR(listR);
////        List<String[]> sync_a = new ArrayList<>();
////        List<String[]> sync_v= new ArrayList<>();
////        List<String[]> sync_r = new ArrayList<>();
////
////        List<IndividualSyncA> isa = new ArrayList<>();
////        List<IndividualSyncV> isv = new ArrayList<>();
////        List<IndividualSyncR> isr = new ArrayList<>();
////
////        List<Async> listAsync = CsvUtil.get_and_save_sync_a(30000, "a",dataA,sync_a,meetingID,isa);
////        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(30000, "v",dataV,sync_v,meetingID,isv);
////        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(30000, "rppg",dataR,sync_r,meetingID,isr);
////
////        individualAService.saveBatch(isa);
////        individualRService.saveBatch(isr);
////        individualVService.saveBatch(isv);
////
////        //整合sync
////        List<List<Double>> sync_all = new ArrayList<>();
////        List<Double> row1 = new ArrayList<>();
////        List<Double> row2 = new ArrayList<>();
////        List<Double> row3 = new ArrayList<>();
////        List<Double> time_sync_all = new ArrayList<>();
////        for (int i = 1; i < sync_a.size(); i++) {
////            row1.add("".equals(sync_a.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[1]));
////            row2.add("".equals(sync_v.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_v.get(i)[1]));
////            row3.add("".equals(sync_r.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_r.get(i)[1]));
////            time_sync_all.add("".equals(sync_a.get(i)[0].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[0]));
////        }
////        sync_all.add(row1);
////        sync_all.add(row2);
////        sync_all.add(row3);
////        List<List<Double>> norm_sync = norm_min_max(sync_all);
//////        List<Double> ave = new ArrayList<>();
////        List<AveSync> aveSyncs = new ArrayList<>();
////
////        for (int i = 0; i < norm_sync.get(0).size(); i++) {
////            Double avg = 0.0d;
////            for(int j = 0; j < norm_sync.size(); j++){
////                avg += norm_sync.get(j).get(i);
////            }
////            avg /= norm_sync.size();
////            aveSyncs.add(new AveSync(meetingID,time_sync_all.get(i),avg));
//////            ave.add(avg);
////        }
////
////        aveSyncService.saveBatch(aveSyncs);
//////        aSyncService.insertA(listAsync);
//////        vSyncService.insertV(listVsync);
//////        rSyncService.insertR(listRsync);
////        System.out.println("a");
//// /*       List<List<Double>> sync_all = new ArrayList<>();
////        List<Double> row1 = new ArrayList<>();
////        List<Double> row2 = new ArrayList<>();
////        List<Double> row3 = new ArrayList<>();
////        List<Double> time_sync_all = new ArrayList<>();
////        for (int i = 1; i < sync_a.size(); i++) {
////            row1.add("".equals(sync_a.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[1]));
////            row2.add("".equals(sync_v.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_v.get(i)[1]));
////            row3.add("".equals(sync_r.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_r.get(i)[1]));
////            time_sync_all.add("".equals(sync_a.get(i)[0].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[0]));
////        }
////        sync_all.add(row1);
////        sync_all.add(row2);
////        sync_all.add(row3);
////        List<List<Double>> norm_sync = norm_min_max(sync_all);
//////        List<Double> ave = new ArrayList<>();
////        List<AveSync> aveSyncs = new ArrayList<>();
////
////        for (int i = 0; i < norm_sync.get(0).size(); i++) {
////            Double avg = 0.0d;
////            for(int j = 0; j < norm_sync.size(); j++){
////                avg += norm_sync.get(j).get(i);
////            }
////            avg /= norm_sync.size();
////            aveSyncs.add(new AveSync(time_sync_all.get(i),avg));
//////            ave.add(avg);
////        }*/
////        System.out.println("aa");
////        //        nlp相关处理
////        List<String[]> nlp_data = amazonUploadService.readNlp("nlp_results.txt", meeting);
////        List<NlpTable> nlpTables = NlpUtil.read_nlp(meetingID, nlp_data);
//////        nlpService.insertNlp(nlpTables);
////        List<String> speakers_keys = new ArrayList<>();
////        List<Double> speakers_time = new ArrayList<>();
////        List<Double> speakers_rate = new ArrayList<>();
////
////        List<String> emotions_keys = new ArrayList<>();
////        List<Double> emotions_time = new ArrayList<>();
////        List<Double> emotions_rate = new ArrayList<>();
////
////        List<String> acts_keys = new ArrayList<>();
////        List<Double> acts_time = new ArrayList<>();
////        List<Double> acts_rate = new ArrayList<>();
////
////        List<List<Double>> bar_speakers = new ArrayList<>();
////        List<List<Double>> bar_emotions = new ArrayList<>();
////
////        List<Double> total_time = new ArrayList<>();
////        List<String> sentences_array = new ArrayList<String>();
////        List<String> users = Arrays.asList("user00","user01","user02","user03","user04","user05","user06"
////                ,"user07","user08","user09");
////
////        NlpUtil.get_pie_and_bar(nlp_data,speakers_keys,speakers_time,speakers_rate,
////                emotions_keys,emotions_time,emotions_rate,
////                acts_keys,acts_time,acts_rate,bar_speakers,
////                bar_emotions,total_time,sentences_array,users);
////
////        List<PieSpeaker> pieSpeakers = new ArrayList<>();
////        List<PieEmotion> pieEmotions = new ArrayList<>();
////        List<PieAct> pieActs = new ArrayList<>();
////
////        for(int i = 0; i < speakers_keys.size(); i ++){
////            PieSpeaker pieSpeaker = new PieSpeaker();
////            pieSpeaker.setMeeting_id(meetingID);
////            pieSpeaker.setSpeaker(speakers_keys.get(i));
////            pieSpeaker.setSpeaker_time(speakers_time.get(i));
////            pieSpeaker.setSpeaker_time_rate(speakers_rate.get(i));
////            pieSpeaker.setNegative(bar_speakers.get(i).get(0));
////            pieSpeaker.setNeutral(bar_speakers.get(i).get(1));
////            pieSpeaker.setPositive(bar_speakers.get(i).get(2));
////            pieSpeakers.add(pieSpeaker);
////        }
////        for(int i = 0; i < emotions_keys.size(); i++){
////            PieEmotion pieEmotion = new PieEmotion();
////            pieEmotion.setMeeting_id(meetingID);
////            pieEmotion.setEmotion(emotions_keys.get(i));
////            pieEmotion.setEmotion_time(emotions_time.get(i));
////            pieEmotion.setEmotion_time_rate(emotions_rate.get(i));
////            pieEmotions.add(pieEmotion);
////        }
////        for(int i = 0; i< acts_keys.size(); i++){
////            PieAct pieAct = new PieAct();
////            pieAct.setMeeting_id(meetingID);
////            pieAct.setAct(acts_keys.get(i));
////            pieAct.setAct_time(acts_time.get(i));
////            pieAct.setAct_time_rate(acts_rate.get(i));
////            pieActs.add(pieAct);
////        }
////
//////        pieSpeakerService.insertPie(pieSpeakers);
//////        pieEmotionService.insertPie(pieEmotions);
//////        pieActService.insertPie(pieActs);
////
////        List<BarEmotion> barEmotions = new ArrayList<>();
////        for(int i = 0; i < emotions_keys.size(); i++ ){
////            for(int j = 0; j < speakers_keys.size(); j++){
////                BarEmotion barEmotion = new BarEmotion();
////                barEmotion.setMeeting_id(meetingID);
////                barEmotion.setEmotion(emotions_keys.get(i));
////                barEmotion.setUsers(speakers_keys.get(j));
////                barEmotion.setScore(bar_emotions.get(i).get(j));
////                barEmotions.add(barEmotion);
////            }
////        }
//////        barEmotionService.insertBar(barEmotions);
////
//////        radar部分处理
////        List<Double> radar_chart_list = new ArrayList<>();
////        List<String> r_keys = new ArrayList<>();
////        List<Radar> radars = new ArrayList<>();
////        NlpUtil.get_radar_components(speakers_time,total_time.get(0),acts_time,emotions_time,sentences_array,dataV,radar_chart_list,r_keys,users);
////
////        for(int i = 0; i < radar_chart_list.size(); i++){
////            Radar r = new Radar();
////            r.setMeeting_id(meetingID);
////            r.setK(r_keys.get(i));
////            r.setV(radar_chart_list.get(i));
////            radars.add(r);
////        }
//////        radarService.insertRadar(radars);
//////        List<String> users = Arrays.asList("user00","user01","user02","user03","user04","user05","user06"
//////                ,"user07","user08","user09");
//////        section部分
////        List<List<Double>> hrv_diff = CsvUtil.get_hrv_diff(3000, dataR);
////        List<List<Double>> hrv_diff_abs_norm = CsvUtil.norm_min_max(hrv_diff);
////        List<List<List<Double>>> va_diff = CsvUtil.get_va_diff(3000, dataV, dataA);
////        List<List<Double>> v_diff_abs_norm = CsvUtil.norm_min_max(va_diff.get(0));
////        List<List<Double>> a_diff_abs_norm = CsvUtil.norm_min_max(va_diff.get(1));
////
////        List<Long> time = new ArrayList<>();
////        List<Section> team = new ArrayList<>();
////        List<Section> user = new ArrayList<>();
////
////        CsvUtil.sections(hrv_diff,hrv_diff_abs_norm,va_diff.get(0),va_diff.get(1),v_diff_abs_norm,a_diff_abs_norm,user,users);
////
////        CsvUtil.get_team_top3(3000,sync_r,sync_v,sync_a,time);
////        for(Long t : time){
////            Section section = new Section();
////            section.setMeeting_id(meetingID);
////            section.setStarts(t);
////            section.setEnds(t + 3000);
////            section.setUsers("team");
////            section.setLabel(-1);
////            team.add(section);
////        }
////        for(int i = 0; i < user.size(); i++){
////            user.get(i).setMeeting_id(meetingID);
////        }
//////        sectionService.insertSection(team);
//////        sectionService.insertSection(user);
////
////
////    }
//
////    @Test
//    public void test(){
//        long start = System.currentTimeMillis();
//        int count = 0;
//        for (int i = 0; i < 100000000; i++) {
//            count += 1;
//        }
//        System.out.println(start);
//        System.out.println(System.currentTimeMillis());
//    }
//
////    @Test
//    public void chat(){
//        String hh = sendPost("请介绍一下你自己");
//        System.out.println(hh);
//    }
//
//    public static String sendPost(String data) {
//        RestTemplate client = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("Authorization","Bearer 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");
//        httpHeaders.add("Content-Type", "application/json"); // 传递请求体时必须设置
////        String requestJson = "{\n" +
////                "    \"model\": \"text-davinci-003\",\n" +
////                "     \"prompt\": \"你好\",\n" +
////                "      \"temperature\": 0, \n" +
////                "      \"max_tokens\": 2048\n" +
////                "}";
//        String requestJson = String.format(
//                "{\n" +
//                        "    \"model\": \"gpt-3.5-turbo\",\n" +
//                        "     \"prompt\": \"%s\",\n" +
//                        "      \"temperature\": 0, \n" +
//                        "      \"max_tokens\": 2048\n" +
//                        "}",data
//        );
//        HttpEntity<String> entity = new HttpEntity<String>(requestJson,httpHeaders);
//        ResponseEntity<String> response = client.exchange("https://api.openai.com/v1/chat/completions", HttpMethod.POST, entity, String.class);
//        System.out.println(response.getBody());
//        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
//        JSONArray choices = jsonObject.getJSONArray("choices");
//        String text = choices.getJSONObject(0).getString("text");
////        Object o = jsonObject.get("\"choices\"");
//        return text;
//    }
//
//
//
//
//
//}
//
//
