package com.aws.sync.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "speaker_user")
@AllArgsConstructor
@NoArgsConstructor
public class SpeakerUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "su_id")
    private Long su_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "user_name")
    private String user_name;

    @Column(name = "speaker_name")
    private String speaker_name;

    public SpeakerUser(Long meeting_id, String user_name, String speaker_name) {
        this.meeting_id = meeting_id;
        this.user_name = user_name;
        this.speaker_name = speaker_name;
    }
}
