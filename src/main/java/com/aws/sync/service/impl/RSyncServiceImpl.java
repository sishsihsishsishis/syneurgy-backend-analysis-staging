package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.SyncR;
import com.aws.sync.entity.Rsync;
import com.aws.sync.mapper.RSyncMapper;
import com.aws.sync.service.RSyncService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RSyncServiceImpl extends ServiceImpl<RSyncMapper, Rsync> implements RSyncService {
    @Resource
    RSyncMapper rSyncMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertR(List<Rsync> rsync) {
        return rSyncMapper.addBatch(rsync);
    }

    @Override
    public List<SyncR> findSync(Long meetingID) {
        return rSyncMapper.findSync(meetingID);
    }
}
