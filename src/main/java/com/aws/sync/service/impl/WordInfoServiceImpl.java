package com.aws.sync.service.impl;


import com.aws.sync.entity.WordInfo;
import com.aws.sync.mapper.WordInfoMapper;
import com.aws.sync.service.WordInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WordInfoServiceImpl extends ServiceImpl<WordInfoMapper, WordInfo> implements WordInfoService {

}
