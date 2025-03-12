package com.aws.sync.service;

import com.aws.sync.entity.HighlightStatement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface HighlightStatementService extends IService<HighlightStatement> {

    List<HighlightStatement> getHighlightStatementByTag(int i);

}
