package uz.tirgo.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tirgo.bot.entity.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    boolean existsById(Long id);
}
