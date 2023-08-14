package uz.tirgo.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.bot.entity.*;
import uz.tirgo.bot.repository.MessageRepository;
import uz.tirgo.bot.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private final ContactService contactService;

    public void save(Messages messages) {
        messageRepository.save(messages);
    }

    public boolean save(Message message, Order order) {
        String chatId = String.valueOf(message.getChatId());
        Optional<User> byChatId = userRepository.findByChatId(chatId);
        Messages messages = new Messages(order, message);
        messages.setFrom(byChatId.get());
        messageRepository.save(messages);

        return true;
    }

    public Boolean saveContact(Message message, Order order) {
        String chatId = String.valueOf(message.getChatId());
        Contact contact = contactService.addContect(message.getContact());
        Messages messages = new Messages();
        messages.setContact(contact);
        messages.setChatId(chatId);
        messages.setOrder(order);
        messageRepository.save(messages);
        return true;
    }

    public void save(Message message, Order order, String path) {
        String chatId = String.valueOf(message.getChatId());
        Optional<User> byChatId = userRepository.findByChatId(chatId);
        Messages messages = new Messages(order, message);
        messages.setFrom(byChatId.get());
        messages.setFileURL(path);
        messageRepository.save(messages);
    }

    public void addPhoots(Message message, String path, Order byChatId) {
        Messages messages = new Messages(byChatId, message);
        messages.setFileURL(path);
        messageRepository.save(messages);
    }

    public void addLocation(Message message, Order byChatId, Location location) {
        Messages messages = new Messages(byChatId, message);
        messages.setLocation(location);
        messageRepository.save(messages);
    }

    public List<Messages> getMessages(Long orderId) {
        return messageRepository.findAllByOrderId(orderId);
    }

    public void deleteMessages(Long id) {
        for (Messages messages : messageRepository.findAllByOrderId(id)) {
            messageRepository.deleteById(messages.getId());
        }
    }
}
