package com.aws.sync.mapper;

import com.aws.sync.entity.match.Speaker;
import com.aws.sync.entity.match.SpeakerUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpeakerMapper extends BaseMapper<Speaker> {
}