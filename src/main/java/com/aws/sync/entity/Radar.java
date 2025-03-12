package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@Table(name = "radar_table")
@TableName("radar_table")
@NoArgsConstructor
@AllArgsConstructor
public class Radar implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "radar_id")
    private Long radar_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "k")
    private String k;

    @Column(name = "v")
    private Double v;

    public Radar(Long meeting_id, String k, Double v) {
        this.meeting_id = meeting_id;
        this.k = k;
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Radar radar = (Radar) o;
        return Objects.equals(k, radar.k);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k);
    }
}
