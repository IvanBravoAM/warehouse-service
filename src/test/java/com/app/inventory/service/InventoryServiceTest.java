package com.app.inventory.service;

import com.app.inventory.entity.InventoryItem;
import com.app.inventory.exception.ItemNotFoundException;
import com.app.inventory.model.InventoryEvent;
import com.app.inventory.repository.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.app.inventory.repository.InventoryItemRepository;

import java.util.Optional;
import java.util.UUID;
// InventoryServiceTest.java
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private InventoryItemRepository inventoryRepository;
    @Mock
    private KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void updateStock_WhenItemExists_UpdatesStockAndPublishesEvent() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = new InventoryItem(itemId, "Laptop", 7, 10);
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.of(item));

        InventoryItem updatedItem = inventoryService.updateStock(itemId, 5);

        assertEquals(12, updatedItem.getQuantity());
        verify(kafkaTemplate).send(eq("inventory-updates"), any(InventoryEvent.class));
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        when(inventoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> {
            inventoryService.getItemById(nonExistentId);
        });
    }
}
