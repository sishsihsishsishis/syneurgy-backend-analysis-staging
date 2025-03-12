package com.aws.sync.mapper;

import com.aws.sync.entity.IndividualSync;
import com.aws.sync.vo.IndividualAllVO;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface IndividualSyncMapper extends BaseMapper<IndividualSync> {
    void addResultOneByOne(IndividualSync i);

    List<String> selectUserList(Long meetingID);

    List<IndividualAllVO> selectIndividualByUser(Long meetingID, String users);
}
