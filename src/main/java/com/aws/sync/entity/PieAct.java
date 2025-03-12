package com.aws.sync.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "pie_act")
public class PieAct implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @TableId(type = IdType.AUTO)
        @Column(name = "act_id")
//        @Column(columnDefinition = "bigint")
        private Long act_id;

        @Column(name = "meeting_id")
//        @Column(columnDefinition = "bigint")
        private Long meeting_id;

        @Column(name = "act")
//        @Column(columnDefinition = "varchar(30)")
        private String act;

        @Column(name = "act_time")
//        @Column(columnDefinition = "double")
        private Double act_time;

        @Column(name = "act_time_rate")
//        @Column(columnDefinition = "double")
        private Double act_time_rate;





}
