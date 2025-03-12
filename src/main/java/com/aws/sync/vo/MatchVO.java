package com.aws.sync.vo;


import lombok.Data;

import java.util.List;

@Data
public class MatchVO {
    private Long meeting_id;
    private Long team_id;
    private String meeting_name;
    private Integer is_handle;
    private Integer cv_handle;
    private Integer nlp_handle;
    private Integer is_match;
    List<String> users;
    List<String> speakers;
}
