package com.aws.sync.service;


import com.aws.sync.entity.SynchronyMoment;
import com.aws.sync.vo.SynchronyMomentVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;

public interface SynchronyMomentService extends IService<SynchronyMoment> {
    void saveSmallest3(Long meetingID) throws IOException;

    List<SynchronyMomentVO> getSynchronyMomentVOByMeetingId(Long meetingID);
}
