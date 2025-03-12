package com.aws.sync.service.impl;

import com.aws.sync.entity.AResult;
import com.aws.sync.entity.DetectionCV;
import com.aws.sync.mapper.AResultMapper;
import com.aws.sync.mapper.DetectionCVMapper;
import com.aws.sync.service.DetectionCVService;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetectionCVServiceImpl extends ServiceImpl<DetectionCVMapper, DetectionCV> implements DetectionCVService {
    @Autowired
    DetectionCVMapper detectionCVMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Override
    public List<DetectionVO> queryDataByMeetingIdAndType(Long meetingId, int type) {
        LambdaQueryWrapper<DetectionCV> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DetectionCV::getMeetingId, meetingId)
                .eq(DetectionCV::getTypes, type);
        List<DetectionCV> detectionCVList = list(queryWrapper);
        List<DetectionVO> detectionCvVOList = detectionCVList.stream()
                .map(detectionCV -> {
                    DetectionVO detectionCvVO = new DetectionVO();
                    BeanUtils.copyProperties(detectionCV, detectionCvVO);
                    return detectionCvVO;
                })
                .collect(Collectors.toList());
        return detectionCvVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDetectionOneByOne(List<DetectionCV> detectionCVList) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        DetectionCVMapper dc = session.getMapper(DetectionCVMapper.class);
        for(DetectionCV a : detectionCVList){
            dc.addDetectionOneByOne(a);
        }
        session.commit();
        session.close();
    }

}
