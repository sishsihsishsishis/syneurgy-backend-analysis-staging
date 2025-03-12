package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.entity.*;
import com.aws.sync.service.*;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.utils.SyncUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.aws.sync.constants.CsvConstants.WINDOW_LENGTH_MS;
import static com.aws.sync.constants.MeetingConstants.SYNCHRONY_MOMENT_HANDLE;
import static com.aws.sync.utils.CsvUtil.norm_min_max;

@Slf4j
@Service
public class CVHandleServiceImpl implements CVHandleService {

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    MeetingService meetingService;

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
    HeatmapService heatmapService;

    @Autowired
    SectionService sectionService;

    @Autowired
    CVUserService cvUserService;

    @Autowired
    IndividualScoreService individualScoreService;

    @Autowired
    EmojiService emojiService;

    @Autowired
    UserDistanceService userDistanceService;

    @Autowired
    PosAndNegRateService posAndNegRateService;

    @Resource
    SynchronyMomentService synchronyMomentService;

    @Autowired
    UserContributionService userContributionService;

    @Autowired
    UniverseGroupService universeGroupService;

    @Autowired
    UserAvatarService userAvatarService;

    @Autowired
    RadarService radarService;

    @Override
    public RestResult handleCV(Long meetingID) throws Exception {
        log.info("[CVHandleServiceImpl][handleCV] meetingID :{}", meetingID);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id", meetingID);
        String meeting = Long.toString(meetingID);

        //handle result、sync
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);

        if (dataA.size() == 0) {
            meetingService.updateCVHandle(meetingID);
            return RestResult.success().data("CV data handled");
        }

        List<String> userList = new ArrayList<>();
        List<AResult> listA = CsvUtil.read_a(meetingID, dataA, userList);
        List<VResult> listV = CsvUtil.read_v(meetingID,dataV);
        List<RResult> listR = CsvUtil.read_r(meetingID,dataR);
        List<Long> timeline = getTimeLine(listA);
        Double hrv = CsvUtil.get_hrv(WINDOW_LENGTH_MS, dataR, timeline.get(0) * 1.0, timeline.get(timeline.size() - 1) * 1.0)
                                  .stream()
                                  .filter(num -> num != null && !Double.isNaN(num))
                                  .mapToDouble(Double::doubleValue)
                                  .average()
                                  .orElse(Double.NaN);

        List<Number> total_rate = new ArrayList<>();





        List<PosNegRate> positiveAndNegative = CsvUtil.getPositiveAndNegative(dataA, dataV, userList, meetingID, total_rate);

        posAndNegRateService.removeByMap(deleteMap);
        posAndNegRateService.saveBatch(positiveAndNegative);
        System.out.println("debug");
        LambdaUpdateWrapper<MeetingTable> updatePosAndNegRateAndHrv = new LambdaUpdateWrapper<>();
        updatePosAndNegRateAndHrv.eq(MeetingTable::getMeeting_id,meetingID)
                .set(MeetingTable::getA_positive_rate, total_rate.get(0))
                .set(MeetingTable::getA_negative_rate, total_rate.get(1))
                .set(MeetingTable::getV_positive_rate, total_rate.get(2))
                .set(MeetingTable::getV_negative_rate, total_rate.get(3));
        updatePosAndNegRateAndHrv.set(MeetingTable::getHrv, hrv);

        meetingService.update(updatePosAndNegRateAndHrv);

        List<EmojiTable> emojiTables = CsvUtil.handleIndividualEmoji(meetingID, listA, listV, userList.size(), timeline);

        List<GroupEmoji> groupEmojiList = CsvUtil.handleUniverseGroupEmoji(meetingID, listA, listV, userList.size(), timeline);


        universeGroupService.removeByMap(deleteMap);
        universeGroupService.addGroupEmojiOneByOne(groupEmojiList);

        emojiService.removeByMap(deleteMap);
        emojiService.addEmojiOneByOne(emojiTables);

