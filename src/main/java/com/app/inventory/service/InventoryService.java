package com.app.inventory.service;

import com.app.inventory.entity.InventoryItem;
import com.app.inventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.app.inventory.model.InventoryEvent;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors;
import com.app.inventory.exception.ItemNotFoundException;



@Service
public class InventoryService {
    private static final Logger logger = LogManager.getLogger(InventoryService.class);
    private final InventoryItemRepository repository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public InventoryService(InventoryItemRepository repository, KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
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
    public InventoryItem update(UUID id, InventoryItem item) {
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

    public InventoryItem getItemById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    public InventoryItem addItem(InventoryItem item) {
        return repository.save(item);
    }

    public void deleteItem(UUID id) {
        repository.deleteById(id);
    }

    public InventoryItem updateStock(UUID itemId, int delta) {
        InventoryItem item = repository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        Instant updated = Instant.now();
        repository.updateStockQuantity(itemId, delta, updated);
        item.setQuantity(item.getQuantity() + delta);

        // Publish Kafka event
        InventoryEvent event = new InventoryEvent(
                itemId,
                delta,
                "STOCK_UPDATED",
                Instant.now()
        );
        // Log the event before sending
        logger.info("Publishing event: {}", event);
  
        // Add a callback to log success/failure
        kafkaTemplate.send("inventory-updates", event).handle((result, ex) -> {
            if (ex == null) {
                logger.info("Event sent successfully: {}", event);
            } else {
                logger.error("Failed to send event: {}", event, ex);
            }
            return null;
        });

        return item;
    }

    public List<InventoryItem> searchBySku(String sku, int page) {
        Pageable pageable = PageRequest.of(page, 10); // assuming 10 items per page
        logger.info("sku: {}", sku);
        List<InventoryItem> list = repository.findBySku(sku, pageable).stream().collect(Collectors.toList());
        logger.info("list: {}", list);
        return list;
    }
}
