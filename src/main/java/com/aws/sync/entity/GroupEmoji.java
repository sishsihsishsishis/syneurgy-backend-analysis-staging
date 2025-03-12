package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@Table(name = "group_emoji")
@TableName("group_emoji")
public class GroupEmoji {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emoji_id")
    @TableId(type = IdType.AUTO)
    private Long emoji_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "current")
    private Integer current;

    @Column(name = "acc_average")
    private Integer acc_average;

    @Column(name = "time_ms")
    private Long time_ms;

    public GroupEmoji(Long meeting_id, Integer current, Long time_ms) {
        this.meeting_id = meeting_id;
        this.current = current;
        this.time_ms = time_ms;
    }

    public GroupEmoji(Long meeting_id, Integer current, Long time_ms, Integer acc_average) {
        this.meeting_id = meeting_id;
        this.current = current;
        this.time_ms = time_ms;
        this.acc_average = acc_average;
    }
}
