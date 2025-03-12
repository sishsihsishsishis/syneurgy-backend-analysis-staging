package com.aws.sync.mapper;

import com.aws.sync.vo.csv.HeatmapVO;
import com.aws.sync.entity.Heatmap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HeatmapMapper extends BaseMapper<Heatmap> {
    int addBatch(@Param("Heatmap") List<Heatmap> heatmaps);

    List<HeatmapVO> findHeatmap(Long meetingID);

    Integer addResultOneByOne(Heatmap h);
}
