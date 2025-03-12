package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "highlight_statement")
@TableName("highlight_statement")
public class HighlightStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "highlight_id")
    @TableId(type = IdType.AUTO)
    private Long highlight_id;

    @Column(name = "label")
    private Integer label;

    @Column(name = "statement")
    private String statement;

    @Column(name = "tag")
    private Integer tag;
}
