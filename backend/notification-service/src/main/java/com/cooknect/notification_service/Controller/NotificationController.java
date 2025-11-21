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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/sendMail")
    @Operation(summary = "Send an email notification", security = @SecurityRequirement(name = "bearerAuth"))
    public String sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        notificationService.sendEmail(to, subject, body);
        return "Mail sent successfully!";
    }

    @GetMapping("/user/{userEmail}")
    @Operation(summary = "Get notifications for a user", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Notification> getUserNotifications(@PathVariable String userEmail) {
        return notificationService.getNotificationsForUser(userEmail);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read", security = @SecurityRequirement(name = "bearerAuth"))
    public Notification markNotificationAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification", security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }
}
