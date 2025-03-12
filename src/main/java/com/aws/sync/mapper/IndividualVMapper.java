package com.aws.sync.mapper;

import com.aws.sync.entity.IndividualSyncV;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface IndividualVMapper extends BaseMapper<IndividualSyncV> {
    List<Double> selectTimeLine(Long meetingID);

    List<String> selectUserList(Long meetingID);

    List<IndividualVO> selectIndividualByUser(Long meetingID, String username);
}
