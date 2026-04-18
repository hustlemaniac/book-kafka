package com.bookstore.inventoryservice.dto;

//outgoing event

public class InventoryEvent {
    private String orderId;
    private String customerId;
    private String inventoryStatus; // RESERVED or FAILED

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getInventoryStatus() { return inventoryStatus; }
    public void setInventoryStatus(String s) { this.inventoryStatus = s; }
}