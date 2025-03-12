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
@Table(name = "score_parameter")
@TableName("score_parameter")
@NoArgsConstructor
@AllArgsConstructor
public class ScoreParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(name = "score_parameter_id")
    private Long score_parameter_id;

    @Column(name = "team_id")
    private Long team_id;

    @Column(name = "coefficient_body")
    private Double coefficient_body;

    @Column(name = "coefficient_behaviour")
    private Double coefficient_behaviour;

    @Column(name = "coefficient_brain")
    private Double coefficient_brain;

    @Column(name = "coefficient_total")
    private Double coefficient_total;

    @Column(name = "weight_brain")
    private Double weight_brain;

    @Column(name = "weight_body")
    private Double weight_body;

    @Column(name = "weight_behaviour")
    private Double weight_behaviour;

    @Column(name = "weight_nlp_speak_time")
    private Double weight_nlp_speak_time;

    @Column(name = "weight_nlp_equal_participation")
    private Double weight_nlp_equal_participation;

    public ScoreParameter(Long team_id, Double coefficient_body, Double coefficient_behaviour, Double coefficient_brain, Double coefficient_total, Double weight_brain, Double weight_body, Double weight_behaviour, Double weight_nlp_speak_time, Double weight_nlp_equal_participation) {
        this.team_id = team_id;
        this.coefficient_body = coefficient_body;
        this.coefficient_behaviour = coefficient_behaviour;
        this.coefficient_brain = coefficient_brain;
        this.coefficient_total = coefficient_total;
        this.weight_brain = weight_brain;
        this.weight_body = weight_body;
        this.weight_behaviour = weight_behaviour;
        this.weight_nlp_speak_time = weight_nlp_speak_time;
        this.weight_nlp_equal_participation = weight_nlp_equal_participation;
    }
}
