package com.aws.sync.service.impl;

import com.aws.sync.constants.CsvConstants;
import com.aws.sync.entity.*;
import com.aws.sync.mapper.*;
import com.aws.sync.service.*;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.vo.RadarVO;
import com.aws.sync.vo.SectionTeamVO;
import com.aws.sync.vo.SectionUserVO;
import com.aws.sync.vo.SectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.aws.sync.constants.CsvConstants.*;

@Service
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService {
    private static final String LABEL_TEAM = "team";
    private static final String USER_PREFIX = "user";
    private static final Double TEAM_PARTICIPATION = 1.0;
    private static final Double MOMENT_SYNCHRONY_PARTICIPATION = 1.0;
    private static final Integer WINDOW_LENGTH = 30000;

    @Resource
    SectionMapper sectionMapper;

    @Resource
    AResultMapper aResultMapper;

    @Resource
    VResultMapper vResultMapper;

    @Resource
    MeetingMapper meetingMapper;

    @Autowired
    IndividualSyncService individualSyncService;

    @Resource
    SynchronyMomentMapper synchronyMomentMapper;

    @Resource
    NlpWordCountMapper nlpWordCountMapper;

    @Autowired
    AmazonUploadService amazonUploadService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSection(List<Section> sections) {
        return sectionMapper.addBatch(sections);
    }

    @Override
    public List<SectionTeamVO> findTeam(Long meetingID) {
        LambdaQueryWrapper<Section> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Section::getMeeting_id,meetingID)
                          .eq(Section::getUsers,LABEL_TEAM);
        List<Section> sections = sectionMapper.selectList(lambdaQueryWrapper);
        List<SectionTeamVO> sectionTeamVOList = new ArrayList<>();
        for (Section section : sections) {
            sectionTeamVOList.add(convertToSectionTeamVO(section));
        }
//        return sectionMapper.findTeam(meetingID);
        return sectionTeamVOList;
    }

    private SectionTeamVO convertToSectionTeamVO(Section section) {
        SectionTeamVO s = new SectionTeamVO();
        BeanUtils.copyProperties(section,s);
        return s;
    }

    @Override
    public List<SectionUserVO> findUser(Long meetingID) {
        LambdaQueryWrapper<Section> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Section::getMeeting_id,meetingID)
                          .ne(Section::getUsers,LABEL_TEAM);
        List<Section> sections = sectionMapper.selectList(lambdaQueryWrapper);
        List<SectionUserVO> sectionUserVOList = new ArrayList<>();
        for (Section section : sections) {
            sectionUserVOList.add(convertToSectionUserVO(section));
        }
