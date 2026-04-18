package com.bookstore.notificationservice.repository;

import com.bookstore.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    boolean existsByOrderId(String orderId);  // idempotency guard
}