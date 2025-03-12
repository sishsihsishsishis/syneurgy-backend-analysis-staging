package com.aws.sync.service;

import com.aws.sync.entity.DetectionCV;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface DetectionCVService extends IService<DetectionCV> {

    void addDetectionOneByOne(List<DetectionCV> detectionCVList);

    List<DetectionVO> queryDataByMeetingIdAndType(Long meetingId, int type);
}
