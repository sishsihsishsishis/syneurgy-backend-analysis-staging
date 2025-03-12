package com.aws.sync.vo;

import lombok.Data;
@Data
public class EmailSendVO {
    private Long team_id;
    private Long meeting_id;
    private Integer is_handle;
    private Integer email_send;
}
