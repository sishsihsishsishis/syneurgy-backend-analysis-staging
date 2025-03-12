package com.aws.sync.service.impl;

import com.aws.sync.entity.WordRate;
import com.aws.sync.mapper.WordRateMapper;
import com.aws.sync.service.*;
import com.aws.sync.vo.WordRateVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WordRateServiceImpl extends ServiceImpl<WordRateMapper, WordRate> implements WordRateService {

    @Override
    public Map<Integer, List<WordRateVO>> queryDataByMeetingID(Long meetingID) {
        LambdaQueryWrapper<WordRate> queryWrapper = new LambdaQueryWrapper<WordRate>()
                .eq(WordRate::getMeetingId, meetingID);
        List<WordRate> wordRateList = list(queryWrapper);
        Map<Integer, List<WordRateVO>> result = wordRateList.stream()
                .collect(Collectors.groupingBy(
                        wordRate -> wordRate.getStarts(),
                        TreeMap::new,
                        Collectors.mapping(WordRateServiceImpl::convertToWordRateVO, Collectors.toList())
                ));
        return result;
    }

    // 转换WordRate为WordRateVO对象
    private static WordRateVO convertToWordRateVO(WordRate wordRate) {
        WordRateVO wordRateVO = new WordRateVO();
        wordRateVO.setMeetingId(wordRate.getMeetingId());
        wordRateVO.setName(wordRate.getName());
        wordRateVO.setRate(wordRate.getRate());
        return wordRateVO;
    }
}
