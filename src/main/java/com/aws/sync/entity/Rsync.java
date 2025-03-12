package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "r_sync_table")
@TableName("r_sync_table")
public class Rsync implements Serializable {
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

    @Column(name = "r_sync")
//    @Column(columnDefinition = "double comment 'r_sync'")
    private Double r_sync;

}
