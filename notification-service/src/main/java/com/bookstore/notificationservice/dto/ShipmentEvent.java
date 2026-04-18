
// ── ShipmentEvent.java (incoming) ────────────────────
package com.bookstore.notificationservice.dto;

public class ShipmentEvent {
    private String orderId;
    private String customerId;
    private String shipmentStatus;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
}