package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "team_table")
public class TeamTable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
//    @Column(columnDefinition = "bigint")
    @TableId(type = IdType.AUTO)
    private Long team_id;

    @Column(name = "team_name")
//    @Column(columnDefinition = "varchar(30) comment 'teamName'")
    private String teamName;

    @Column(name = "team_password")
//    @Column(columnDefinition = "varchar(30) comment 'teamPassword'")
    private String teamPassword;

}
