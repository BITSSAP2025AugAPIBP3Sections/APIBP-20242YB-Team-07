package com.cooknect.notification_service.service;

import org.springframework.stereotype.Service;

import com.cooknect.common.events.UserEvent;
import com.cooknect.notification_service.Repository.NotificationRepository;
import com.cooknect.notification_service.model.Notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = {"recipe-topic", "user-topic", "challenge-topic"}, groupId = "notification-group")
    public void handleUserEvent(UserEvent event) {
        System.out.println("Received user created event: " + event);
        sendEmail(event.getEmail(), event.getMessageSubject(), event.getMessageBody());
    }
    
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
            Notification notification = new Notification();
            notification.setEmail(to);
            notification.setSubject(subject);
            notification.setBody(body);
            notification.setReadStatus(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            createNotification(notification);
        } catch (Exception e) {
            log.error("Exception while sendEmail ", e);
        }
    }

    public Notification createNotification(Notification notification) {
        return repository.save(notification);
    }

    public List<Notification> getNotificationsForUser(String email) {
        return repository.findByEmail(email);
    }

    public Notification markAsRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setReadStatus(true);
        return repository.save(notification);
    }

    public void deleteNotification(Long id) {
        repository.deleteById(id);
    }
}
