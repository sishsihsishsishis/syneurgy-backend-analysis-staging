package com.aws.sync.service.impl;

import com.aws.sync.entity.DetectionCV;
import com.aws.sync.entity.DetectionNLP;
import com.aws.sync.mapper.DetectionNLPMapper;
import com.aws.sync.service.DetectionNLPService;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DetectionNLPServiceImpl extends ServiceImpl<DetectionNLPMapper, DetectionNLP> implements DetectionNLPService {

    @Override
    public List<DetectionVO> queryDataByMeetingId(Long meetingId) {
        LambdaQueryWrapper<DetectionNLP> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DetectionNLP::getMeetingId, meetingId)
                .orderByAsc(DetectionNLP::getStarts);
        List<DetectionNLP> detectionNLPList = list(queryWrapper);
        List<DetectionVO> detectionCvVOList = detectionNLPList.stream()
                .map(detectionNLP -> {
                    DetectionVO detectionVO = new DetectionVO();
                    BeanUtils.copyProperties(detectionNLP, detectionVO);
                    return detectionVO;
                })
                .collect(Collectors.toList());
        return detectionCvVOList;
    }
}
