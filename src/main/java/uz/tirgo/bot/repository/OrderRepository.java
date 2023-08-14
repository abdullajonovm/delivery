package uz.tirgo.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.bot.entity.Order;
import uz.tirgo.bot.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCustomerChatId(String customer_chatId);
    Optional<Order> findBySupplierChatId(String supplier_chatId);

    Optional<Order> findByCustomerChatId(String customer_chatId);

    Optional<Order> findByCustomerChatIdAndOrderStatus(String customer_chatId, OrderStatus orderStatus);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

//    @Query(value = "DELETE FROM orders WHERE customer_id = :customerId and order_status = 'OVERDUE'", nativeQuery = true)
    void deleteAllByOrderStatusAndCustomerChatId(OrderStatus orderStatus, String customerId);

    List<Order> findAllBySupplierChatId(String valueOf);
}
