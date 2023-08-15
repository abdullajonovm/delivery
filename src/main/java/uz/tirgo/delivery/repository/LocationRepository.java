package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.delivery.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
