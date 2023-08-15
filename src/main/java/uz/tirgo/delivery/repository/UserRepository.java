package uz.tirgo.delivery.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.delivery.entity.Seller;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Seller, Long> {

    Boolean existsByChatId(String chatId);

    Optional<Seller> findByChatId(String chatId);
}
