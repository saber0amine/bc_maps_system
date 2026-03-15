package org.example.bc_maps_system.util;

public class GeoUtils {
    // Rayon moyen de la Terre en km (formule Haversine)
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Formule Haversine — distance en km entre deux points GPS.
     *
     * @param lat1 latitude  du point 1 (degrés)
     * @param lng1 longitude du point 1 (degrés)
     * @param lat2 latitude  du point 2 (degrés)
     * @param lng2 longitude du point 2 (degrés)
     * @return distance en kilomètres
     */
    static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
