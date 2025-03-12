package com.aws.sync.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "pie_speaker")
public class PieSpeaker implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @TableId(type = IdType.AUTO)
        @Column(name = "speaker_id")
//        @Column(columnDefinition = "bigint")
        private Long speaker_id;

        @Column(name = "meeting_id")
//        @Column(columnDefinition = "bigint")
        private Long meeting_id;

        @Column(name = "speaker")
//        @Column(columnDefinition = "varchar(30)")
        private String speaker;

        @Column(name = "speaker_time")
//        @Column(columnDefinition = "double")
        private Double speaker_time;

        @Column(name = "speaker_time_rate")
//        @Column(columnDefinition = "double")
        private Double speaker_time_rate;

        @Column(name = "negative")
//        @Column(columnDefinition = "varchar(30)")
        private Double negative;

        @Column(name = "neutral")
//        @Column(columnDefinition = "double")
        private Double neutral;

        @Column(name = "positive")
//        @Column(columnDefinition = "double")
        private Double positive;






}
