package com.aws.sync.service;

import com.aws.sync.entity.Section;
import com.aws.sync.vo.SectionTeamVO;
import com.aws.sync.vo.SectionUserVO;
import com.aws.sync.vo.SectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SectionService extends IService<Section> {
    int insertSection(List<Section> sections);

    List<SectionTeamVO> findTeam(Long meetingID);

    List<SectionUserVO> findUser(Long meetingID);

    void addAdditionInfoToSection(Long meetingID) throws NoSuchFieldException, IllegalAccessException, IOException;

    Map<String, List<SectionVO>> queryDataByMeetingId(Long meetingID);
}
