package com.bookstore.inventoryservice.kafka;

import com.bookstore.inventoryservice.dto.PaymentEvent;
import com.bookstore.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "payment-processed", groupId = "inventory-group")
    public void handlePaymentProcessed(PaymentEvent event) {
        log.info("Received payment event | orderId={} | status={}",
                event.getOrderId(), event.getPaymentStatus());
        inventoryService.processPaymentEvent(event);
    }
}