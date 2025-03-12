package com.aws.sync.service;

import com.aws.sync.vo.csv.HeatmapVO;
import com.aws.sync.entity.Heatmap;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HeatmapService extends IService<Heatmap> {
    int insertBatch(List<Heatmap> heatmaps);

    void addOneByOne(List<Heatmap> heatmaps);

    List<HeatmapVO> findHeatmap(Long meetingID);
}
