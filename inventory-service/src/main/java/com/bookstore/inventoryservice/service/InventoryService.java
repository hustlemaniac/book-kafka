package com.bookstore.inventoryservice.service;

import com.bookstore.inventoryservice.dto.InventoryEvent;
import com.bookstore.inventoryservice.dto.PaymentEvent;
import com.bookstore.inventoryservice.model.Inventory;
import com.bookstore.inventoryservice.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final String PUBLISH_TOPIC = "inventory-updated";

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Only process successful payments
    @Transactional
    public void processPaymentEvent(PaymentEvent event) {
        // Throwing here triggers the retry + DLT flow
        if (!"SUCCESS".equals(event.getPaymentStatus())) {
            throw new IllegalArgumentException(
                    "Payment not successful for orderId=" + event.getOrderId()
                            + ", status=" + event.getPaymentStatus()
                            + " — routing to DLT immediately"
            );
        }

        Inventory inventory = new Inventory();
        inventory.setOrderId(event.getOrderId());
        inventory.setCustomerId(event.getCustomerId());
        inventory.setStatus("RESERVED");
        inventoryRepository.save(inventory);

        log.info("Stock reserved for orderId={}", event.getOrderId());

        InventoryEvent outEvent = new InventoryEvent();
        outEvent.setOrderId(event.getOrderId());
        outEvent.setCustomerId(event.getCustomerId());
        outEvent.setInventoryStatus("RESERVED");

        kafkaTemplate.send(PUBLISH_TOPIC, event.getOrderId(), outEvent);
        log.info("Inventory event published | orderId={} | status=RESERVED", event.getOrderId());
    }
}