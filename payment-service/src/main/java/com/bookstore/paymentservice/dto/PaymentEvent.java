package com.bookstore.paymentservice.dto;

// This is what gets published to the payment-processed topic
// used to produce the event
public class PaymentEvent {

    private String orderId;
    private String customerId;
    private String paymentStatus;  // SUCCESS or FAILED

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}