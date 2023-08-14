package uz.tirgo.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.bot.entity.AttachmentContent;

import java.util.Optional;

@Repository
public interface AttachmentContentRepository extends JpaRepository<AttachmentContent,Long> {


    Optional<AttachmentContent> findByAttachmentId(Long attachment_id);
}
