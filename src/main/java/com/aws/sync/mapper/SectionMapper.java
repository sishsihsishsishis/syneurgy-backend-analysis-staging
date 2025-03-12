package com.aws.sync.mapper;

import com.aws.sync.entity.Radar;
import com.aws.sync.entity.Section;
import com.aws.sync.vo.RadarVO;
import com.aws.sync.vo.SectionTeamVO;
import com.aws.sync.vo.SectionUserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SectionMapper extends BaseMapper<Section> {
    int addBatch(@Param("Section") List<Section> sections);

    List<SectionTeamVO> findTeam(Long meetingID);

    List<SectionUserVO> findUser(Long meetingID);

}