package com.aws.sync.service;


import com.aws.sync.entity.DetectionNLP;
import com.aws.sync.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DetectionNLPService extends IService<DetectionNLP> {

    List<DetectionVO> queryDataByMeetingId(Long meetingId);
}
