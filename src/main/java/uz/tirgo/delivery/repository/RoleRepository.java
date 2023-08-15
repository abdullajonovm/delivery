package uz.tirgo.delivery.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.delivery.entity.Role;
import uz.tirgo.delivery.entity.enums.RoleEnum;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
