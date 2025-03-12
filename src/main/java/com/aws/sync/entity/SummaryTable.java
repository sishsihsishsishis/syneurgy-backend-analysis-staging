package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "summary_table")
@TableName("summary_table")
public class SummaryTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    @TableId(type = IdType.AUTO)
    private Long summary_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "sequence" )
    private Integer sequence;

    @Column(name = "summary")
    private String summary;

    public SummaryTable(Long meeting_id, Integer sequence, String summary) {
        this.meeting_id = meeting_id;
        this.sequence = sequence;
        this.summary = summary;
    }
}
