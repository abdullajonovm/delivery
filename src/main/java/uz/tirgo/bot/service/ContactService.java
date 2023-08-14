package uz.tirgo.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tirgo.bot.entity.Contact;
import uz.tirgo.bot.repository.ContactRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public Contact addContect(org.telegram.telegrambots.meta.api.objects.Contact contact) {
        Contact contact1;
        if (contactRepository.existsById(contact.getUserId())) {
            Optional<Contact> byId = contactRepository.findById(contact.getUserId());
            contact1 = byId.get();
        } else {
            contact1 = new Contact(contact);
            contact1 = contactRepository.save(contact1);
        }
        return contact1;
    }
}
