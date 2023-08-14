package uz.tirgo.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.bot.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
