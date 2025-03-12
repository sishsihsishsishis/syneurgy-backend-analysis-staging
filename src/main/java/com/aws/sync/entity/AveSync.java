package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ave_sync")
public class AveSync {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "ave_sync_id")
    private Long ave_sync_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "time_ms")
    private Double time_ms;

    @Column(name = "ave")
    private Double ave;

    @Column(name = "current")
    private Double current;

    @Column(name = "acc_average")
    private Double acc_average;

    public AveSync(Long meeting_id, Double time_ms, Double ave) {
        this.meeting_id = meeting_id;
        this.time_ms = time_ms;
        this.ave = ave;
    }

    public AveSync() {
    }

    public AveSync(Double time_ms, Double ave) {
        this.time_ms = time_ms;
        this.ave = ave;
    }
}
