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
@Table(name = "analysis_info")
@TableName("analysis_info")
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @TableId(value = "analysis_id", type = IdType.AUTO)
    private Long analysisId;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("model")
    private String model;

    @TableField("start_time")
    private Long startTime;

    @TableField("end_time")
    private Long endTime;

    public AnalysisInfo(Long meetingId, String model, Long startTime, Long endTime) {
        this.meetingId = meetingId;
        this.model = model;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
