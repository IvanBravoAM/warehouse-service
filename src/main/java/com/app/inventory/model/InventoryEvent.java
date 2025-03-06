package com.app.inventory.model;

import java.time.Instant;
import java.util.UUID;

public record InventoryEvent(
        UUID itemId,
        int stockDelta,
        String eventType,
        Instant timestamp
) {}