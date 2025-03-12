package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.SyncA;
import com.aws.sync.entity.Async;
import com.aws.sync.mapper.ASyncMapper;
import com.aws.sync.service.ASyncService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ASyncServiceImpl extends ServiceImpl<ASyncMapper, Async> implements ASyncService {
    @Resource
    ASyncMapper aSyncMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertA(List<Async> async) {
        return aSyncMapper.addBatch(async);
    }

    @Override
    public List<SyncA> findSync(Long meetingID) {
        return aSyncMapper.findSync(meetingID);
    }
}
