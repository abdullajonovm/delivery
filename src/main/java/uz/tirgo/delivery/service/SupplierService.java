package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.tirgo.delivery.bot.customer.config.SellerBot;
import uz.tirgo.delivery.bot.supplier.config.SupplierBot;
import uz.tirgo.delivery.entity.Contact;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Messages;
import uz.tirgo.delivery.entity.Seller;
import uz.tirgo.delivery.entity.enums.Language;
import uz.tirgo.delivery.payload.KeyWords;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SellerBot sellerBot;
    private final UserSevice userSevice;
    private final OrderService orderService;
    private final MessageService messageService;
    private final SupplierBot supplierBot;

    private Boolean language;

    public String sendOrder(Long supplierId, Long orderId) {
        var order = orderService.existOrderById(orderId);
        Seller seller = userSevice.finById(supplierId);

       language = KeyWords.supplierLanguage.get(supplierId) != null && KeyWords.supplierLanguage.get(supplierId);

        String supplierChatId = String.valueOf(supplierId);

        sellerBot.sendMessage(new SendMessage(supplierChatId, language ? KeyWords.CONFIRM_ORDER_RESPONSE_RUS : KeyWords.CONFIRM_ORDER_RESPONSE_UZB));
        if (!order)
            return "Order topilmadi";
        orderService.addSupplier(orderId, supplierId);
        for (Messages messages : messageService.getMessages(orderId)) {
            if (messages.getLocation() != null) {
                Location location = messages.getLocation();
                SendLocation sendLocation = new SendLocation();
                sendLocation.setChatId(supplierChatId);
                sendLocation.setHeading(location.getHeading());
                sendLocation.setLatitude(location.getLatitude());
                sendLocation.setHorizontalAccuracy(location.getHorizontalAccuracy());
                sendLocation.setLivePeriod(location.getLivePeriod());
                sendLocation.setProximityAlertRadius(location.getProximityAlertRadius());
                sendLocation.setLongitude(location.getLongitude());
                sellerBot.sendMessage(sendLocation);
            } else if (messages.getFileURL() != null) {
                sendFile(messages, supplierChatId);
            } else if (messages.getContact() != null) {
                Contact contact = messages.getContact();
                SendContact sendContact = new SendContact();
                sendContact.setChatId(supplierChatId);
                sendContact.setFirstName(contact.getFirstName());
                sendContact.setLastName(contact.getLastName());
                sendContact.setPhoneNumber(contact.getPhoneNumber());
                sellerBot.sendMessage(sendContact);
            } else {
                SendMessage sendMessage = new SendMessage(supplierChatId, messages.getText());
                sellerBot.sendMessage(sendMessage);
            }
        }

        selectMenyu(supplierChatId);
        return "Xabar yetkazib beruvchiga yuborildi";
    }

    private void sendFile(Messages messages, String supplierChatId) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(new InputFile(new File(messages.getFileURL())));
        sendDocument.setCaption(messages.getCaption());
        sendDocument.setChatId(supplierChatId);
        sellerBot.sendMessage(sendDocument);
    }

    private void selectMenyu(String chatId) {
        String acceptedOrder, dontAcceptedOrder;
        if (this.language) {
            acceptedOrder = KeyWords.ACEPTED_ORDER_RUS;
            dontAcceptedOrder = KeyWords.DONT_ACCEPTED_ORDER_RUS;
        } else {
            acceptedOrder = KeyWords.ACEPTED_ORDER_UZB;
            dontAcceptedOrder = KeyWords.DONT_ACCEPTED_ORDER_UZB;
        }

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardRow keyboardRow = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(acceptedOrder);
        keyboardRow.add(keyboardButton);

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(dontAcceptedOrder);
        keyboardRow.add(keyboardButton1);

        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText("qabul qilish");
        sellerBot.sendMessage(sendMessage);
    }

//    @Scheduled(fixedRate = 3600000)
    private void chekingBot() {
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        System.out.println("Bot ishlayabdi");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId("1542672167");
        sendMessage.setText("Bot ishlab turibdi");
        sellerBot.sendMessage(sendMessage);
        supplierBot.sendMessage(sendMessage);
    }
}

