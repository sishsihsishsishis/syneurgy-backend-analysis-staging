package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "heatmap")
public class Heatmap implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "heatmap_id")
    private Long heatmap_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "x")
    private int x;

    @Column(name = "y")
    private int y;

    @Column(name = "img")
    private int img;
}
