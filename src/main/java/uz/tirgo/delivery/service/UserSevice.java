package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.Role;
import uz.tirgo.delivery.entity.Seller;
import uz.tirgo.delivery.entity.enums.Language;
import uz.tirgo.delivery.entity.enums.RoleEnum;
import uz.tirgo.delivery.repository.RoleRepository;
import uz.tirgo.delivery.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSevice {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public boolean existsByChatId(String chatId) {
        return userRepository.existsByChatId(chatId);
    }

    public Seller finByChatId(String chatId) {
        return userRepository.findByChatId(chatId).get();
    }

    public String finByChatId(Long userId) {
        Optional<Seller> byId = userRepository.findById(userId);
        return byId.get().getChatId();
    }

    public void addUser(Message message) {
        Seller seller = new Seller(message);
        Optional<Role> byName = roleRepository.findByName(RoleEnum.CUSTOMER);
        seller.setRoles(byName.get());
        userRepository.save(seller);
    }

    public Seller finById(Long supplierId) {
        Optional<Seller> byId = userRepository.findById(supplierId);
        return byId.get();
    }

    public void addUser(Message message, Boolean language) {
        Seller seller = new Seller(message);
        Optional<Role> byName = roleRepository.findByName(RoleEnum.CUSTOMER);
        seller.setRoles(byName.get());
        seller.setLanguage(language ? Language.RUS : Language.UZB);
        userRepository.save(seller);
    }

    public void editUser(Message message, boolean language) {
        Optional<Seller> byChatId = userRepository.findByChatId(String.valueOf(message.getChatId()));
        Seller seller = byChatId.get();
        seller.setLanguage(language ? Language.RUS : Language.UZB);
        userRepository.save(seller);
    }

    public void editUser(Long chatId, Contact contact) {
        Optional<Seller> byChatId = userRepository.findByChatId(String.valueOf(chatId));
        Seller seller = byChatId.get();
        seller.setLastName(contact.getLastName());
        seller.setFirstName(contact.getFirstName());
        seller.setPhoneNumber(contact.getPhoneNumber());
        userRepository.save(seller);
    }

    public boolean getLanguage(Long chatId) {
        Optional<Seller> byChatId = userRepository.findByChatId(String.valueOf(chatId));
        Seller seller = byChatId.get();
        return seller.getLanguage().equals(Language.RUS);
    }
}
