package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.delivery.entity.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {

}
