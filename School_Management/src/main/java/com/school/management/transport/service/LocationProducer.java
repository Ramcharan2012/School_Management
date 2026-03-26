package com.school.management.transport.service;

import com.school.management.transport.dto.BusLocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer — publishes GPS coordinates to the "bus-location-events" topic.
 *
 * Flow: Driver App → POST API → LocationProducer → Kafka Topic
 *
 * Uses vehicleId as the Kafka key so all events for the same bus
 * go to the same partition (preserving order).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationProducer {

    private static final String TOPIC = "bus-location-events";

    private final KafkaTemplate<String, BusLocationEvent> kafkaTemplate;

    public void publishLocation(BusLocationEvent event) {
        String key = String.valueOf(event.getVehicleId());
        kafkaTemplate.send(TOPIC, key, event);
        log.info("📡 Published GPS → Kafka | Vehicle {} | ({}, {})",
                event.getVehicleNumber(), event.getLatitude(), event.getLongitude());
    }
}
