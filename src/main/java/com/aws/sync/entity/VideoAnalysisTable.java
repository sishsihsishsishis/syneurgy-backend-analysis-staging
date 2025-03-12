package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "video_analysis_table")
@TableName("video_analysis_table")
public class VideoAnalysisTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_analysis_id")
    @TableId(type = IdType.AUTO)
    private Long video_analysis_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "a_status")
    private String a_status;

    @Column(name = "v_status")
    private String v_status;

    @Column(name = "rppg_status")
    private String rppg_status;

    @Column(name = "cv_status")
    private String cv_status;

    @Column(name = "nlp_status")
    private String nlp_status;

    @Column(name = "anchor_status")
    private String anchor_status;

    @Column(name = "active_speaker_status")
    private String active_speaker_status;

    @Column(name = "emotion_detection")
    private String emotion_detection;

    @Column(name = "posture_detection")
    private String posture_detection;

    @Column(name = "blink_results")
    private String blink_results;

    @Column(name = "detectionCV0")
    private String detectionCV0;

    @Column(name = "detectionCV1")
    private String detectionCV1;

    @Column(name = "nlp_and_match")
    private String nlp_and_match;

    public VideoAnalysisTable(Long meeting_id) {
        this.meeting_id = meeting_id;
    }
}