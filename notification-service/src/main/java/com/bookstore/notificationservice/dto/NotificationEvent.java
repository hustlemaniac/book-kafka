// ── NotificationEvent.java (outgoing) ────────────────
package com.bookstore.notificationservice.dto;

public class NotificationEvent {
    private String orderId;
    private String customerId;
    private String message;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}