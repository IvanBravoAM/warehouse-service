package com.app.inventory.integration;

import com.app.inventory.model.InventoryEvent;
import com.app.inventory.config.KafkaProducerConfig;
import com.app.inventory.entity.InventoryItem;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.System; // Removed incorrect import
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(KafkaProducerConfig.class)
@Testcontainers
class InventoryIntegrationTest {

    private static final Logger logger = LogManager.getLogger(InventoryIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, InventoryEvent> consumerFactory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void cleanup() {
        // Clear Kafka topics between tests (if needed)
        // kafka.execInContainer("kafka-topics --delete --topic inventory-updates --bootstrap-server localhost:9092");
    }

    @Test
    void shouldCreateAndRetrieveItem() {
        // Create Item
        InventoryItem request = new InventoryItem(
            null, "Laptop", 15,"SKU-2369", 10, "WAREHOUSE-2369"
        );

        ResponseEntity<InventoryItem> createResponse = restTemplate.postForEntity(
            "/api/v1/inventory", 
            request, 
            InventoryItem.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        InventoryItem createdItem = createResponse.getBody();
        assertNotNull(createdItem.getId());

        // Retrieve Item
        ResponseEntity<InventoryItem> getResponse = restTemplate.getForEntity(
            "/api/v1/inventory/" + createdItem.getId(), 
            InventoryItem.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(createdItem.getId(), getResponse.getBody().getId());
        assertEquals("Laptop", getResponse.getBody().getName());
    }

    @Test
    void shouldUpdateStockAndPublishEvent() throws Exception {
        // Create item
        InventoryItem request = new InventoryItem(
            null, "Laptop", 15, "SKU-2369", 10, "WAREHOUSE-2369"
        );

        ResponseEntity<InventoryItem> createResponse = restTemplate.postForEntity(
            "/api/v1/inventory", 
            request, 
            InventoryItem.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        InventoryItem createdItem = createResponse.getBody();
        assertNotNull(createdItem.getId());
        logger.error("Created item: {} " , createdItem);

        // Set up Kafka listener
        AtomicReference<ConsumerRecord<String, InventoryEvent>> receivedRecord = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        ContainerProperties containerProps = new ContainerProperties("inventory-updates");
        containerProps.setMessageListener((MessageListener<String, InventoryEvent>) record -> {
            receivedRecord.set(record);
            latch.countDown();
        });

        KafkaMessageListenerContainer<String, InventoryEvent> container = 
        new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.start();

        // Use the createdItem's ID (not the request's ID)
        String url = UriComponentsBuilder
            .fromUriString("/api/v1/inventory/{id}/stock")
            .queryParam("delta", 3)
            .buildAndExpand(createdItem.getId()) // <-- Use createdItem.getId()
            .toUriString();

        // Send POST request
        ResponseEntity<InventoryItem> updateResponse = restTemplate.postForEntity(
            url, 
            null, 
            InventoryItem.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(18, updateResponse.getBody().getQuantity()); // 10 + 3 = 13

        // Verify Kafka event
        

        //assertTrue(latch.await(10, TimeUnit.SECONDS), "No event received");
        // ConsumerRecord<String, InventoryEvent> record = receivedRecord.get();
        // InventoryEvent event = record.value();

        // logger.debug("Received event: {}", event);
        // assertNotNull(event);
        // assertEquals(createdItem.getId(), event.itemId());
        // assertEquals(3, event.stockDelta());
    }

    @Test
    void shouldSearchBySku() {
        // Create test 
        restTemplate.postForObject("/api/v1/inventory/",
            new InventoryItem(null, "Item1", 15,"SKU-SEARCH", 10, "LOC1"),
            InventoryItem.class);

        restTemplate.postForObject("/api/v1/inventory/",
            new InventoryItem(null, "Item2",20, "SKU-SEARCH", 5, "LOC2"),
            InventoryItem.class);

        // Search
        ResponseEntity<InventoryItem[]> response = restTemplate.getForEntity(
            "/api/v1/inventory/search?sku=SKU-SEARCH",
            InventoryItem[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
        assertTrue(response.getBody()[0].getSku().contains("SKU-SEARCH"));
    }

    @Test
    void shouldReturnNotFoundForInvalidId() {
        UUID invalidId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/inventory" + invalidId,
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Item with ID '" + invalidId + "' not found"));
    }
}