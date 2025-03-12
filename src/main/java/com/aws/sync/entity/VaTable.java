package com.aws.sync.entity;


import lombok.Data;

import javax.persistence.*;

@Data
//@Entity
@Table(name = "va_table")
public class VaTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private long meetingId;

    @Column(columnDefinition = "bigint comment 'teamId'")
    private int userMeetingId;

    @Column(columnDefinition = "bigint comment 'time_ms'")
    private int time_ms;

    @Column(columnDefinition = "float comment 'valenceValue'")
    private Float valenceValue;

    @Column(columnDefinition = "float comment 'arousalValue'")
    private Float arousalValue;

}
