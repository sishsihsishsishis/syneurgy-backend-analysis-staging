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
@Table(name = "word_info")
@TableName("word_info")
public class WordInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("users")
    private String users;

    @TableField("speed")
    private Double speed;

    @TableField("rate")
    private Double rate;

    public WordInfo(Long meetingId, String users, Double speed, Double rate) {
        this.meetingId = meetingId;
        this.users = users;
        this.speed = speed;
        this.rate = rate;
    }

}
