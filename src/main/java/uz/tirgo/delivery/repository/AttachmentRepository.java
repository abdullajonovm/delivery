package uz.tirgo.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.delivery.entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
}
