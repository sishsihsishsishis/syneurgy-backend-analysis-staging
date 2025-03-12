package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;


@Data
@Entity
@NoArgsConstructor
@Table(name = "meeting_table")
public class MeetingTable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "meeting_id")
    @TableId(type = IdType.AUTO)
    private Long meeting_id;

    @Column(name = "team_id")
    private Long team_id;

    @Column(name = "upload_time")
    private Long upload_time;

    @Column(name = "meeting_start_time")
    private Long meeting_start_time;

    @Column(name = "meeting_name")
    private String meeting_name;

    @Column(name = "is_update", columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_update;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "rppg_sampling_rate")
    private Integer rppg_sampling_rate;

    @Column(name = "va_sampling_rate")
    private Integer va_sampling_rate;

    @Column(name = "is_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_handle;

    @Column(name = "video_url")
    private String video_url;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "img_url",columnDefinition = "varchar(1000)")
    private String img_url;

    @Column(name = "body_score")
    private Double body_score;

    @Column(name = "brain_score")
    private Double brain_score;

    @Column(name = "behavior_score")
    private Double behavior_score;

    @Column(name = "total_score")
    private Double total_score;

    @Column(name = "threshold", columnDefinition = "INTEGER DEFAULT 0")
    private Double threshold;

    @Column(name = "cv_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer cv_handle;

    @Column(name = "nlp_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer nlp_handle;

    @Column(name = "email_send",columnDefinition = "INTEGER DEFAULT 0")
    private Integer email_send;

    @Column(name = "video_create_time")
    private Long video_create_time;

    @Column(name = "meeting_type",columnDefinition = "varchar DEFAULT 'Creative'")
    private String meeting_type;

    @Column(name = "nlp_file",columnDefinition = "INTEGER DEFAULT 0")
    private Integer nlp_file;

    @Column(name = "is_match",columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_match;

    @Column(name = "team_distance")
    private Double team_distance;

    @Column(name = "is_merge",columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_merge;

    @Column(name = "a_positive_rate")
    private Double a_positive_rate;

    @Column(name = "v_positive_rate")
    private Double v_positive_rate;

    @Column(name = "v_negative_rate")
    private Double v_negative_rate;

    @Column(name = "a_negative_rate")
    private Double a_negative_rate;

    @Column(name = "hrv")
    private Double hrv;

    @Column(name = "nlp_speaker_time")
    private Double nlp_speaker_time;

    @Column(name = "nlp_equal_participation")
    private Double nlp_equal_participation;

    @Column(name = "synchrony_moment_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer synchrony_moment_handle;

    @Column(name = "gpt_report_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer gpt_report_handle;

    public MeetingTable(Long team_id, Long upload_time,
                        Integer is_update, Integer is_handle,
                        String video_url, String thumbnail,
                        String meeting_name, Long video_create_time,
                        Long meeting_start_time, String meeting_type) {
        this.team_id = team_id;
        this.upload_time = upload_time;
        this.is_update = is_update;
        this.is_handle = is_handle;
        this.video_url = video_url;
        this.meeting_name = meeting_name;
        this.thumbnail = thumbnail;
        this.video_create_time = video_create_time;
        this.meeting_start_time = meeting_start_time;
        this.meeting_type = meeting_type;
    }

    public MeetingTable(Long upload_time, String meeting_name, String video_url, String meeting_type) {
        this.upload_time = upload_time;
        this.meeting_name = meeting_name;
        this.video_url = video_url;
        this.meeting_type = meeting_type;
    }
}