        cvUserService.removeByMap(deleteMap);
        MeetingTable meetingTable = meetingService.getByMeetingId(meetingID);
        //add CVUser to CVUser_table
        for (String user : userList) {
            CVUser addUser = new CVUser();
            addUser.setUser_name(user);
            addUser.setMeeting_id(meetingID);
            addUser.setTeam_id(meetingTable.getMeeting_id());
            addUser.setCreate_date(meetingTable.getVideo_create_time());
            addUser.setTeam_id(meetingTable.getTeam_id());
            cvUserService.save(addUser);
        }
        System.out.println("debug");
        //第二次处理时，删除多余的userAvatar
/*        LambdaQueryWrapper<UserAvatar> userAvatarLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userAvatarLambdaQueryWrapper.eq(UserAvatar::getMeeting_id, meetingID);
        List<UserAvatar> userAvatars = userAvatarService.list(userAvatarLambdaQueryWrapper);
        for (UserAvatar userAvatar : userAvatars) {
            String user = userAvatar.getUsers();
            if (!userList.contains(user)) {
                HashMap<String, Object> delMap = new HashMap<>();
                delMap.put("meeting_id", userAvatar.getMeeting_id());
                delMap.put("users", user);
                userAvatarService.removeByMap(deleteMap);
            }
        }*/

        //TODO: 插入user 顺序调整，sync文件判断路径是否存在
        aResultService.removeByMap(deleteMap);
        vResultService.removeByMap(deleteMap);
        rResultService.removeByMap(deleteMap);

        aResultService.addOneByOne(listA);
        vResultService.addOneByOne(listV);
        rResultService.addOneByOne(listR);

        List<String[]> sync_a = new ArrayList<>();
        List<String[]> sync_v= new ArrayList<>();
        List<String[]> sync_r = new ArrayList<>();
        List<IndividualSyncA> isa = new ArrayList<>();
        List<IndividualSyncV> isv = new ArrayList<>();
        List<IndividualSyncR> isr = new ArrayList<>();


        List<Async> listAsync = CsvUtil.get_and_save_sync_a(10000, CsvConstants.CSV_FILE_A, dataA, sync_a, meetingID, isa);


        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(10000, CsvConstants.CSV_FILE_V, dataV, sync_v, meetingID, isv);

        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, meetingID, isr);

        //此处通过rppg求Brain的方式对blink获取brainScore
        //List<String[]> dataB = amazonUploadService.readBlinkData(CsvConstants.BLINK_RESULT, meetingID);
        List<String[]> sync_b = new ArrayList<>();
        List<IndividualSyncR> isb = new ArrayList<>();
        //List<Rsync> listBsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_BLINK, dataB, sync_b, meetingID, isb);

        /*List<IndividualScore> individual_score = CsvUtil.get_individual_score(meetingID, isa, isv, isr, isb);
        individualScoreService.removeByMap(deleteMap);
        individualScoreService.saveBatch(individual_score);*/


        //update duration
        meetingService.updateDuration(meetingID,isa.get(isa.size() - 1).getTime_ms());

        //update team sync
        Double team_sync = CsvUtil.get_team_sync(listAsync, listVsync, listRsync);
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper
                .set(MeetingTable::getTeam_distance, team_sync)
                .eq(MeetingTable::getMeeting_id, meetingID);
        meetingService.update(lambdaUpdateWrapper);

        //individual contribution
        List<IndividualSync> individualSyncAll = new ArrayList<>();
        SyncUtil.handleIndividualSyncAll(isa, isv, isr, individualSyncAll);
        List<UserDistance> userDistanceList = SyncUtil.handleIndividualDistance(individualSyncAll, meetingID, team_sync);

        //add 7/23
        List<UserContribution> userContributionList = SyncUtil.handleMyContribution(individualSyncAll, meetingID);
        userContributionService.removeByMap(deleteMap);
        userContributionService.saveBatch(userContributionList);

        userDistanceService.removeByMap(deleteMap);
        userDistanceService.saveBatch(userDistanceList);

        individualSyncService.removeByMap(deleteMap);
        individualSyncService.addOneByOne(individualSyncAll);

        aSyncService.removeByMap(deleteMap);
        vSyncService.removeByMap(deleteMap);
        rSyncService.removeByMap(deleteMap);
        aSyncService.insertA(listAsync);
        vSyncService.insertV(listVsync);
        rSyncService.insertR(listRsync);

        individualAService.removeByMap(deleteMap);
        individualVService.removeByMap(deleteMap);
        individualVService.removeByMap(deleteMap);
        individualAService.saveBatch(isa);
        individualRService.saveBatch(isr);
        individualVService.saveBatch(isv);

        //整合sync
        List<List<Double>> sync_all = new ArrayList<>();
        List<Double> row1 = new ArrayList<>();
        List<Double> row2 = new ArrayList<>();
        List<Double> row3 = new ArrayList<>();
        List<Double> time_sync_all = new ArrayList<>();
        for (int i = 1; i < sync_a.size(); i++) {
            row1.add("".equals(sync_a.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[1]));
            row2.add("".equals(sync_v.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_v.get(i)[1]));
            row3.add("".equals(sync_r.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_r.get(i)[1]));
            time_sync_all.add("".equals(sync_a.get(i)[0].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[0]));
        }
        sync_all.add(row1);
        sync_all.add(row2);
        sync_all.add(row3);

        List<List<Double>> norm_sync = norm_min_max(sync_all);

        List<AveSync> aveSyncs = new ArrayList<>();

        for (int i = 0; i < norm_sync.get(0).size(); i++) {
            Double avg = 0.0d;
            int count = 0;
            for(int j = 0; j < norm_sync.size(); j++){
                if(!Double.isNaN(norm_sync.get(j).get(i))){
                    avg += norm_sync.get(j).get(i);
                    count++;
                }
            }
            if(count != 0){
                avg /= count;
            }else {
                avg = Double.NaN;
            }

            aveSyncs.add(new AveSync(meetingID, time_sync_all.get(i), avg));
        }


        computeAndSetCurrentAndAverage(aveSyncs);


        aveSyncService.removeByMap(deleteMap);
        aveSyncService.saveBatch(aveSyncs);