//        return sectionMapper.findUser(meetingID);
        return sectionUserVOList;
    }

    private SectionUserVO convertToSectionUserVO(Section section) {
        SectionUserVO s = new SectionUserVO();
        BeanUtils.copyProperties(section,s);
        return s;
    }

    @Override
    public void addAdditionInfoToSection(Long meetingID) throws NoSuchFieldException, IllegalAccessException, IOException {
        List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meetingID.toString());
        //1、handle team
        LambdaQueryWrapper<Section> teamQueryWrapper = new LambdaQueryWrapper<>();
        teamQueryWrapper.eq(Section::getMeeting_id,meetingID).eq(Section::getUsers, LABEL_TEAM);
        List<Section> sections = sectionMapper.selectList(teamQueryWrapper);

        //1.1、Compute Team Attention
        LambdaQueryWrapper<AResult> aResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        aResultLambdaQueryWrapper.eq(AResult::getMeeting_id,meetingID);
        List<AResult> aResults = aResultMapper.selectList(aResultLambdaQueryWrapper);
        for (Section section : sections) {
            Double start = section.getStarts() * 1000;
            Double end = section.getEnds() * 1000;
            Double total = 0.0d;
            int count = 0;
            for (AResult aResult : aResults) {
                if(aResult.getA_mean() != null && !Double.isNaN(aResult.getA_mean()) && aResult.getTime_ms() >= start && aResult.getTime_ms() <= end){
                    count++;
                    total+= aResult.getA_mean();
                }
            }

            Double avg_team_a = 0.0d;
            if(count != 0){
                avg_team_a = total / count;
            }else {
                avg_team_a = Double.NaN;
            }
            section.setAttention(avg_team_a);
            section.setParticipation(TEAM_PARTICIPATION);

            List<Double> hrv = CsvUtil.get_hrv(WINDOW_LENGTH, dataR, start, end);
            Double average = Double.NaN;
            int hrv_count = 0;
            Double hrv_sum = 0.0d;
            for (Double h : hrv) {
                if (!Double.isNaN(h)){
                    hrv_count++;
                    hrv_sum += h;
                }
            }
            if (hrv_count != 0){
                average = hrv_sum / hrv_count;
            }
            section.setHrv(average);
            section.setStress(Math.exp(-average));
        }

        //1.2、Compute Team Sentiment
        LambdaQueryWrapper<VResult> vResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        vResultLambdaQueryWrapper.eq(VResult::getMeeting_id,meetingID);
        List<VResult> vResults = vResultMapper.selectList(vResultLambdaQueryWrapper);
        for (Section section : sections) {
            Double start = section.getStarts() * 1000;
            Double end = section.getEnds() * 1000;
            Double total = 0.0d;
            int count = 0;
            for (VResult vResult : vResults) {
                if(vResult.getV_mean() != null && !Double.isNaN(vResult.getV_mean()) && vResult.getTime_ms() >= start && vResult.getTime_ms() <= end){
                    count++;
                    total+= vResult.getV_mean();
                }
            }

            Double avg_team_v = 0.0d;
            if(count != 0){
                avg_team_v = total / count;
            }else {
                avg_team_v = Double.NaN;
            }
            section.setSentiment(avg_team_v);
        }

        // 更新Section
        for (Section section : sections) {
            LambdaUpdateWrapper<Section> teamUpdateWrapper = new LambdaUpdateWrapper<>();
            teamUpdateWrapper
                    .eq(Section::getMeeting_id, meetingID)
                    .eq(Section::getUsers, LABEL_TEAM)
                    .eq(Section::getStarts, section.getStarts())
                    .set(Section::getAttention, section.getAttention())
                    .set(Section::getParticipation, TEAM_PARTICIPATION)
                    .set(Section::getSentiment, section.getSentiment())
                    .set(Section::getHrv, section.getHrv())
                    .set(Section::getStress, section.getStress());
            sectionMapper.update(null, teamUpdateWrapper);
        }

        //2、handle user
        List<String> userList = individualSyncService.findUserList(meetingID);
        for (String u : userList) {
            LambdaQueryWrapper<Section> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(Section::getMeeting_id,meetingID).eq(Section::getUsers, u);
            List<Section> userSections = sectionMapper.selectList(userQueryWrapper);
            //2.1 Compute User Attention
            for (Section userSection : userSections) {
                Double start = userSection.getStarts() * 1000;
                Double end = userSection.getEnds() * 1000;
                Double total_user_a = 0.0d;
                int count_user_a = 0;
                for (AResult aResult : aResults) {
                    Field field = AResult.class.getDeclaredField(u);
                    field.setAccessible(true);
                    Double value = (Double) field.get(aResult);
                    if(value != null && !Double.isNaN(value) && aResult.getTime_ms() >= start && aResult.getTime_ms() <= end) {
                        count_user_a++;
                        total_user_a += aResult.getA_mean();
                    }
                }

                Double avg_user_a = 0.0d;
                if(count_user_a != 0){
                    avg_user_a = 1.0 * total_user_a / count_user_a;
                }else {
                    avg_user_a = Double.NaN;
                }
                userSection.setAttention(avg_user_a);
                List<Double> hrv = CsvUtil.get_hrv(WINDOW_LENGTH, dataR, start, end);
                Integer index = Integer.valueOf(u.substring(u.length() - 2));
                userSection.setHrv(hrv.get(index));
                if(!Double.isNaN(hrv.get(index))){
                    userSection.setStress(Math.exp(-hrv.get(index)));
                }
            }
            //2.2 Compute User Sentiment
            for (Section userSection : userSections) {
                Double start = userSection.getStarts() * 1000;
                Double end = userSection.getEnds() * 1000;
                Double total_user_v = 0.0d;
                int count_user_v = 0;
                for (VResult vResult : vResults) {
                    Field field = VResult.class.getDeclaredField(u);
                    field.setAccessible(true);
                    Double value = (Double) field.get(vResult);
                    if(value != null && !Double.isNaN(value) && vResult.getTime_ms() >= start && vResult.getTime_ms() <= end) {
                        count_user_v++;
                        total_user_v += vResult.getV_mean();
                    }
                }

                Double avg_user_v = 0.0d;
                if(count_user_v != 0){
                    avg_user_v = total_user_v / count_user_v;
                }else {
                    avg_user_v = Double.NaN;
                }
                userSection.setSentiment(avg_user_v);
            }

            for (Section userSection : userSections) {
                LambdaUpdateWrapper<Section> userUpdateWrapper = new LambdaUpdateWrapper<>();
                userUpdateWrapper
                        .eq(Section::getMeeting_id, meetingID)
                        .eq(Section::getUsers, u)
                        .eq(Section::getStarts, userSection.getStarts())
                        .set(Section::getAttention, userSection.getAttention())
                        .set(Section::getSentiment, userSection.getSentiment())
                        .set(Section::getHrv, userSection.getHrv())
                        .set(Section::getStress, userSection.getStress());
                sectionMapper.update(null, userUpdateWrapper);
            }
        }

        //2.3、 User Participation
        LambdaQueryWrapper<NlpWordCount> userParticipationQueryWrapper = new LambdaQueryWrapper<>();
        userParticipationQueryWrapper.eq(NlpWordCount::getMeeting_id,meetingID);
        List<NlpWordCount> nlpWordCountList = nlpWordCountMapper.selectList(userParticipationQueryWrapper);
        Set<String> user = new HashSet<>();
        for (NlpWordCount nlpWordCount : nlpWordCountList) {
            user.add(nlpWordCount.getSpeaker());
        }
        List<String> speakerList = new ArrayList<>(user);
