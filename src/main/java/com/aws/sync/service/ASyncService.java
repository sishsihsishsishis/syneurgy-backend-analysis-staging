package com.aws.sync.service;

import com.aws.sync.vo.csv.SyncA;
import com.aws.sync.entity.Async;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ASyncService extends IService<Async> {
    int insertA(List<Async> async);

    List<SyncA> findSync(Long meetingID);

}
