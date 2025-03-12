package com.aws.sync.service.impl;

import com.aws.sync.entity.AveSync;
import com.aws.sync.mapper.AveSyncMapper;
import com.aws.sync.service.AveSyncService;
import com.aws.sync.vo.AveSyncVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AveSyncServiceImpl extends ServiceImpl<AveSyncMapper, AveSync> implements AveSyncService {
    @Resource
    AveSyncMapper aveSyncMapper;

    @Override
    public List<AveSyncVO> findAveSync(Long meetingID) {
        return aveSyncMapper.selectAveSync(meetingID);
    }

    @Override
    public List<AveSync> findUniverseGroupMeter(Long meetingID) {
        LambdaQueryWrapper<AveSync> universeGroupMeterQueryWrapper = new LambdaQueryWrapper<>();
        universeGroupMeterQueryWrapper.eq(AveSync::getMeeting_id, meetingID)
                .orderByAsc(AveSync::getTime_ms);
        List<AveSync> aveSyncs = aveSyncMapper.selectList(universeGroupMeterQueryWrapper);
        return aveSyncs;
    }
}
