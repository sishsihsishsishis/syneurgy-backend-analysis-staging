package com.aws.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "user_avatar")
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatar implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_avatar_id")
    @TableId(type = IdType.AUTO)
    private Long user_avatar_id;

    @Column(name = "meeting_id")
    private Long meeting_id;

    @Column(name = "users")
    private String users;

    @Column(name = "url")
    private String url;

    public UserAvatar(Long meeting_id, String users, String url) {
        this.meeting_id = meeting_id;
        this.users = users;
        this.url = url;
    }
}
