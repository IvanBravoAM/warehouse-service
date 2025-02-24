package com.app.inventory.service;

import com.app.inventory.entity.InventoryItem;
import com.app.inventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.app.inventory.model.InventoryEvent;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors;S



@Service
public class InventoryService {

    private final InventoryItemRepository repository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public InventoryService(InventoryItemRepository repository) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;

    }

    /**
     * Save a new InventoryItem and return a response DTO.
     *
     * @param item InventoryItem object
     * @return InventoryItemResponse with saved item details
     */
    public InventoryItem save(InventoryItem item) {
        InventoryItem savedItem = repository.save(item);
        return savedItem;
    }

    /**
     * Update an existing InventoryItem and return a response DTO.
     *
     * @param id   ID of the item to update
     * @param item InventoryItem object with updated data
     * @return InventoryItemResponse with updated item details
     */
    public InventoryItem update(Long id, InventoryItem item) {
        InventoryItem existingItem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found!"));

        // Update fields
        existingItem.setName(item.getName());
        existingItem.setQuantity(item.getQuantity());
        existingItem.setPrice(item.getPrice());

        InventoryItem updatedItem = repository.save(existingItem);
        return updatedItem;
    }

    public List<InventoryItem> getAllItems() {
        return repository.findAll();
    }

    public InventoryItem getItemById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Item not found!"));
    }

    public InventoryItem addItem(InventoryItem item) {
        return repository.save(item);
    }

    public void deleteItem(Long id) {
        repository.deleteById(id);
    }

    public InventoryItem updateStock(UUID itemId, int delta) {
        InventoryItem item = repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        repository.updateStockQuantity(itemId, delta);
        item.setQuantity(item.getQuantity() + delta);

        // Publish Kafka event
        InventoryEvent event = new InventoryEvent(
                itemId,
                delta,
                "STOCK_UPDATED",
                Instant.now()
        );
        kafkaTemplate.send("inventory-updates", event);

        return item;
    }

    public List<InventoryItem> searchBySku(String sku, int page) {
        Pageable pageable = PageRequest.of(page, 10); // assuming 10 items per page
        return repository.findBySku(sku, pageable).stream().collect(Collectors.toList());
    }
}
