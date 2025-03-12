package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "user_contribution")
@TableName("user_contribution")
@Entity
@NoArgsConstructor
public class UserContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "user_contribution_id")
    private Long user_contribution_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "rate")
    private Double rate;

    public UserContribution(Long meeting_id, String users, Double rate) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.rate = rate;
    }
}


