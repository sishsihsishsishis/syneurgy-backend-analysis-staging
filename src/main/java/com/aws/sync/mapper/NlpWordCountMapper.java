package com.aws.sync.mapper;

import com.aws.sync.entity.NlpWordCount;
import com.aws.sync.entity.match.CVUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NlpWordCountMapper extends BaseMapper<NlpWordCount> {
    void addOneByOne(NlpWordCount nlpWordCount);
}