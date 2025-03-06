package com.app.inventory.exception;
import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    private final UUID itemId;

    public ItemNotFoundException(UUID itemId) {
        super("Item with ID '" + itemId + "' not found");
        this.itemId = itemId;
    }

    // Optional: Include the item ID for programmatic error handling
    public UUID getItemId() {
        return itemId;
    }
}