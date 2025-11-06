package com.cooknect.notification_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {
    @KafkaListener(topics = "recipe-topic", groupId = "notification-group")
    public void consume(String message) {
        System.out.println("📩 Received Notification: " + message);
    }
}