/*        上传csv文件 TODO: 需要解决(系统找不到指定的路径。)
        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_A),meeting);
        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_V),meeting);
        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_RPPG),meeting);
        删除本地文件
        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_A).delete();
        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_V).delete();
        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_RPPG).delete();
        new File(CsvConstants.CSV_SYNC_PRE+ meeting).delete();*/

        //处理score
        Score scores = CsvUtil.get_scores(sync_a, sync_v, sync_r);
        scores.setMeeting_id(meetingID);
        meetingService.updateScore(scores);

        //TODO: 处理radar
        LambdaQueryWrapper<Radar> radarLambdaQueryWrapper = new LambdaQueryWrapper<>();
        radarLambdaQueryWrapper.eq(Radar::getMeeting_id, meetingID);
        List<Radar> radars = radarService.list(radarLambdaQueryWrapper);
        //说明还没拆分Trust
        if (radars.size() == 5 && scores.getBehavior_score() != null && scores.getBody_score() != null) {
            //1、找到Trust and Psychological Safety的索引
            int ind = -1;
            Double value = 0.0d;
            for (int i = 0; i < radars.size(); i++) {
                if ("Trust and Psychological Safety".equals(radars.get(i).getK())) {
                    ind = i;
                    value = radars.get(i).getV();
                    break;
                }
            }
            if (ind != -1) {
                radars.remove(ind);
                Double behaviourScore = scores.getBehavior_score();
                Double bodyScore = scores.getBody_score();
                Double rateTrust = behaviourScore / (behaviourScore + bodyScore);
                if (rateTrust > 0.6) rateTrust = 0.6;
                if (rateTrust < 0.4) rateTrust = 0.4;
                Double ratePsy = 1 - rateTrust;
                radars.add(new Radar(meetingID, "Trust", rateTrust * value * 2));
                radars.add(new Radar(meetingID, "Psychological Safety", ratePsy * value * 2));
                radarService.removeByMap(deleteMap);
                log.info("[CVHandleServiceImpl][handleCV] meetingID :{}, radars:{}", meetingID, radars);
                Set<Radar> radarSet = new HashSet<>(radars);
                List<Radar> uniqueRadars = new ArrayList<>(radarSet);
                log.info("[CVHandleServiceImpl][handleCV] meetingID :{}, uniqueRadars:{}", meetingID, uniqueRadars);
                radarService.insertRadar(uniqueRadars);
            }
        }

        //处理heatmap
        List<Heatmap> heatmaps = CsvUtil.va_heatmap(meetingID, dataV, dataA);
        heatmapService.removeByMap(deleteMap);
        heatmapService.addOneByOne(heatmaps);

        //section部分
       /* List<List<Double>> hrv_diff = CsvUtil.get_hrv_diff(WINDOW_LENGTH_MS, dataR);
        List<List<Double>> hrv_diff_abs_norm = norm_min_max(hrv_diff);
        List<List<List<Double>>> va_diff = CsvUtil.get_va_diff(WINDOW_LENGTH_MS, dataV, dataA);
        List<List<Double>> v_diff_abs_norm = norm_min_max(va_diff.get(0));
        List<List<Double>> a_diff_abs_norm = norm_min_max(va_diff.get(1));

        List<Long> time = new ArrayList<>();
        List<Integer> label = new ArrayList<>();
        List<Section> team = new ArrayList<>();
        List<Section> user = new ArrayList<>();

        CsvUtil.sections(hrv_diff,hrv_diff_abs_norm,va_diff.get(0),va_diff.get(1),v_diff_abs_norm,a_diff_abs_norm,user,userList);

        //改为Gpt处理,在nlpService中处理
        sectionService.removeByMap(deleteMap);
        if(userList.size() > 1){
            CsvUtil.get_team_top3(WINDOW_LENGTH_MS, sync_r, sync_v, sync_a, time, label);
            for (int i = 0; i < time.size() && i < label.size(); i++) {
                team.add(new Section(meetingID, time.get(i),time.get(i) + WINDOW_LENGTH_MS, label.get(i), CsvConstants.USER_TEAM));
            }
            sectionService.insertSection(team);
        }*/




        meetingService.updateCVHandle(meetingID);

        LambdaQueryWrapper<MeetingTable> syncMomentQueryWrapper = new LambdaQueryWrapper<>();
        syncMomentQueryWrapper.eq(MeetingTable::getMeeting_id, meetingID);
        List<MeetingTable> tables = meetingService.list(syncMomentQueryWrapper);

        if(BooleanUtils.isTrue(meetingService.checkNlpHandle(meetingID))){
            meetingService.updateDataHandle(meetingID);
            if(tables != null && tables.size() > 0){
                synchronyMomentService.removeByMap(deleteMap);
                synchronyMomentService.saveSmallest3(meetingID);
                UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meeting_id",meetingID)
                        .set("synchrony_moment_handle",SYNCHRONY_MOMENT_HANDLE);
                meetingService.update(null, updateWrapper);
            }

            return RestResult.success().data("All data handled");
        }

        return RestResult.success().data("CV data handled");
    }

    public static String toString(List<List<Double>> listOfLists) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < listOfLists.size(); i++) {
            List<Double> innerList = listOfLists.get(i);
            sb.append("[");
            for (int j = 0; j < innerList.size(); j++) {
                sb.append(innerList.get(j));
                if (j < innerList.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            if (i < listOfLists.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public RestResult handleScore(Long meetingID, int coefficientBody,int coefficientBehaviour, int coefficientTotal) throws Exception {
        String meeting = Long.toString(meetingID);
        //handle result、sync
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
        //处理score
//        Score scores = CsvUtil.get_score(dataR, dataV, dataA, coefficientBody, coefficientBehaviour, coefficientTotal);
//        scores.setMeeting_id(meetingID);
//        meetingService.updateScore(scores);
        return RestResult.success().data("Score data handled");
    }

    @Override
    public RestResult handlePartCV(Long meetingID, Integer part) throws Exception {
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);

        String meeting = Long.toString(meetingID);

        //handle result、sync
        List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
        List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
        List<String> userList = new ArrayList<>();
        List<AResult> listA = CsvUtil.read_a(meetingID,dataA,userList);
        List<VResult> listV = CsvUtil.read_v(meetingID,dataV);
        List<RResult> listR = CsvUtil.read_r(meetingID,dataR);
        List<Long> timeline = getTimeLine(listA);

        if (part == 0) {
            //处理score
            Score scores = CsvUtil.get_scores(dataR,dataV,dataA);
            scores.setMeeting_id(meetingID);
            meetingService.updateScore(scores);
            return RestResult.success();
        }
        if (part == 1) {
            //TODO: 1、清除数据  2、处理handle标记
            Double hrv = CsvUtil.get_hrv(WINDOW_LENGTH_MS, dataR, timeline.get(0) * 1.0, timeline.get(timeline.size() - 1) * 1.0)
                    .stream()
                    .filter(num -> num != null && !Double.isNaN(num))
                    .mapToDouble(Double::doubleValue)
                    .average().getAsDouble();
            List<Number> total_rate = new ArrayList<>();
            List<PosNegRate> positiveAndNegative = CsvUtil.getPositiveAndNegative(dataA, dataV, userList, meetingID,total_rate);
            posAndNegRateService.removeByMap(deleteMap);
            posAndNegRateService.saveBatch(positiveAndNegative);
            LambdaUpdateWrapper<MeetingTable> updatePosAndNegRateAndHrv = new LambdaUpdateWrapper<>();
            updatePosAndNegRateAndHrv.eq(MeetingTable::getMeeting_id,meetingID)
                    .set(MeetingTable::getA_positive_rate, total_rate.get(0))
                    .set(MeetingTable::getA_negative_rate, total_rate.get(1))
                    .set(MeetingTable::getV_positive_rate, total_rate.get(2))
                    .set(MeetingTable::getV_negative_rate, total_rate.get(3));
            updatePosAndNegRateAndHrv.set(MeetingTable::getHrv, hrv);
            meetingService.update(updatePosAndNegRateAndHrv);
            return RestResult.success().message("Positive and negative rate data add success!");
        }

        if (part == 2) {
            cvUserService.removeByMap(deleteMap);
            //add CVUser to CVUser_table
            for (String user : userList) {
                cvUserService.save(new CVUser(meetingID, user));
            }
            return RestResult.success().message("CV user data add success!");
        }

        if (part == 3) {
            List<EmojiTable> emojiTables = CsvUtil.handleIndividualEmoji(meetingID, listA, listV, userList.size(), timeline);
            emojiService.removeByMap(deleteMap);
            emojiService.addEmojiOneByOne(emojiTables);
            return RestResult.success().message("Emoji data add success!");
        }
        if (part == 4) {
            List<GroupEmoji> groupEmojiList = CsvUtil.handleUniverseGroupEmoji(meetingID, listA, listV, userList.size(), timeline);
            universeGroupService.removeByMap(deleteMap);
            universeGroupService.addGroupEmojiOneByOne(groupEmojiList);
            return RestResult.success().message("GroupEmoji data add success!");
        }

        if (part == 5) {
            //TODO: 插入user 顺序调整，sync文件判断路径是否存在
            aResultService.removeByMap(deleteMap);
            vResultService.removeByMap(deleteMap);
            rResultService.removeByMap(deleteMap);

            aResultService.addOneByOne(listA);
            vResultService.addOneByOne(listV);
            rResultService.addOneByOne(listR);
            return RestResult.success().message("Result data add success!");
        }

/*        List<String[]> sync_a = new ArrayList<>();
        List<String[]> sync_v= new ArrayList<>();
        List<String[]> sync_r = new ArrayList<>();
        List<IndividualSyncA> isa = new ArrayList<>();
        List<IndividualSyncV> isv = new ArrayList<>();
        List<IndividualSyncR> isr = new ArrayList<>();



        List<Async> listAsync = CsvUtil.get_and_save_sync_a(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_A, dataA, sync_a, meetingID, isa);
        List<Vsync> listVsync = CsvUtil.get_and_save_sync_v(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_V, dataV, sync_v, meetingID, isv);
        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(WINDOW_LENGTH_MS, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, meetingID, isr);

        if (part == 6) {
            List<IndividualScore> individual_score = CsvUtil.get_individual_score(meetingID,isa, isv, isr);
            individualScoreService.removeByMap(deleteMap);
            individualScoreService.saveBatch(individual_score);
            return RestResult.success().message("IndividualScore data add success!");
        }



        //update duration
        meetingService.updateDuration(meetingID,isa.get(isa.size() - 1).getTime_ms());

        //update team sync
        Double team_sync = CsvUtil.get_team_sync(listAsync, listVsync, listRsync);
        LambdaUpdateWrapper<MeetingTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper
                .set(MeetingTable::getTeam_distance,team_sync)
                .eq(MeetingTable::getMeeting_id,meetingID);
        meetingService.update(lambdaUpdateWrapper);

        //individual contribution
        List<IndividualSync> individualSyncAll = new ArrayList<>();
        SyncUtil.handleIndividualSyncAll(isa,isv,isr,individualSyncAll);
        List<UserDistance> userDistanceList = SyncUtil.handleIndividualDistance(individualSyncAll, meetingID, team_sync);

        List<UserContribution> userContributionList = SyncUtil.handleMyContribution(individualSyncAll, meetingID);
        if (part == 7) {
            userContributionService.removeByMap(deleteMap);
            userDistanceService.removeByMap(deleteMap);

            userContributionService.saveBatch(userContributionList);
            userDistanceService.saveBatch(userDistanceList);
            return RestResult.success();
        }
        //add

        if (part == 8) {
            individualScoreService.removeByMap(deleteMap);
            aSyncService.removeByMap(deleteMap);
            vSyncService.removeByMap(deleteMap);
            rSyncService.removeByMap(deleteMap);
            individualAService.removeByMap(deleteMap);
            individualRService.removeByMap(deleteMap);
            individualVService.removeByMap(deleteMap);

            individualSyncService.addOneByOne(individualSyncAll);

            aSyncService.insertA(listAsync);
            vSyncService.insertV(listVsync);
            rSyncService.insertR(listRsync);

            individualAService.saveBatch(isa);
            individualRService.saveBatch(isr);
            individualVService.saveBatch(isv);
            return RestResult.success();
        }


        if (part == 9) {
            //整合sync
            List<List<Double>> sync_all = new ArrayList<>();
            List<Double> row1 = new ArrayList<>();
            List<Double> row2 = new ArrayList<>();
            List<Double> row3 = new ArrayList<>();
            List<Double> time_sync_all = new ArrayList<>();
            for (int i = 1; i < sync_a.size(); i++) {
                row1.add("".equals(sync_a.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[1]));
                row2.add("".equals(sync_v.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_v.get(i)[1]));
                row3.add("".equals(sync_r.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(sync_r.get(i)[1]));
                time_sync_all.add("".equals(sync_a.get(i)[0].trim()) ? Double.NaN : Double.parseDouble(sync_a.get(i)[0]));
            }
            sync_all.add(row1);
            sync_all.add(row2);
            sync_all.add(row3);
            List<List<Double>> norm_sync = norm_min_max(sync_all);
            List<AveSync> aveSyncs = new ArrayList<>();

            for (int i = 0; i < norm_sync.get(0).size(); i++) {
                Double avg = 0.0d;
                int count = 0;
                for(int j = 0; j < norm_sync.size(); j++){
                    if(!Double.isNaN(norm_sync.get(j).get(i))){
                        avg += norm_sync.get(j).get(i);
                        count++;
                    }
                }
                if(count != 0){
                    avg /= count;
                }else {
                    avg = Double.NaN;
                }

                aveSyncs.add(new AveSync(meetingID,time_sync_all.get(i),avg));
            }

            computeAndSetCurrentAndAverage(aveSyncs);

            aveSyncService.removeByMap(deleteMap);
            aveSyncService.saveBatch(aveSyncs);
            return RestResult.success().data("AveSync data add success!");
        }

//        //上传csv文件 TODO: 需要解决(系统找不到指定的路径。)
////        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_A),meeting);
////        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_V),meeting);
////        amazonUploadService.saveCSV(new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_RPPG),meeting);
////        删除本地文件
////        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_A).delete();
////        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_V).delete();
////        new File(CsvConstants.CSV_SYNC_PRE+ meeting + "/" + CsvConstants.CSV_SYNC_RPPG).delete();
////        new File(CsvConstants.CSV_SYNC_PRE+ meeting).delete();



        if (part == 10) {
            //处理heatmap
            List<Heatmap> heatmaps = CsvUtil.va_heatmap(meetingID,dataV,dataA);
            heatmapService.removeByMap(deleteMap);
            heatmapService.addOneByOne(heatmaps);
            return RestResult.success().message("Heatmap data add success!");
        }

        if (part == 11) {
            //section部分
            List<List<Double>> hrv_diff = CsvUtil.get_hrv_diff(WINDOW_LENGTH_MS, dataR);
            List<List<Double>> hrv_diff_abs_norm = norm_min_max(hrv_diff);
            List<List<List<Double>>> va_diff = CsvUtil.get_va_diff(WINDOW_LENGTH_MS, dataV, dataA);
            List<List<Double>> v_diff_abs_norm = norm_min_max(va_diff.get(0));
            List<List<Double>> a_diff_abs_norm = norm_min_max(va_diff.get(1));

            List<Long> time = new ArrayList<>();
            List<Integer> label = new ArrayList<>();
            List<Section> team = new ArrayList<>();
            List<Section> user = new ArrayList<>();

            CsvUtil.sections(hrv_diff,hrv_diff_abs_norm,va_diff.get(0),va_diff.get(1),v_diff_abs_norm,a_diff_abs_norm,user,userList);

            sectionService.removeByMap(deleteMap);

            if(userList.size() > 1){
                CsvUtil.get_team_top3(WINDOW_LENGTH_MS,sync_r,sync_v,sync_a,time,label);
                for (int i = 0; i < time.size(); i++) {
                    team.add(new Section(meetingID,time.get(i),time.get(i) + WINDOW_LENGTH_MS,label.get(i),CsvConstants.USER_TEAM));
                }
                sectionService.insertSection(team);
            }


            for(int i = 0; i < user.size(); i++){
                user.get(i).setMeeting_id(meetingID);
            }

            sectionService.insertSection(user);
        }*/

/*        meetingService.updateCVHandle(meetingID);
        meetingService.updateUserMerge(meetingID);

        LambdaQueryWrapper<MeetingTable> syncMomentQueryWrapper = new LambdaQueryWrapper<>();
        syncMomentQueryWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        List<MeetingTable> tables = meetingService.list(syncMomentQueryWrapper);

        if(BooleanUtils.isTrue(meetingService.checkNlpHandle(meetingID))&& meetingService.checkMatch(meetingID)){
            meetingService.updateDataHandle(meetingID);

            if(tables != null && tables.size() > 0 && tables.get(0).getSynchrony_moment_handle() == 0){
                synchronyMomentService.saveSmallest3(meetingID);
                UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meeting_id",meetingID)
                        .set("synchrony_moment_handle",SYNCHRONY_MOMENT_HANDLE);
                meetingService.update(null,updateWrapper);
            }

            return RestResult.success().data("All data handled");
        }*/
        return RestResult.success().data("CV data handled");
    }

    @Override
    public RestResult removePartCV(Long meetingID, Integer part) throws Exception {
        return null;
    }

    private void computeAndSetCurrentAndAverage(List<AveSync> aveSyncs) {
        if (aveSyncs == null || aveSyncs.isEmpty()) {
            return;
        }
        //使用前一个有效值替换NaN
        Double preCurrent = 0.0d;
        for (int i = 0; i < aveSyncs.size(); i++) {
            AveSync aveSync = aveSyncs.get(i);
            if (aveSync.getAve() == null || Double.isNaN(aveSync.getAve())) {
                aveSync.setCurrent(preCurrent);
            } else {
                preCurrent = aveSync.getAve();
                aveSync.setCurrent(preCurrent);
            }
        }
        Double pre = 0.0d;
        for (int i = 0; i < aveSyncs.size(); i++) {
            AveSync aveSync = aveSyncs.get(i);
            if (i == 0) {
                aveSync.setAcc_average(aveSync.getCurrent());
            } else {
                aveSync.setAcc_average((pre + aveSync.getCurrent()) / 2);
            }
            pre = aveSyncs.get(i).getAcc_average();
        }
    }

    private List<Long> getTimeLine(List<AResult> listA) {
        List<Long> timeline = new ArrayList<>();
        for (AResult aResult : listA) {
            timeline.add(aResult.getTime_ms());
        }
        return  timeline;
    }
}
