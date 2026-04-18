package com.bookstore.paymentservice.kafka;

import com.bookstore.paymentservice.dto.OrderEvent;
import com.bookstore.paymentservice.dto.PaymentEvent;
import com.bookstore.paymentservice.model.Payment;
import com.bookstore.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);
    private static final String PUBLISH_TOPIC = "payment-processed";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentConsumer(PaymentRepository paymentRepository,
                           KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "order-placed",
            groupId = "payment-group",
            concurrency = "3"   // one thread per partition
    )
    public void handleOrderPlaced(OrderEvent event) {
        log.info("Received order event | orderId={} | customerId={}",
                event.getOrderId(), event.getCustomerId());

        // Simulate payment processing (always succeeds for now)
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setCustomerId(event.getCustomerId());
        payment.setStatus("SUCCESS");
        payment.setAmount(event.getAmount());
        paymentRepository.save(payment);

        // Publish result downstream
        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setOrderId(event.getOrderId());
        paymentEvent.setCustomerId(event.getCustomerId());
        paymentEvent.setPaymentStatus("SUCCESS");

        kafkaTemplate.send(PUBLISH_TOPIC, event.getOrderId(), paymentEvent);
        log.info("Payment processed | orderId={} | status=SUCCESS", event.getOrderId());
    }
}