package com.app.inventory.repository;
import java.util.UUID;
import com.app.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;



@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    InventoryItem findByName(String name); 

    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity + :delta WHERE i.id = :id")
    void updateStockQuantity(@Param("id") UUID id, @Param("delta") int delta);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Page<InventoryItem> findBySku(@Param("sku") String sku, Pageable pageable);

}
