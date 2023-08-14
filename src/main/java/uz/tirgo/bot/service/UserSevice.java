package uz.tirgo.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.bot.entity.Role;
import uz.tirgo.bot.entity.User;
import uz.tirgo.bot.entity.enums.Language;
import uz.tirgo.bot.entity.enums.RoleEnum;
import uz.tirgo.bot.repository.RoleRepository;
import uz.tirgo.bot.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSevice {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public boolean existsByChatId(String chatId) {
        return userRepository.existsByChatId(chatId);
    }

    public User finByChatId(String chatId) {
        return userRepository.findByChatId(chatId).get();
    }

    public String finByChatId(Long userId) {
        Optional<User> byId = userRepository.findById(userId);
        return byId.get().getChatId();
    }

    public void addUser(Message message) {
        User user = new User(message);
        Optional<Role> byName = roleRepository.findByName(RoleEnum.CUSTOMER);
        user.setRoles(byName.get());
        userRepository.save(user);
    }

    public User finById(Long supplierId) {
        Optional<User> byId = userRepository.findById(supplierId);
        return byId.get();
    }

    public void addUser(Message message, Boolean language) {
        User user = new User(message);
        Optional<Role> byName = roleRepository.findByName(RoleEnum.CUSTOMER);
        user.setRoles(byName.get());
        user.setLanguage(language ? Language.RUS : Language.UZB);
        userRepository.save(user);
    }

    public void editUser(Message message, boolean language) {
        Optional<User> byChatId = userRepository.findByChatId(String.valueOf(message.getChatId()));
        User user = byChatId.get();
        user.setLanguage(language ? Language.RUS : Language.UZB);
        userRepository.save(user);
    }

    public void editUser(Long chatId, Contact contact) {
        Optional<User> byChatId = userRepository.findByChatId(String.valueOf(chatId));
        User user = byChatId.get();
        user.setLastName(contact.getLastName());
        user.setFirstName(contact.getFirstName());
        user.setPhoneNumber(contact.getPhoneNumber());
        userRepository.save(user);
    }

    public boolean getLanguage(Long chatId) {
        Optional<User> byChatId = userRepository.findByChatId(String.valueOf(chatId));
        User user = byChatId.get();
        return user.getLanguage().equals(Language.RUS);
    }
}
