package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "pos_neg_rate")
@TableName("pos_neg_rate")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PosNegRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "pos_neg_rate_id")
    private Long pos_neg_rate_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "a_positive_rate")
    private Double a_positive_rate;

    @Column(name = "a_negative_rate")
    private Double a_negative_rate;

    @Column(name = "v_positive_rate")
    private Double v_positive_rate;

    @Column(name = "v_negative_rate")
    private Double v_negative_rate;

    public PosNegRate(Long meeting_id, String users, Double a_positive_rate, Double a_negative_rate, Double v_positive_rate, Double v_negative_rate) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.a_positive_rate = a_positive_rate;
        this.a_negative_rate = a_negative_rate;
        this.v_positive_rate = v_positive_rate;
        this.v_negative_rate = v_negative_rate;
    }
}
