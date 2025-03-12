package com.aws.sync.service.impl;

import com.aws.sync.entity.UserDistance;
import com.aws.sync.mapper.UserDistanceMapper;
import com.aws.sync.service.UserDistanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserDistanceServiceImpl extends ServiceImpl<UserDistanceMapper, UserDistance> implements UserDistanceService {

}
