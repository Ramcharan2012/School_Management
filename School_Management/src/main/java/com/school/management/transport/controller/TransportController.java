package com.school.management.transport.controller;

import com.school.management.common.response.ApiResponse;
import com.school.management.transport.dto.BusLocationEvent;
import com.school.management.transport.entity.Route;
import com.school.management.transport.entity.RouteStop;
import com.school.management.transport.entity.Vehicle;
import com.school.management.transport.service.LocationProducer;
import com.school.management.transport.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Transport & Bus Tracking controller.
 *
 * Admin endpoints  → manage vehicles, routes, stops.
 * Driver endpoint  → POST GPS coordinates (ingested via Kafka).
 * Parent endpoint  → GET current bus location (from Redis).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Transport & Bus Tracking", description = "Manage routes, vehicles, and live bus tracking")
@SecurityRequirement(name = "bearerAuth")
public class TransportController {

    private final TransportService transportService;
    private final LocationProducer locationProducer;

    // ── Admin: Vehicle Management ─────────────────────────────────────────

    @PostMapping("/admin/transport/vehicles")
    @Operation(summary = "Register a new bus/vehicle")
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@RequestBody CreateVehicleRequest req) {
        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(req.getVehicleNumber())
                .vehicleType(req.getVehicleType() != null ? req.getVehicleType() : "BUS")
                .capacity(req.getCapacity())
                .driverName(req.getDriverName())
                .driverPhone(req.getDriverPhone())
                .build();

        // Optionally link to route
        if (req.getRouteId() != null) {
            Route route = transportService.getRouteById(req.getRouteId());
            vehicle.setRoute(route);
        }

        return ResponseEntity.ok(ApiResponse.success("Vehicle registered.", transportService.createVehicle(vehicle)));
    }

    @GetMapping("/admin/transport/vehicles")
    @Operation(summary = "List all active vehicles")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getVehicles() {
        return ResponseEntity.ok(ApiResponse.success(transportService.getAllActiveVehicles()));
    }

    // ── Admin: Route Management ───────────────────────────────────────────

    @PostMapping("/admin/transport/routes")
    @Operation(summary = "Create a new bus route")
    public ResponseEntity<ApiResponse<Route>> createRoute(@RequestBody CreateRouteRequest req) {
        Route route = Route.builder()
                .routeName(req.getRouteName())
                .description(req.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Route created.", transportService.createRoute(route)));
    }

    @GetMapping("/admin/transport/routes")
    @Operation(summary = "List all active routes")
    public ResponseEntity<ApiResponse<List<Route>>> getRoutes() {
        return ResponseEntity.ok(ApiResponse.success(transportService.getAllActiveRoutes()));
    }

    // ── Admin: Stop Management ────────────────────────────────────────────

    @PostMapping("/admin/transport/routes/{routeId}/stops")
    @Operation(summary = "Add a GPS-tagged stop to a route")
    public ResponseEntity<ApiResponse<RouteStop>> addStop(@PathVariable Long routeId,
            @RequestBody CreateStopRequest req) {
        RouteStop stop = RouteStop.builder()
                .stopName(req.getStopName())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .stopOrder(req.getStopOrder())
                .pickupTime(req.getPickupTime())
                .dropTime(req.getDropTime())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Stop added.", transportService.addStopToRoute(routeId, stop)));
    }

    @GetMapping("/admin/transport/routes/{routeId}/stops")
    @Operation(summary = "List all stops on a route (ordered)")
    public ResponseEntity<ApiResponse<List<RouteStop>>> getStops(@PathVariable Long routeId) {
        return ResponseEntity.ok(ApiResponse.success(transportService.getStopsForRoute(routeId)));
    }

    // ── Driver: GPS Ingestion (→ Kafka) ────────────────────────────────────

    @PostMapping("/transport/location")
    @Operation(summary = "Driver sends live GPS coordinates (pushed to Kafka)")
    public ResponseEntity<ApiResponse<String>> sendLocation(@RequestBody SendLocationRequest req) {
        BusLocationEvent event = BusLocationEvent.builder()
                .vehicleId(req.getVehicleId())
                .vehicleNumber(req.getVehicleNumber())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .speed(req.getSpeed())
                .timestamp(Instant.now())
                .build();
        locationProducer.publishLocation(event);
        return ResponseEntity.ok(ApiResponse.success("Location recorded."));
    }

    // ── Parent: Get Current Bus Location (from Redis) ──────────────────────

    @GetMapping("/transport/bus/{vehicleId}/location")
    @Operation(summary = "Get current bus location (from Redis cache)")
    public ResponseEntity<ApiResponse<Map<String, String>>> getBusLocation(@PathVariable Long vehicleId) {
        Map<String, String> location = transportService.getCurrentBusLocation(vehicleId);
        if (location == null) {
            return ResponseEntity.ok(ApiResponse.success("Bus has not reported yet.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Current bus location.", location));
    }

    // ── Request DTOs ──────────────────────────────────────────────────────

    @Data
    static class CreateVehicleRequest {
        private String vehicleNumber;
        private String vehicleType;
        private Integer capacity;
        private String driverName;
        private String driverPhone;
        private Long routeId;
    }

    @Data
    static class CreateRouteRequest {
        private String routeName;
        private String description;
    }

    @Data
    static class CreateStopRequest {
        private String stopName;
        private Double latitude;
        private Double longitude;
        private Integer stopOrder;
        private LocalTime pickupTime;
        private LocalTime dropTime;
    }

    @Data
    static class SendLocationRequest {
        private Long vehicleId;
        private String vehicleNumber;
        private Double latitude;
        private Double longitude;
        private Double speed;
    }
}
