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
@Table(name = "nlp_word_count")
public class NlpWordCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nlp_word_count_id")
    @TableId(type = IdType.AUTO)
    private Long nlp_word_count_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "speaker")
    private String speaker;

    @Column(name = "time_ms")
    private int time_ms;

    @Column(name = "count")
    private int count;

    public NlpWordCount(Long meeting_id, String speaker, int time_ms, int count) {
        this.meeting_id = meeting_id;
        this.speaker = speaker;
        this.time_ms = time_ms;
        this.count = count;
    }
}
