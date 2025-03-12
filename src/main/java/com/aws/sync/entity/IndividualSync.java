package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "individual_sync")
@TableName("individual_sync")
@Entity
@NoArgsConstructor
public class IndividualSync {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "individual_id")
    private Long individual_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "time_ms")
    private Double time_ms;

    @Column(name = "individual_sync")
    private Double individual_sync;

    @Column(name = "individual_distance")
    private Double individual_distance;

    @Column(name = "distance")
    private Double distance;

    public IndividualSync(Long meeting_id, String users, Double time_ms, Double individual_sync, Double individual_distance) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.time_ms = time_ms;
        this.individual_sync = individual_sync;
        this.individual_distance = individual_distance;
    }

    public IndividualSync(Long meeting_id, String users, Double time_ms, Double individual_sync, Double individual_distance,Double distance) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.time_ms = time_ms;
        this.individual_sync = individual_sync;
        this.individual_distance = individual_distance;
        this.distance = distance;
    }

}


