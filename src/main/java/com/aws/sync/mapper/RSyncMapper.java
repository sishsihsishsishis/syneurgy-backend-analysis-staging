package com.aws.sync.mapper;

import com.aws.sync.entity.Rsync;
import com.aws.sync.vo.csv.SyncR;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RSyncMapper extends BaseMapper<Rsync> {
    int addBatch(@Param("Rsync")List<Rsync> rsync);

    List<SyncR> findSync(@Param("meetingID") Long meetingID);


}
