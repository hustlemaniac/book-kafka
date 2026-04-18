package com.bookstore.inventoryservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Listens to the DLT so failed messages are visible and actionable.
// In production you'd alert, store in a DB, or trigger a manual review flow.
@Component
public class DltConsumer {

    private static final Logger log = LoggerFactory.getLogger(DltConsumer.class);

    @KafkaListener(topics = "payment-processed.DLT", groupId = "inventory-dlt-group")
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("Dead letter received | topic={} | partition={} | offset={} | key={} | value={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value());

    }
}