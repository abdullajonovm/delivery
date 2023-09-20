package uz.tirgo.delivery.test;

import uz.tirgo.delivery.entity.Location;

public class Test {
    public static final double RADIUS_OF_EARTH = 6371.0; // Earth's radius in kilometers

    public static void main(String[] args) {
        double distance = calculateDistance(41.365005, 69.230144, 69.242603, 41.284093);
        System.out.println("distance = " + distance);
    }

    public static double calculateDistance(double lat1, double lon1, double lon2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = RADIUS_OF_EARTH * c;
        return distance;
    }
}
