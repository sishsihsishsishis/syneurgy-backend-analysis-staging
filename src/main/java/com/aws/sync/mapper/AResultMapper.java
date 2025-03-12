package com.aws.sync.mapper;

import com.aws.sync.vo.csv.AMean;
import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.entity.AResult;

import com.aws.sync.vo.DataAVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AResultMapper extends BaseMapper<AResult> {
    int addBatch(@Param("AResult")List<AResult> aResults);

    Integer addResultOneByOne(AResult aResult);

    List<AMean> findMean(@Param("meetingID")Long meetingID);

    List<AllUser> findUser(@Param("meetingID")Long meetingID);


    List<Long> findTimeline(Long meetingID);

    List<DataAVO> findData(Long meetingID);
}
