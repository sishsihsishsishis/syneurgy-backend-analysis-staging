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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "handle_table")
@TableName("handle_table")
public class HandleTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "handle_table_id")
    @TableId(type = IdType.AUTO)
    private Long handle_table_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "handle_id")
    private Integer handle_id;

    @Column(name = "is_handle", columnDefinition = "INTEGER DEFAULT 0")
    private Integer is_handle;

}
