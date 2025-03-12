package com.aws.sync.service.impl;



import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.SummaryTable;
import com.aws.sync.mapper.SummaryMapper;
import com.aws.sync.service.SummaryService;
import com.aws.sync.vo.SummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class SummaryServiceImpl extends ServiceImpl<SummaryMapper, SummaryTable> implements SummaryService {
    @Resource
    SummaryMapper summaryMapper;

    @Override
    public RestResult getSummaryByMeetingId(Long meetingID) {
        LambdaQueryWrapper<SummaryTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SummaryTable::getMeeting_id,meetingID).orderByAsc(SummaryTable::getSequence);
        List<SummaryTable> summaryTables = summaryMapper.selectList(lambdaQueryWrapper);
        List<SummaryVO> summaryVOList = new ArrayList<>();
        for (SummaryTable summaryTable : summaryTables) {
            SummaryVO summaryVO = new SummaryVO();
            BeanUtils.copyProperties(summaryTable,summaryVO);
            summaryVOList.add(summaryVO);
        }
        return RestResult.success().data(summaryVOList);
    }

    @Override
    public String getSummaryDataByMeetingId(Long meetingId) {
        LambdaQueryWrapper<SummaryTable> lambdaQueryWrapper = new LambdaQueryWrapper<SummaryTable>()
                .eq(SummaryTable::getMeeting_id, meetingId)
                .orderByAsc(SummaryTable::getSequence);
        List<SummaryTable> summaryTables = summaryMapper.selectList(lambdaQueryWrapper);
        StringBuilder content = new StringBuilder();
        for (SummaryTable summaryTable : summaryTables) {
            content.append(summaryTable.getSummary() + "\n");
        }
        return content.toString();
    }
}
