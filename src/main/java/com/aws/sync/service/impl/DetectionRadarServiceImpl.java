package com.aws.sync.service.impl;

import com.aws.sync.entity.DetectionNLP;
import com.aws.sync.entity.DetectionRadar;
import com.aws.sync.mapper.DetectionNLPMapper;
import com.aws.sync.mapper.DetectionRadarMapper;
import com.aws.sync.service.DetectionNLPService;
import com.aws.sync.service.DetectionRadarService;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DetectionRadarServiceImpl extends ServiceImpl<DetectionRadarMapper, DetectionRadar> implements DetectionRadarService {

    @Override
    public List<DetectionVO> queryDataByMeetingId(Long meetingId) {
        LambdaQueryWrapper<DetectionRadar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DetectionRadar::getMeetingId, meetingId)
                .orderByAsc(DetectionRadar::getStarts);
        List<DetectionRadar> detectionRadarList = list(queryWrapper);
        List<DetectionVO> detectionCvVOList = detectionRadarList.stream()
                .map(detectionRadar -> {
                    DetectionVO detectionVO = new DetectionVO();
                    BeanUtils.copyProperties(detectionRadar, detectionVO);
                    return detectionVO;
                })
                .collect(Collectors.toList());
        return detectionCvVOList;
    }
}
