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
@Table(name = "detection_cv")
@TableName("detection_cv")
public class DetectionCV {
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

    //0 emotion 1 posture
    @TableField("types")
    private int types;

    @TableField("keyword")
    private String keyword;

    public DetectionCV(Long meetingId, String users, double starts, double ends, int types, String keyword) {
        this.meetingId = meetingId;
        this.users = users;
        this.starts = starts;
        this.ends = ends;
        this.types = types;
        this.keyword = keyword;
    }
}
