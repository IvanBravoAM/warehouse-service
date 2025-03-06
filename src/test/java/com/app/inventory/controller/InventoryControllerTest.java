package com.app.inventory.controller;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import com.app.inventory.config.SecurityConfig;
import com.app.inventory.entity.InventoryItem;
import com.app.inventory.exception.ItemNotFoundException;
import com.app.inventory.service.InventoryService;

import java.util.UUID;

// InventoryControllerTest.java
@WebMvcTest(InventoryController.class)

@Import(SecurityConfig.class) 
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InventoryService inventoryService;

    @Test
    void updateStock_ValidRequest_Returns200() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(inventoryService.updateStock(itemId, 10))
            .thenReturn(new InventoryItem(itemId, "Keyboard", 10, 30));

        (mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/inventory/{id}/stock", itemId)
                .param("delta", "10")))
                .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    void updateStock_InvalidDelta_Returns400() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/inventory/{id}/stock", itemId)
                .param("delta", "2000")) // Invalid delta
                .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateStock_ItemNotFound_Returns404() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(inventoryService.updateStock(itemId, 10))
            .thenThrow(new ItemNotFoundException(itemId));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/inventory/{id}/stock", itemId)
                .param("delta", "10"))
                .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}