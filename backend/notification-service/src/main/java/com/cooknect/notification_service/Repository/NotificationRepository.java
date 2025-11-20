package com.cooknect.notification_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooknect.notification_service.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmail(String email);
}
