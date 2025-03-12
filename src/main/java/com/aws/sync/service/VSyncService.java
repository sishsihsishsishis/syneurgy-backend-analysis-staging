package com.aws.sync.service;

import com.aws.sync.vo.csv.SyncV;
import com.aws.sync.entity.Vsync;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VSyncService extends IService<Vsync> {
    int insertV(List<Vsync> vsync);

    List<SyncV> findSync(Long meetingID);

}
