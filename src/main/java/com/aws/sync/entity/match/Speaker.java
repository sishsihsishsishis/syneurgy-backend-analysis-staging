package com.aws.sync.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "speaker_table")
@TableName("speaker_table")
public class Speaker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "speaker_id")
    @TableId(type = IdType.AUTO)
    private Long speaker_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "speaker_name")
    private String speaker_name;

    public Speaker(Long meeting_id, String speaker_name) {
        this.meeting_id = meeting_id;
        this.speaker_name = speaker_name;
    }
}
