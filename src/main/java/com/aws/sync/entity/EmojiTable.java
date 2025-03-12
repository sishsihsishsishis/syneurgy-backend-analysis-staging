package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "emoji_table")
public class EmojiTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emoji_id")
    @TableId(type = IdType.AUTO)
    private Long emoji_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "emoji")
    private Integer emoji;

    @Column(name = "time_ms")
    private Long time_ms;

    public EmojiTable(Long meeting_id, String users, Integer emoji, Long time_ms) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.emoji = emoji;
        this.time_ms = time_ms;
    }
}
