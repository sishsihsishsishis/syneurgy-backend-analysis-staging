package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_distance")
@TableName("user_distance")
public class UserDistance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "user_distance_id")
    private Long user_distance_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "time_ms")
    private Integer time_ms;

    @Column(name = "label")
    private Integer label;

    public UserDistance(Long meeting_id, Double distance, Integer label, Integer time_ms) {
        this.meeting_id = meeting_id;
        this.distance = distance;
        this.label = label;
        this.time_ms = time_ms;
    }
}
