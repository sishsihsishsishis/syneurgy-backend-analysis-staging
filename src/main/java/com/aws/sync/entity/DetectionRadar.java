package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "detection_radar")
@TableName("detection_radar")
public class DetectionRadar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("users")
    private String users;

    @TableField("starts")
    private double starts;

    @TableField("ends")
    private double ends;

    @TableField("frequency_map")
    private String frequencyMap;

    @TableField("unknown")
    private String unknown;

    @TableField("substances")
    private String substances;

    @TableField("similarities")
    private String similarities;

    @TableField("keyword")
    private String keyword;

    public DetectionRadar(Long meetingId, String users, double starts, double ends, String keyword) {
        this.meetingId = meetingId;
        this.users = users;
        this.starts = starts;
        this.ends = ends;
        this.keyword = keyword;
    }

    public DetectionRadar(Long meetingId, String users, double starts, double ends, String frequencyMap, String unknown, String substances, String similarities, String keyword) {
        this.meetingId = meetingId;
        this.users = users;
        this.starts = starts;
        this.ends = ends;
        this.frequencyMap = frequencyMap;
        this.unknown = unknown;
        this.substances = substances;
        this.similarities = similarities;
        this.keyword = keyword;
    }
}
