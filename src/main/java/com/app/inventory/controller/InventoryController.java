package com.app.inventory.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.app.inventory.service.*;
import com.app.inventory.entity.InventoryItem;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody @Valid InventoryItem inventory) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.save(inventory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItem(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable UUID id, @RequestBody @Valid InventoryItem inventory) {
        return ResponseEntity.ok(inventoryService.update(id, inventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<InventoryItem> updateStock(
            @PathVariable UUID id,
            @RequestParam @Min(-1000) @Max(1000) int delta
    ) {
        
        return ResponseEntity.ok(inventoryService.updateStock(id, delta));
    }

    @GetMapping("/search")
    public ResponseEntity<List<InventoryItem>> searchItems(
            @RequestParam String sku,
            @RequestParam(defaultValue = "0") int page
    ) {
        String sanitizedSku = sku.replace("\"", "");
        return ResponseEntity.ok(inventoryService.searchBySku(sanitizedSku, page));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }
}
