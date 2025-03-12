package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "v_sync_table")
@TableName("v_sync_table")
public class Vsync implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "sync_id")
//    @Column(columnDefinition = "bigint")
    private Long sync_id;

    @Column(name = "meeting_id")
//    @Column(columnDefinition = "bigint")
    private Long meeting_id;

    @Column(name = "start_time")
//    @Column(columnDefinition = "bigint comment 'start_time'")
    private Long start_time;

    @Column(name = "v_sync")
//    @Column(columnDefinition = "double comment 'v_sync'")
    private Double v_sync;

}
