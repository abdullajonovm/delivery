package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.delivery.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
