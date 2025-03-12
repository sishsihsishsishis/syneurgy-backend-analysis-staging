package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "individual_score")
public class IndividualScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "individual_score_id")
    @TableId(type = IdType.AUTO)
    private Long individual_score_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "brain_score")
    private Double brain_score;

    @Column(name = "body_score")
    private Double body_score;

    @Column(name = "behavior_score")
    private Double behavior_score;


}
