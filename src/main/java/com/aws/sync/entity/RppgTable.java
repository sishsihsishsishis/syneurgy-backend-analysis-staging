package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
//@Entity
@Table(name = "rppg_table")
public class RppgTable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
//    @Column(columnDefinition = "bigint(20)")
    private long meeting_id;

    @Column(name = "user_meeting_id")
//    @Column(columnDefinition = "float(30) comment 'userMeetingId'")
    private int user_meeting_id;

    @Column(name = "time_ms")
//    @Column(columnDefinition = "float(30) comment 'timeS'")
    private Float time_ms;

    @Column(name = "value")
//    @Column(columnDefinition = "float(30) comment 'value'")
    private Float value;
}
