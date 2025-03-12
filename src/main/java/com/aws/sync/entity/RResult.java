package com.aws.sync.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "r_result_table")
@TableName("r_result_table")
public class RResult implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @TableId(type = IdType.AUTO)
        @Column(name = "r_result")
        private Long r_result;

        @Column(name = "meeting_id")
        private Long meeting_id;

        @Column(name = "time_ms")
        private Long time_ms;

        @Column(name = "r_mean")
        private Double r_mean;

        @Column(name = "r_std")
        private Double r_std;

        @Column(name = "user00")
        private Double user00;

        @Column(name = "user10")
        private Double user10;

        @Column(name = "user01")
        private Double user01;

        @Column(name = "user02")
        private Double user02;

        @Column(name = "user03")
        private Double user03;

        @Column(name = "user04")
        private Double user04;

        @Column(name = "user05")
        private Double user05;

        @Column(name = "user06")
        private Double user06;

        @Column(name = "user07")
        private Double user07;

        @Column(name = "user08")
        private Double user08;

        @Column(name = "user09")
        private Double user09;

        @Column(name = "user11")
        private Double user11;

        @Column(name = "user12")
        private Double user12;

        @Column(name = "user13")
        private Double user13;

        @Column(name = "user14")
        private Double user14;

        @Column(name = "user15")
        private Double user15;

        @Column(name = "user16")
        private Double user16;

        @Column(name = "user17")
        private Double user17;

        @Column(name = "user18")
        private Double user18;

        @Column(name = "user19")
        private Double user19;

        @Column(name = "user20")
        private Double user20;

        @Column(name = "user21")
        private Double user21;

        @Column(name = "user22")
        private Double user22;

        @Column(name = "user23")
        private Double user23;

        @Column(name = "user24")
        private Double user24;

        @Column(name = "user25")
        private Double user25;

        @Column(name = "user90")
        private Double user90;

        @Column(name = "user91")
        private Double user91;

        @Column(name = "user92")
        private Double user92;

        @Column(name = "user93")
        private Double user93;

        @Column(name = "user94")
        private Double user94;

        @Column(name = "user95")
        private Double user95;

        @Column(name = "user96")
        private Double user96;

        @Column(name = "user97")
        private Double user97;

        @Column(name = "user98")
        private Double user98;

        @Column(name = "user99")
        private Double user99;



}
