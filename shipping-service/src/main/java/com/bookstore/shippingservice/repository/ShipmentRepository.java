package com.bookstore.shippingservice.repository;

import com.bookstore.shippingservice.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {
}