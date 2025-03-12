package com.aws.sync.mapper;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.VMean;
import com.aws.sync.entity.VResult;
import com.aws.sync.vo.DataVVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VResultMapper extends BaseMapper<VResult> {
    int addBatch(@Param("VResult") List<VResult> vResults);

    Integer addResultOneByOne(VResult v);

    List<VMean> findMean(@Param("meetingID")Long meetingID);

    List<AllUser> findUser(@Param("meetingID")Long meetingID);

    List<DataVVO> findData(Long meetingID);
}
