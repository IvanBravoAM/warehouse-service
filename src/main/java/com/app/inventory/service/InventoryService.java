package com.app.inventory.service;

import com.app.inventory.entity.InventoryItem;
import com.app.inventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository repository;

    public InventoryService(InventoryItemRepository repository) {
        this.repository = repository;
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
}
