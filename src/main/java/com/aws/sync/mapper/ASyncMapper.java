package com.aws.sync.mapper;

import com.aws.sync.vo.csv.SyncA;
import com.aws.sync.entity.Async;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ASyncMapper extends BaseMapper<Async> {
    int addBatch(@Param("Async")List<Async> async);

    List<SyncA> findSync(@Param("meetingID") Long meetingID);


}
