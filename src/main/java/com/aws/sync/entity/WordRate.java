package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "word_rate")
@TableName("word_rate")
public class WordRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("name")
    private String name;

    @TableField("starts")
    private Integer starts;

    @TableField("rate")
    private Double rate;

    public WordRate(Long meetingId, String name, Integer starts, Double rate) {
        this.meetingId = meetingId;
        this.name = name;
        this.starts = starts;
        this.rate = rate;
    }
}
