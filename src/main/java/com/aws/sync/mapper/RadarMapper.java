package com.aws.sync.mapper;

import com.aws.sync.entity.Radar;
import com.aws.sync.vo.RadarVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RadarMapper extends BaseMapper<Radar> {
    int addBatch(@Param("Radar") List<Radar> radars);

    List<RadarVO> getKV(Long meetingID);
}
