package com.app.inventory.repository;

import com.app.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    // Custom query methods can be defined here if needed
    InventoryItem findByName(String name); // Example custom query method
}
