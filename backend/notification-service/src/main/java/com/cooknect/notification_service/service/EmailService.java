package com.cooknect.notification_service.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            log.info("Sending email to: {}", to);
            log.info("Email subject: {}", subject);
            log.info("Email body: {}", body);
            javaMailSender.send(mail);
        } catch (Exception e) {
            log.error("Exception while sendEmail ", e);
        }
    }
}
