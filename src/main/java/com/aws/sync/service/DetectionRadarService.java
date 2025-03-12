package com.aws.sync.service;


import com.aws.sync.entity.DetectionRadar;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DetectionRadarService extends IService<DetectionRadar> {

    List<DetectionVO> queryDataByMeetingId(Long meetingId);
}
