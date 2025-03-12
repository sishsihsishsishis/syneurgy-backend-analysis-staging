package com.aws.sync.mapper;

import com.aws.sync.entity.AResult;
import com.aws.sync.entity.NlpTable;
import com.aws.sync.vo.NlpVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NlpMapper extends BaseMapper<NlpTable> {
    int addBatch(@Param("NlpTable") List<NlpTable> nlpTables);

    List<NlpVO> findNlp(Long meetingID);
}
