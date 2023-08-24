package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Supplier;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.repository.SupplierRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationService {
    public static final double RADIUS_OF_EARTH = 6371.0; // Earth's radius in kilometers
    private final SupplierRepository supplierRepository;

    public Supplier getNearestSupplier(Location location) {
        Long chatId = null;
        double minDistance = Double.MAX_VALUE; // Boshlang'ich max qiymat
        Supplier nearestSupplier = null;

        for (Map.Entry<Long, Location> entry : KeyWords.supplierLocation.entrySet()) {
            double distance = calculateDistance(location, entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                chatId = entry.getKey();
            }
        }

        if (chatId != null) {
            // Chat ID bo'yicha qo'shimcha amallar
            nearestSupplier = supplierRepository.findById(chatId).get();// ... Supplier olish kerak bo'lsa
        }

        return nearestSupplier;
    }


    public double calculateDistance(Location l1, Location l2) {
        double lat1 = l1.getLatitude();
        double lon1 = l1.getLongitude();
        double lat2 = l2.getLatitude();
        double lon2 = l2.getLongitude();
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
