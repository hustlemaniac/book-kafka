package com.bookstore.orderservice.service;

import com.bookstore.orderservice.dto.OrderEvent;
import com.bookstore.orderservice.kafka.OrderProducer;
import com.bookstore.orderservice.model.Order;
import com.bookstore.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderService(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    @Transactional
    public Order placeOrder(Order order) {
        order.setStatus("PLACED");
        Order saved = orderRepository.save(order);

        OrderEvent event = new OrderEvent();
        event.setOrderId(saved.getId());
        event.setBookId(saved.getBookId());
        event.setCustomerId(saved.getCustomerId());
        event.setQuantity(saved.getQuantity());
        event.setStatus(saved.getStatus());
        event.setAmount(saved.getAmount());

        orderProducer.sendOrderEvent(event);
        return saved;
    }
}