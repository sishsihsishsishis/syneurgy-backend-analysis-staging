package com.aws.sync.service.impl;



import com.aws.sync.entity.HighlightStatement;
import com.aws.sync.mapper.HighlightStatementMapper;
import com.aws.sync.service.HighlightStatementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HighlightStatementServiceImpl extends ServiceImpl<HighlightStatementMapper, HighlightStatement> implements HighlightStatementService {
    @Autowired
    HighlightStatementMapper highlightStatementMapper;

    @Override
    public List<HighlightStatement> getHighlightStatementByTag(int i) {
        LambdaQueryWrapper<HighlightStatement> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(HighlightStatement::getTag,i);
        return highlightStatementMapper.selectList(lambdaQueryWrapper);
    }

}
