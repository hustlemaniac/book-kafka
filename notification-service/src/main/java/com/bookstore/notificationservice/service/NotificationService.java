package com.bookstore.notificationservice.service;

import com.bookstore.notificationservice.dto.NotificationEvent;
import com.bookstore.notificationservice.dto.ShipmentEvent;
import com.bookstore.notificationservice.model.Notification;
import com.bookstore.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String PUBLISH_TOPIC = "notification-sent";

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processShipmentEvent(ShipmentEvent event) {

        // Idempotency guard — if we already sent a notification for this
        // order (e.g. after a crash + retry), skip it.
        if (notificationRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Notification already sent for orderId={}, skipping",
                    event.getOrderId());
            return;
        }

        String message = "Your order " + event.getOrderId()
                + " has been shipped! Thank you for shopping with us.";

        Notification notification = new Notification();
        notification.setOrderId(event.getOrderId());
        notification.setCustomerId(event.getCustomerId());
        notification.setMessage(message);
        notification.setStatus("SENT");
        notificationRepository.save(notification);

        // In real life: send an actual email here via SendGrid, SES, etc.
        log.info("Notification sent | orderId={} | customerId={} | message={}",
                event.getOrderId(), event.getCustomerId(), message);

        // Publish confirmation event — wrapped in the same Kafka transaction
        NotificationEvent outEvent = new NotificationEvent();
        outEvent.setOrderId(event.getOrderId());
        outEvent.setCustomerId(event.getCustomerId());
        outEvent.setMessage(message);

        kafkaTemplate.executeInTransaction(kt ->
                kt.send(PUBLISH_TOPIC, event.getOrderId(), outEvent));
    }
}