package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@Table(name = "gpt_summary")
@TableName("gpt_summary")
public class GptSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("team_id")
    private Long team_id;

    @TableField("k")
    private String k;

    @TableField("v")
    private String v;

    @TableField("label")
    private int label;

    public GptSummary(Long meetingId, Long team_id, String k, String v, int label) {
        this.meetingId = meetingId;
        this.team_id = team_id;
        this.k = k;
        this.v = v;
        this.label = label;
    }
}
