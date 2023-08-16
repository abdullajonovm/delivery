package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.*;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.repository.MessageRepository;
import uz.tirgo.delivery.repository.SellerRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final SellerRepository sellerRepository;

    private final ContactService contactService;


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

    public void save(Messages messages) {
        messageRepository.save(messages);
    }


    public boolean save(Message message, Order order) {
        Messages messages = new Messages(order, message);
        messages.setFrom(sellerRepository.findById(message.getChatId()).get());
        messageRepository.save(messages);
        return true;
    }


    public void save(Message message, Order order, String path) {
        Messages messages = new Messages(order, message);
        messages.setFrom(sellerRepository.findById(message.getChatId()).get());
        messages.setFileURL(path);
        messageRepository.save(messages);
    }

    public void addPhoots(Message message, String path, Order byChatId) {
        Messages messages = new Messages(byChatId, message);
        messages.setFileURL(path);
        messageRepository.save(messages);
    }

    public void addLocation(Message message, Order byChatId, Location location) {
        KeyWords.lastRequestSeller.put(message.getChatId(), "addLocation(Message message, Order byChatId, Location location)");
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
