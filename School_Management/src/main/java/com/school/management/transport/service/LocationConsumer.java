package com.school.management.transport.service;

import com.school.management.common.service.EmailService;
import com.school.management.common.util.HaversineUtil;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import com.school.management.transport.dto.BusLocationEvent;
import com.school.management.transport.entity.RouteStop;
import com.school.management.transport.entity.Vehicle;
import com.school.management.transport.repository.RouteStopRepository;
import com.school.management.transport.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Kafka Consumer — listens to "bus-location-events" topic and:
 * 1. Caches latest position in Redis
 * 2. Broadcasts to WebSocket subscribers (parents watching map)
 * 3. Checks geofence (< 2km from stop) and sends email alert to parents
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final VehicleRepository vehicleRepo;
    private final RouteStopRepository routeStopRepo;
    private final StudentRepository studentRepo;
    private final EmailService emailService;

    private static final double GEOFENCE_RADIUS_KM = 2.0;

    @KafkaListener(topics = "bus-location-events", groupId = "bus-tracking-group")
    public void consume(BusLocationEvent event) {
        log.info("📥 Kafka consumed | Vehicle {} | ({}, {})",
                event.getVehicleNumber(), event.getLatitude(), event.getLongitude());

        // ── 1. Cache latest position in Redis ───────────────────────────────
        String redisKey = "bus:location:" + event.getVehicleId();
        redisTemplate.opsForHash().put(redisKey, "latitude", event.getLatitude().toString());
        redisTemplate.opsForHash().put(redisKey, "longitude", event.getLongitude().toString());
        redisTemplate.opsForHash().put(redisKey, "speed", event.getSpeed() != null ? event.getSpeed().toString() : "0");
        redisTemplate.opsForHash().put(redisKey, "timestamp", event.getTimestamp().toString());
        redisTemplate.expire(redisKey, Duration.ofHours(12)); // Auto-expire at end-of-day

        // ── 2. Push to WebSocket subscribers ────────────────────────────────
        messagingTemplate.convertAndSend("/topic/bus/" + event.getVehicleId(), event);

        // ── 3. Geofence check — alert parents if bus is approaching ─────────
        checkGeofenceAndAlert(event);
    }

    private void checkGeofenceAndAlert(BusLocationEvent event) {
        Optional<Vehicle> vehicleOpt = vehicleRepo.findById(event.getVehicleId());
        if (vehicleOpt.isEmpty() || vehicleOpt.get().getRoute() == null) return;

        Long routeId = vehicleOpt.get().getRoute().getId();
        List<RouteStop> stops = routeStopRepo.findByRouteIdOrderByStopOrderAsc(routeId);

        for (RouteStop stop : stops) {
            double distance = HaversineUtil.distanceInKm(
                    event.getLatitude(), event.getLongitude(),
                    stop.getLatitude(), stop.getLongitude());

            if (distance <= GEOFENCE_RADIUS_KM) {
                log.info("🔔 Geofence triggered! Bus {} is {:.1f}km from stop '{}'",
                        event.getVehicleNumber(), distance, stop.getStopName());

                // Find all students assigned to this stop and email their parents
                notifyParentsAtStop(stop, event.getVehicleNumber(), distance);
            }
        }
    }

    private void notifyParentsAtStop(RouteStop stop, String vehicleNumber, double distance) {
        // Find students with matching routeStop
        List<Student> students = studentRepo.findAll().stream()
                .filter(s -> s.getRouteStop() != null
                        && s.getRouteStop().getId().equals(stop.getId())
                        && s.getParentEmail() != null)
                .toList();

        for (Student student : students) {
            // Prevent duplicate alerts: use Redis key per student per day
            String alertKey = "bus:alert:" + student.getId() + ":" + stop.getId() + ":" + LocalDate.now();
            Boolean alreadySent = redisTemplate.hasKey(alertKey);

            if (Boolean.TRUE.equals(alreadySent)) continue; // Already alerted today

            try {
                emailService.sendBusApproachingEmail(
                        student.getParentEmail(),
                        student.getParentName(),
                        student.getUser() != null ? student.getUser().getFullName() : student.getRollNumber(),
                        stop.getStopName(),
                        vehicleNumber,
                        distance);
                // Mark as sent for today
                redisTemplate.opsForValue().set(alertKey, "sent", Duration.ofHours(18));
                log.info("✉️ Bus alert sent to {} for student {} at stop '{}'",
                        student.getParentEmail(), student.getRollNumber(), stop.getStopName());
            } catch (Exception e) {
                log.error("Failed to send bus alert email to {}", student.getParentEmail(), e);
            }
        }
    }
}
