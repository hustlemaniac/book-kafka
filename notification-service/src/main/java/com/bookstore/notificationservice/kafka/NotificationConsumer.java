package com.bookstore.notificationservice.kafka;

import com.bookstore.notificationservice.dto.ShipmentEvent;
import com.bookstore.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-shipped", groupId = "notification-group")
    public void handleOrderShipped(ShipmentEvent event) {
        log.info("Received shipment event | orderId={} | status={}",
                event.getOrderId(), event.getShipmentStatus());
        notificationService.processShipmentEvent(event);
    }
}