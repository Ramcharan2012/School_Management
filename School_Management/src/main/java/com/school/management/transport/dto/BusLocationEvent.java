package com.school.management.transport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * GPS event published by the driver app and sent through Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusLocationEvent {
    private Long vehicleId;
    private String vehicleNumber;
    private Double latitude;
    private Double longitude;
    private Double speed;        // km/h (optional, from phone GPS)
    private Instant timestamp;
}
