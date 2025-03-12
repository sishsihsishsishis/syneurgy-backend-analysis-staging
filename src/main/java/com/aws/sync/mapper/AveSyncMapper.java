package com.aws.sync.mapper;

import com.aws.sync.entity.AveSync;
import com.aws.sync.vo.AveSyncVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface AveSyncMapper extends BaseMapper<AveSync> {
    List<AveSyncVO> selectAveSync(Long meetingID);
}
