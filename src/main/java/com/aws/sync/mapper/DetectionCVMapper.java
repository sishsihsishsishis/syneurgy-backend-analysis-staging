package com.aws.sync.mapper;

import com.aws.sync.entity.DetectionCV;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetectionCVMapper extends BaseMapper<DetectionCV> {
    Integer addDetectionOneByOne(DetectionCV detectionCV);
}
