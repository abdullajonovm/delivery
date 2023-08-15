package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tirgo.delivery.entity.Messages;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findAllByOrderId(Long order_id);

    void deleteMessagesByOrderId(Long order_id);
}