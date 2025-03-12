package com.aws.sync.service.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EmailService {
    @Resource
    private JavaMailSender javaMailSender;

    public String senderMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        // 发件人 你的邮箱
        message.setFrom("89375133@qq.com");
        // 接收人 接收者邮箱
        message.setTo(new String[]{"1029003236@qq.com"});

        //邮件标题
        message.setSubject("hello");

        //邮件内容
        message.setText("world");

        javaMailSender.send(message);

        return "success";
    }
}
