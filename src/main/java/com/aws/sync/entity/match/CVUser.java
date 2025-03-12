package com.aws.sync.entity.match;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "user_table")
@TableName("user_table")
public class CVUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "user_name")
    private String user_name;

//    @Column(name = "login_user")
//    private String login_user;

    @Column(name = "user_id")
    private String user_id;

//    @Column(name = "merge_id")
//    private Integer merge_id;
//    @Column(name = "email_send", columnDefinition = "INTEGER DEFAULT 0")
//    private Integer email_send;
//
    @Column(name = "is_match", columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_match;

    @Column(name = "create_date")
    private Long create_date;

    @Column(name = "team_id")
    private Long team_id;

    //若可以修改username则保存原始数据
//    @Column(name = "origin_name")
//    private String origin_name;
    public CVUser(Long meeting_id, String user_name) {
        this.meeting_id = meeting_id;
        this.user_name = user_name;
    }

}
