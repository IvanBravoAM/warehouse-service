package com.app.inventory.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendInventoryUpdate(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
