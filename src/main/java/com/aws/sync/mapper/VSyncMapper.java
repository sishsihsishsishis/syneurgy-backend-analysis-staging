package com.aws.sync.mapper;

import com.aws.sync.vo.csv.SyncV;
import com.aws.sync.entity.Vsync;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VSyncMapper extends BaseMapper<Vsync> {
    int addBatch(@Param("Vsync")List<Vsync> vsync);

    List<SyncV> findSync(@Param("meetingID") Long meetingID);
}
