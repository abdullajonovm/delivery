package uz.tirgo.bot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.bot.entity.Role;
import uz.tirgo.bot.entity.enums.RoleEnum;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
