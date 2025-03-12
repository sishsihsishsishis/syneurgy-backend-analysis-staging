package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.entity.Radar;
import com.aws.sync.entity.Section;
import com.aws.sync.mapper.MeetingMapper;
import com.aws.sync.mapper.NlpMapper;
import com.aws.sync.mapper.RadarMapper;
import com.aws.sync.service.NlpService;
import com.aws.sync.service.RadarService;
import com.aws.sync.vo.MeetingSummaryVO;
import com.aws.sync.vo.NlpVO;
import com.aws.sync.vo.PieEmotionVO;
import com.aws.sync.vo.RadarVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.aws.sync.constants.CsvConstants.DATA_HANDLE;

@Service
public class RadarServiceImpl extends ServiceImpl<RadarMapper, Radar> implements RadarService {
    private final String ENGAGEMENT_VALUE = "Absorption or Task Engagement";
    @Resource
    RadarMapper radarMapper;

    @Resource
    MeetingMapper meetingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRadar(List<Radar> radars) {
        return radarMapper.addBatch(radars);
    }

    @Override
    public List<RadarVO> findKV(Long meetingID) {
        List<RadarVO> kv = radarMapper.getKV(meetingID);
        return kv.stream()
                .collect(Collectors.toMap(
                        RadarVO::getK, // 以k作为Map的key
                        vo -> vo,      // 以RadarVO作为Map的value
                        (existing, replacement) -> existing // 如果有重复的key，保留第一个出现的value
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public RestResult getLatestFiveRadar(Long teamID) {
        LambdaQueryWrapper<MeetingTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeetingTable::getTeam_id,teamID)
                .eq(MeetingTable::getIs_handle, DATA_HANDLE)
                .orderByDesc(MeetingTable::getUpload_time).last("LIMIT 5");
        List<MeetingTable> latestFiveMeeting = meetingMapper.selectList(lambdaQueryWrapper);

        HashMap<String,Double> radarScore = new HashMap<>();
        radarScore.put("Equal Participation",0.0d);
        radarScore.put("Enjoyment",0.0d);
        radarScore.put("Shared Goal Commitment",0.0d);
        radarScore.put("Absorption or Task Engagement",0.0d);
        radarScore.put("Trust",0.0d);
        radarScore.put("Psychological Safety",0.0d);
        int size = latestFiveMeeting.size();
        for (MeetingTable meetingTable : latestFiveMeeting) {
            List<RadarVO> kv = radarMapper.getKV(meetingTable.getMeeting_id());
            boolean flag = false;
            for (RadarVO radarVO : kv) {
                if (radarVO.getV() == null || Double.isNaN(radarVO.getV())) {
                    flag = true;
                    size--;
                    break;
                }
            }
            if (flag) continue;
            for (RadarVO radarVO : kv) {
                radarScore.put(radarVO.getK(),radarScore.getOrDefault(radarVO.getK(),0.0d) + radarVO.getV());
            }
        }
//        BigDecimal equal = BigDecimal.valueOf(radarScore.get("Equal Participation") / latestFiveMeeting.size()).setScale(3, BigDecimal.ROUND_HALF_UP);
        size = size > 0 ? size : 1;
        radarScore.put("Equal Participation",BigDecimal.valueOf(radarScore.get("Equal Participation") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        radarScore.put("Enjoyment",BigDecimal.valueOf(radarScore.get("Enjoyment") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        radarScore.put("Shared Goal Commitment",BigDecimal.valueOf(radarScore.get("Shared Goal Commitment") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        radarScore.put("Absorption or Task Engagement",BigDecimal.valueOf(radarScore.get("Absorption or Task Engagement") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        radarScore.put("Trust",BigDecimal.valueOf(radarScore.get("Trust") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        radarScore.put("Psychological Safety",BigDecimal.valueOf(radarScore.get("Psychological Safety") / size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());

        return RestResult.success().data(radarScore);
    }

    @Override
    public void setEngagementAndAgency(List<MeetingTable> meetingTables, MeetingSummaryVO meetingSummaryVO) {
        int engagementCount = 0;
        Double engagementScore = 0.0d;
        int agencyCount = 0;
        Double agencyScore = 0.0d;

        for (MeetingTable meetingTable : meetingTables) {
            LambdaQueryWrapper<Radar> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Radar::getMeeting_id, meetingTable.getMeeting_id());
            List<Radar> radarList = radarMapper.selectList(lambdaQueryWrapper);
            for (Radar radar : radarList) {
                if(radar != null && radar.getV() != null && ENGAGEMENT_VALUE.equals(radar.getK()) && !Double.isNaN(radar.getV())){
                    engagementCount ++;
                    engagementScore += radar.getV();
                }

                if(radar != null && radar.getV() != null && !Double.isNaN(radar.getV())){
                    agencyCount ++;
                    agencyScore += radar.getV();
                }
            }
        }

        if(engagementCount != 0){
            meetingSummaryVO.setEngagement(engagementScore / engagementCount);
        }

        if(agencyCount != 0){
            meetingSummaryVO.setAgency(agencyScore / agencyCount);
        }

    }


}
