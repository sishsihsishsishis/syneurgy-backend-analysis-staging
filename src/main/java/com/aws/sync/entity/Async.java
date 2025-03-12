package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "a_sync_table")
@TableName("a_sync_table")
public class Async implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "sync_id")
    private Long sync_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "start_time")
    private Long start_time;

    @Column(name = "a_sync")
    private Double a_sync;

}
