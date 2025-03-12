package com.aws.sync.service;


import com.aws.sync.dto.TimeSearchDTO;

import java.util.HashMap;

public interface TeamService {

    HashMap<String, Long> queryTimeInfo(String teamId, Long timestamp);

    Long queryVideoTime(TimeSearchDTO timeSearchDTO);
}
