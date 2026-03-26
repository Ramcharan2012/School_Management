package com.school.management.common.util;

/**
 * Haversine formula — calculates the great-circle distance between
 * two GPS coordinates on Earth's surface.
 *
 * Used for geofencing: "Is the bus within 2km of the student's stop?"
 *
 * Example:
 *   Bus:  lat=13.0500, lng=80.2500
 *   Stop: lat=13.0827, lng=80.2707
 *   Result → 1.8 km → ALERT!
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineUtil() {} // Utility class, no instantiation

    /**
     * @return distance in KILOMETERS between two lat/lng points.
     */
    public static double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0; // rounded to 2 decimals
    }
}
