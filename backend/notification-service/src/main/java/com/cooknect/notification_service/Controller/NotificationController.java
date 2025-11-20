package com.cooknect.notification_service.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cooknect.notification_service.model.Notification;
import com.cooknect.notification_service.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/sendMail")
    public String sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        notificationService.sendEmail(to, subject, body);
        return "Mail sent successfully!";
    }

    @GetMapping("/user/{userEmail}")
    public List<Notification> getUserNotifications(@PathVariable String userEmail) {
        return notificationService.getNotificationsForUser(userEmail);
    }

    @PatchMapping("/{id}/read")
    public Notification markNotificationAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }
}
