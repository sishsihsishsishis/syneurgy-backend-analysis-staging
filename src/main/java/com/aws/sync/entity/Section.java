package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "section_table")
@TableName("section_table")
@NoArgsConstructor
@AllArgsConstructor
public class Section implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "section_id")
    private Long section_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "starts")
    private Double starts;

    @Column(name = "ends")
    private Double ends;

    @Column(name = "label")
    private int label;

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

    @Column(name = "description")
    private String description;

    public Section(Long meeting_id, Double starts, Double ends, int label, String users) {
        this.meeting_id = meeting_id;
        this.starts = starts;
        this.ends = ends;
        this.label = label;
        this.users = users;
    }

    public Section(Long meeting_id, Double starts, Double ends, String users, String description) {
        this.meeting_id = meeting_id;
        this.starts = starts;
        this.ends = ends;
        this.users = users;
        this.description = description;
    }
}
