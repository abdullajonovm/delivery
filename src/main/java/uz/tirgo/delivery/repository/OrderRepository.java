package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.delivery.entity.Order;
import uz.tirgo.delivery.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCustomerId(Long customer_id);

    List<Order> findAllByOrderStatusAndCustomerId(OrderStatus orderStatus, Long customer_id);

    List<Order> findAllBySupplierIdAndOrderStatus(Long supplier_id, OrderStatus orderStatus);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findAllBySupplierId(Long supplier_id);
}
