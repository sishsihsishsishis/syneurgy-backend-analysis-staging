package com.aws.sync.service;

import com.aws.sync.entity.AveSync;
import com.aws.sync.vo.AveSyncVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AveSyncService extends IService<AveSync> {
    List<AveSyncVO> findAveSync(Long meetingID);
    List<AveSync> findUniverseGroupMeter(Long meetingID);
}
