package com.aws.sync.service;

import com.aws.sync.vo.csv.SyncR;
import com.aws.sync.entity.Rsync;
import com.baomidou.mybatisplus.extension.service.IService;


import java.util.List;

public interface RSyncService extends IService<Rsync> {
    int insertR (List<Rsync> rsync);

    List<SyncR> findSync(Long meetingID);


}
