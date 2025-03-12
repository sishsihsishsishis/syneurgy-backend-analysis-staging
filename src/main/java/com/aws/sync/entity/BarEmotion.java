package com.aws.sync.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "bar_emotion")
public class BarEmotion implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @TableId(type = IdType.AUTO)
//        @Column(columnDefinition = "bigint")
        @Column(name = "emotion_id")
        private Long emotion_id;

        @Column(name = "meeting_id")
//        @Column(columnDefinition = "bigint")
        private Long meeting_id;

        @Column(name = "users")
//        @Column(columnDefinition = "varchar(30)")
        private String users;

//        @Column(columnDefinition = "varchar(30)")
//        private String negative;
//
//        @Column(columnDefinition = "varchar(30)")
//        private String neutral;
//
//        @Column(columnDefinition = "varchar(30)")
//        private String positive;

        @Column(name = "emotion")
//        @Column(columnDefinition = "varchar(30)")
        private String emotion;

        @Column(name = "score")
//        @Column(columnDefinition = "double")
        private Double score;







}
