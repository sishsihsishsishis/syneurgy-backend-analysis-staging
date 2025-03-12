package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "synchrony_moment")
@TableName("synchrony_moment")
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SynchronyMoment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "synchrony_moment_id")
    private Long synchrony_moment_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "starts")
    private Double starts;

    @Column(name = "ends")
    private Double ends;

    @Column(name = "label")
    private Integer label;

    @Column(name = "sentence")
    private String sentence;

    @Column(name = "attention")
    private Double attention;

    @Column(name = "sentiment ")
    private Double sentiment ;

    @Column(name = "participation")
    private Double participation;

    @Column(name = "hrv")
    private Double hrv;

    @Column(name = "stress")
    private Double stress;

    public SynchronyMoment(Long meeting_id, Double starts, Double ends, Integer label, String sentence) {
        this.meeting_id = meeting_id;
        this.starts = starts;
        this.ends = ends;
        this.label = label;
        this.sentence = sentence;
    }
}
