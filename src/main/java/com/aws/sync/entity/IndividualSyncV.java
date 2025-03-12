package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "individual_sync_v")
@TableName("individual_sync_v")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class IndividualSyncV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "individual_v_id")
    private Long individual_v_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "time_ms")
    private Double time_ms;

    @Column(name = "users")
    private String users;

    @Column(name = "individual_sync")
    private Double individual_sync;

    @Column(name = "individual_distance")
    private Double individual_distance;

    @Column(name = "individual_rate")
    private Double individual_rate;

    @Column(name = "individual_score")
    private Double individual_score;

    public IndividualSyncV(Long meeting_id,Double time_ms, String users, Double individual_sync, Double individual_distance, Double individual_rate) {
        this.meeting_id = meeting_id;
        this.time_ms = time_ms;
        this.users = users;
        this.individual_sync = individual_sync;
        this.individual_distance = individual_distance;
        this.individual_rate = individual_rate;
    }

    public IndividualSyncV(Long meeting_id,Double time_ms, String users, Double individual_sync, Double individual_distance, Double individual_rate, Double individual_score) {
        this.meeting_id = meeting_id;
        this.time_ms = time_ms;
        this.users = users;
        this.individual_sync = individual_sync;
        this.individual_distance = individual_distance;
        this.individual_rate = individual_rate;
        this.individual_score = individual_score;
    }
}
