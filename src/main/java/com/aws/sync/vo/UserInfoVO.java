package com.aws.sync.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long user_id;
    private Long meeting_id;
    private String user_name;
    private String login_user;
    private String login_username;
    private Long create_date;
    private Long team_id;
}
