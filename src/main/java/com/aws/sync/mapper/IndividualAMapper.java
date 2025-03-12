package com.aws.sync.mapper;

import com.aws.sync.entity.IndividualSyncA;
import com.aws.sync.vo.IndividualVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


public interface IndividualAMapper extends BaseMapper<IndividualSyncA> {
    List<IndividualVO> findIndividual(Long meetingID);

    List<Double> findTimeLine(Long meetingID);

    List<String> findUsers(Long meetingID);

    List<IndividualVO> findByUser(Long meetingID, String userName);
}
