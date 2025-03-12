package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.HeatmapVO;
import com.aws.sync.entity.Heatmap;
import com.aws.sync.mapper.HeatmapMapper;
import com.aws.sync.service.HeatmapService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HeatmapServiceImpl extends ServiceImpl<HeatmapMapper, Heatmap> implements HeatmapService {
    @Resource
    HeatmapMapper heatmapMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatch(List<Heatmap> heatmaps) {
        return heatmapMapper.addBatch(heatmaps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(List<Heatmap> heatmaps) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        HeatmapMapper hm = session.getMapper(HeatmapMapper.class);
        for(Heatmap h : heatmaps){
            hm.addResultOneByOne(h);
        }
        session.commit();
        session.close();
    }

    @Override
    public List<HeatmapVO> findHeatmap(Long meetingID) {
        return heatmapMapper.findHeatmap(meetingID);
    }
}
