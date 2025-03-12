package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.SyncV;
import com.aws.sync.entity.Vsync;
import com.aws.sync.mapper.VSyncMapper;
import com.aws.sync.service.VSyncService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VSyncServiceImpl extends ServiceImpl<VSyncMapper, Vsync> implements VSyncService {
    @Resource
    VSyncMapper vSyncMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertV(List<Vsync> vsync) {
        return vSyncMapper.addBatch(vsync);
    }

    @Override
    public List<SyncV> findSync(Long meetingID) {
        return vSyncMapper.findSync(meetingID);
    }
}