//        HashMap<String,List<Number>> speakerMap = new HashMap<>();
//        for (String s : user) {
//            speakerMap.put(s,new ArrayList<>());
//        }
        for (String s : speakerList) {
            String username = USER_PREFIX + s.substring(s.length() - 2);
            LambdaQueryWrapper<Section> speakerQueryWrapper = new LambdaQueryWrapper<>();
            speakerQueryWrapper.eq(Section::getMeeting_id,meetingID).eq(Section::getUsers, username);
            List<Section> speakerSections = sectionMapper.selectList(speakerQueryWrapper);
            for (Section speakerSection : speakerSections) {
                int total_count = 0;
                int s_count = 0;
                Double start = speakerSection.getStarts() * 1000;
                Double end = speakerSection.getEnds() * 1000;
                for (NlpWordCount nlpWordCount : nlpWordCountList) {
                    if(s.equals(nlpWordCount.getSpeaker()) && start <= nlpWordCount.getTime_ms() && end >= nlpWordCount.getTime_ms()){
                        s_count += nlpWordCount.getCount();
                        total_count += nlpWordCount.getCount();
                    } else if (!s.equals(nlpWordCount.getSpeaker()) && start <= nlpWordCount.getTime_ms() && end >= nlpWordCount.getTime_ms() ) {
                        total_count += nlpWordCount.getCount();
                    }
                }
                Double rate;
                if(total_count != 0){
                    rate = s_count * 1.0 / total_count;
                }else
                    rate = Double.NaN;
                List<Double> hrv = CsvUtil.get_hrv(WINDOW_LENGTH, dataR, start, end);

                LambdaUpdateWrapper<Section> userUpdateWrapper = new LambdaUpdateWrapper<>();
                userUpdateWrapper
                        .eq(Section::getMeeting_id,meetingID)
                        .eq(Section::getUsers, username)
                        .eq(Section::getStarts,speakerSection.getStarts())
                        .set(Section::getParticipation,rate);
                sectionMapper.update(null,userUpdateWrapper);
            }
        }

        System.out.println("debug");
        //3、handle moments of synchrony
        LambdaQueryWrapper<SynchronyMoment> synchronyMomentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        synchronyMomentLambdaQueryWrapper.eq(SynchronyMoment::getMeeting_id,meetingID);
        List<SynchronyMoment> synchronyMomentList = synchronyMomentMapper.selectList(synchronyMomentLambdaQueryWrapper);
        for (SynchronyMoment synchronyMoment : synchronyMomentList) {
            //注意：此处时间单位
            Double start = synchronyMoment.getStarts() * 1000;
            Double end = synchronyMoment.getEnds() * 1000;
            //3.1、 Compute Synchrony Moments Attention
            Double total_synchrony_moment_a = 0.0d;
            int count_synchrony_moment_a = 0;
            for (AResult aResult : aResults) {
                if(aResult.getA_mean() != null && !Double.isNaN(aResult.getA_mean()) && aResult.getTime_ms() >= start && aResult.getTime_ms() <= end){
                    count_synchrony_moment_a ++;
                    total_synchrony_moment_a += aResult.getA_mean();
                }
            }

            Double avg_synchrony_moment_a = 0.0d;
            if(count_synchrony_moment_a != 0){
                avg_synchrony_moment_a = total_synchrony_moment_a / count_synchrony_moment_a;
            }else {
                avg_synchrony_moment_a = Double.NaN;
            }

            //3.2、 Compute Synchrony Moments Sentiment
            Double total_synchrony_moment_v = 0.0d;
            int count_synchrony_moment_v = 0;
            for (VResult vResult : vResults) {
                if(vResult.getV_mean() != null && !Double.isNaN(vResult.getV_mean()) && vResult.getTime_ms() >= start && vResult.getTime_ms() <= end){
                    count_synchrony_moment_v++;
                    total_synchrony_moment_v += vResult.getV_mean();
                }
            }

            Double avg_synchrony_moment_v = 0.0d;
            if(count_synchrony_moment_v != 0){
                avg_synchrony_moment_v = total_synchrony_moment_v / count_synchrony_moment_v;
            }else {
                avg_synchrony_moment_v = Double.NaN;
            }
            List<Double> hrv = CsvUtil.get_hrv(WINDOW_LENGTH, dataR, start, end);
            Double average = Double.NaN;
            int hrv_count = 0;
            Double hrv_sum = 0.0d;
            for (Double h : hrv) {
                if (!Double.isNaN(h)){
                    hrv_count++;
                    hrv_sum += h;
                }
            }
            if (hrv_count != 0){
                average = hrv_sum / hrv_count;
            }
            synchronyMoment.setHrv(average);
            synchronyMoment.setStress(Math.exp(-average));
            //更新
            LambdaUpdateWrapper<SynchronyMoment> momentUpdateWrapper = new LambdaUpdateWrapper<>();
            momentUpdateWrapper.eq(SynchronyMoment::getMeeting_id, synchronyMoment.getMeeting_id())
                                .eq(SynchronyMoment::getStarts, synchronyMoment.getStarts())
                                .set(SynchronyMoment::getAttention, avg_synchrony_moment_a)
                                .set(SynchronyMoment::getSentiment, avg_synchrony_moment_v)
                                .set(SynchronyMoment::getParticipation, MOMENT_SYNCHRONY_PARTICIPATION)
                                .set(SynchronyMoment::getHrv, synchronyMoment.getHrv())
                                .set(SynchronyMoment::getStress, synchronyMoment.getStress());
            synchronyMomentMapper.update(null, momentUpdateWrapper);
        }




    }

    @Override
    public Map<String, List<SectionVO>> queryDataByMeetingId(Long meetingID) {
        LambdaQueryWrapper<Section> queryWrapper = new LambdaQueryWrapper<Section>()
                .eq(Section::getMeeting_id, meetingID);
//        List<SectionVO> collect = list(queryWrapper).stream()
//                .map(section -> {
//                    SectionVO sectionVO = new SectionVO();
//                    sectionVO.setUser(section.getUsers());
//                    sectionVO.setStarts(section.getStarts());
//                    sectionVO.setEnds(section.getEnds());
//                    sectionVO.setDescription(section.getDescription());
//                    return sectionVO;
//                }).collect(Collectors.toList());

        Map<String, List<SectionVO>> map = list(queryWrapper).stream()
                .collect(Collectors.groupingBy(
                        Section::getUsers, // 根据user进行分组
                        Collectors.mapping(section -> { // 使用mapping收集SectionVO对象
                            SectionVO sectionVO = new SectionVO();
                            sectionVO.setUser(section.getUsers());
                            sectionVO.setStarts(section.getStarts());
                            sectionVO.setEnds(section.getEnds());
                            sectionVO.setDescription(section.getDescription());
                            sectionVO.setLabel(section.getLabel());
                            sectionVO.setAttention(section.getAttention());
                            sectionVO.setSentiment(section.getSentiment());
                            sectionVO.setParticipation(section.getParticipation());
                            sectionVO.setHrv(section.getHrv());
                            sectionVO.setStress(section.getStress());
                            return sectionVO;
                        }, Collectors.toList()) // 将SectionVO对象收集到List中
                ));
        return map;
    }


}
