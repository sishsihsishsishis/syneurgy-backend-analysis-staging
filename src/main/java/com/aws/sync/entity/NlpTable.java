package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;


@Data
@Entity
@Table(name = "nlp_table")
public class NlpTable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nlp_id")
    @TableId(type = IdType.AUTO)
    private Long nlp_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "speaker")
    private String speaker;

    @Column(name = "starts")
    private Double starts;

    @Column(name = "ends")
    private Double ends;

    @Column(name = "sentence",columnDefinition = "varchar(1000)")
    private String sentence;

    @Column(name = "emotion")
    private String emotion;

    @Column(name = "dialogue")
    private String dialogue;

}
