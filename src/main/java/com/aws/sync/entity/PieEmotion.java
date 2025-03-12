package com.aws.sync.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "pie_emotion")
public class PieEmotion implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @TableId(type = IdType.AUTO)
        @Column(name = "emotion_id")
//        @Column(columnDefinition = "bigint")
        private Long emotion_id;

        @Column(name = "meeting_id")
//        @Column(columnDefinition = "bigint")
        private Long meeting_id;

        @Column(name = "emotion")
//        @Column(columnDefinition = "varchar(30)")
        private String emotion;

        @Column(name = "emotion_time")
//        @Column(columnDefinition = "double")
        private Double emotion_time;

        @Column(name = "emotion_time_rate")
//        @Column(columnDefinition = "double")
        private Double emotion_time_rate;





}
