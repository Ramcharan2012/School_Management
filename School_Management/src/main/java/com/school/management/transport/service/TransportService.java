package com.school.management.transport.service;

import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.transport.entity.Route;
import com.school.management.transport.entity.RouteStop;
import com.school.management.transport.entity.Vehicle;
import com.school.management.transport.repository.RouteRepository;
import com.school.management.transport.repository.RouteStopRepository;
import com.school.management.transport.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD service for Transport module — manages vehicles, routes, and stops.
 * Also provides current bus location from Redis cache.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransportService {

    private final VehicleRepository vehicleRepo;
    private final RouteRepository routeRepo;
    private final RouteStopRepository routeStopRepo;
    private final RedisTemplate<String, String> redisTemplate;

    // ── Vehicle CRUD ──────────────────────────────────────────────────────

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepo.save(vehicle);
    }

    public List<Vehicle> getAllActiveVehicles() {
        return vehicleRepo.findByIsActiveTrue();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    // ── Route CRUD ────────────────────────────────────────────────────────

    public Route createRoute(Route route) {
        return routeRepo.save(route);
    }

    public List<Route> getAllActiveRoutes() {
        return routeRepo.findByIsActiveTrue();
    }

    public Route getRouteById(Long id) {
        return routeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
    }

    // ── RouteStop CRUD  ────────────────────────────────────────────────────

    public RouteStop addStopToRoute(Long routeId, RouteStop stop) {
        Route route = getRouteById(routeId);
        stop.setRoute(route);
        return routeStopRepo.save(stop);
    }

    public List<RouteStop> getStopsForRoute(Long routeId) {
        return routeStopRepo.findByRouteIdOrderByStopOrderAsc(routeId);
    }

    // ── Live Location from Redis ──────────────────────────────────────────

    /**
     * Get the latest cached location for a bus from Redis.
     * Returns null if bus has not reported yet.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getCurrentBusLocation(Long vehicleId) {
        String redisKey = "bus:location:" + vehicleId;
        Map<Object, Object> rawData = redisTemplate.opsForHash().entries(redisKey);

        if (rawData.isEmpty()) return null;

        Map<String, String> location = new HashMap<>();
        rawData.forEach((k, v) -> location.put(k.toString(), v.toString()));
        return location;
    }
}
