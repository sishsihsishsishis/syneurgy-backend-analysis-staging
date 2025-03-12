package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@Table(name = "nlp_summary")
@TableName("nlp_summary")
public class NlpSummary implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nlp_summary_id")
    @TableId(type = IdType.AUTO)
    private Long nlp_summary_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "speakers")
    private String speakers;

    @Column(name = "starts")
    private double starts;

    @Column(name = "ends")
    private double ends;

    @Column(name = "type")
    private String type;

    @Column(name = "subtype")
    private String subtype;
}
