package com.aws.sync.mapper;

import com.aws.sync.vo.csv.Score;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.vo.VideoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface MeetingMapper extends BaseMapper<MeetingTable> {
    List<VideoVO> findVideo();

    VideoVO findHandle(Long meetingID);

    int updateHandle(Long meetingID);

    int updateScore(Score score);

    Score getScore(Long meetingID);

    String findFileName(Long meetingID);

    void updateTime(Long meetingID, Long date);

    Long savaMeeting(MeetingTable meetingTable);

    // 使用注解方式
    @Select("SELECT DISTINCT team_id FROM meeting_table WHERE team_id is not null order by team_id DESC")
    Page<Long> selectDistinctTeamIds(Page<?> page);
}
