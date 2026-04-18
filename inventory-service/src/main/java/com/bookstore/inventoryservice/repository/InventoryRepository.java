package com.bookstore.inventoryservice.repository;

import com.bookstore.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InventoryRepository extends JpaRepository<Inventory, String> {

}

