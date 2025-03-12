package com.aws.sync.service.impl;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.PosNegRate;
import com.aws.sync.mapper.MeetingMapper;
import com.aws.sync.mapper.PosAndNegRateMapper;
import com.aws.sync.service.PosAndNegRateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class PosAndNegRateServiceImpl extends ServiceImpl<PosAndNegRateMapper, PosNegRate> implements PosAndNegRateService {
    @Autowired
    PosAndNegRateMapper posAndNegRateMapper;

    @Resource
    MeetingMapper meetingMapper;

    @Override
    public RestResult getPositiveAndNegativeRateByMeetingId(Long meetingID) {
        HashMap<String,HashMap<String,Number>> ans = new HashMap<>();

        LambdaQueryWrapper<PosNegRate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PosNegRate::getMeeting_id,meetingID);
        List<PosNegRate> posNegRateList = list(lambdaQueryWrapper);

        LambdaQueryWrapper<MeetingTable> meetingTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingTableLambdaQueryWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        MeetingTable meetingTable = meetingMapper.selectOne(meetingTableLambdaQueryWrapper);

        if(meetingTable != null){
            HashMap<String,Number> total_rate = new HashMap<>();
            total_rate.put("a_positive_rate", meetingTable.getA_positive_rate());
            total_rate.put("a_negative_rate", meetingTable.getA_negative_rate());
            total_rate.put("v_positive_rate", meetingTable.getV_positive_rate());
            total_rate.put("v_negative_rate", meetingTable.getV_negative_rate());
            ans.put("total_pos_neg_rate", total_rate);
        }

        for (PosNegRate posNegRate : posNegRateList) {
            HashMap<String,Number> p_n_r = new HashMap<>();
            p_n_r.put("a_positive_rate", posNegRate.getA_positive_rate());
            p_n_r.put("a_negative_rate", posNegRate.getA_negative_rate());
            p_n_r.put("v_positive_rate", posNegRate.getV_positive_rate());
            p_n_r.put("v_negative_rate", posNegRate.getV_negative_rate());

            ans.put(posNegRate.getUsers(), p_n_r);
        }
        return RestResult.success().data(ans);
    }
}
