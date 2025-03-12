package com.aws.sync.mapper;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.RMean;
import com.aws.sync.entity.RResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RResultMapper extends BaseMapper<RResult>{
    int addBatch(@Param("RResult") List<RResult> rResults);

    List<RMean> findMean(@Param("meetingID")Long meetingID);

    List<AllUser> findUser(@Param("meetingID")Long meetingID);

    Integer addResultOneByOne(RResult r);
}
