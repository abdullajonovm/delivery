package uz.tirgo.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "heading")
    private Integer heading;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "horizontal_accuracy")
    private Double horizontalAccuracy;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "live_period")
    private Integer livePeriod;

    @Column(name = "proximity_alert_radius")
    private Integer proximityAlertRadius;

    private Integer date;

    public Location(String text) {
        this.name = text;
    }

    public Location(org.telegram.telegrambots.meta.api.objects.Location location) {

        this.heading = location.getHeading();
        this.latitude = location.getLatitude();
        this.horizontalAccuracy = location.getHorizontalAccuracy();
        this.longitude = location.getLongitude();
        this.livePeriod = location.getLivePeriod();
        this.proximityAlertRadius = location.getProximityAlertRadius();
    }

    public Location(org.telegram.telegrambots.meta.api.objects.Location location, Integer editDate) {
        this.heading = location.getHeading();
        this.latitude = location.getLatitude();
        this.horizontalAccuracy = location.getHorizontalAccuracy();
        this.longitude = location.getLongitude();
        this.livePeriod = location.getLivePeriod();
        this.proximityAlertRadius = location.getProximityAlertRadius();
        this.date = editDate;
    }
}

