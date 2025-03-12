package com.aws.sync.service.impl;

import com.aws.sync.entity.ScoreParameter;
import com.aws.sync.mapper.ScoreMapper;
import com.aws.sync.service.ScoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, ScoreParameter> implements ScoreService {

}
