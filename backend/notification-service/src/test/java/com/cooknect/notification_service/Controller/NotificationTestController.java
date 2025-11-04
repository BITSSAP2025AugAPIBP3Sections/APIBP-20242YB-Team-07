package com.cooknect.notification_service.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import com.cooknect.notification_service.service.EmailService;

public class NotificationTestController {
    @Autowired
    private EmailService emailService;

    @GetMapping("/test-mail")
    public String testMail() {
        emailService.sendEmail("receiver@example.com", "Hello from Spring Boot", "This is a test email!");
        return "Mail Sent!";
    }
}
