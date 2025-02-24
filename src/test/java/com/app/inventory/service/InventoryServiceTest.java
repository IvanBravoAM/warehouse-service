package com.app.inventory.service;

import com.app.inventory.entity.InventoryItem;
import com.app.inventory.repository.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private final InventoryItemRepository repository = mock(InventoryItemRepository.class);
    private final InventoryService service = new InventoryService(repository);

    @Test
    void testSave() {
        // InventoryItem item = new InventoryItem("Widget", 10, 15.5);
        // when(repository.save(item)).thenReturn(new InventoryItem("Widget", 10, 15.5));

        // InventoryItem response = service.save(item);
        // assertThat(response.getId()).isEqualTo(1L);
        // assertThat(response.getName()).isEqualTo("Widget");
        // assertThat(response.getQuantity()).isEqualTo(10);
        // assertThat(response.getPrice()).isEqualTo(15.5);
    }

    @Test
    void testUpdate() {
        // InventoryItem existingItem = new InventoryItem("Widget", 10, 15.5);
        // InventoryItem updatedItem = new InventoryItem("Updated Widget", 20, 25.5);
        // when(repository.findById(1L)).thenReturn(java.util.Optional.of(existingItem));
        // when(repository.save(existingItem)).thenReturn(new InventoryItem("Updated Widget", 20, 25.5));

        // InventoryItem response = service.update(1L, updatedItem);
        // assertThat(response.getId()).isEqualTo(1L);
        // assertThat(response.getName()).isEqualTo("Updated Widget");
        // assertThat(response.getQuantity()).isEqualTo(20);
        // assertThat(response.getPrice()).isEqualTo(25.5);
    }
}
