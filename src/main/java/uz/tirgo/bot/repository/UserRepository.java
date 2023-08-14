package uz.tirgo.bot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.bot.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByChatId(String chatId);

    Optional<User> findByChatId(String chatId);
}
