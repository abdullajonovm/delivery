package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Messages;
import uz.tirgo.delivery.entity.Order;
import uz.tirgo.delivery.entity.Seller;
import uz.tirgo.delivery.entity.enums.Language;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.repository.LocationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyBotService {

    private final CallbackQueryService callbackQueryService;

    private final OrderService orderService;


    private final MessageService messageService;

    private final UserSevice userSevice;


    private final LocationRepository locationRepository;

    public boolean currentLanguage = false;

    public boolean saveUser(Message message) {
        if (message.getChatId().equals(message.getContact().getUserId())) {
            userSevice.addUser(message);
            return true;
        }
        messageService.saveContact(message, orderService.findByChatId(String.valueOf(message.getChatId())));
        return false;
    }


    public SendMessage contact(Message message) {
        setCurrentLanguage(message.getChatId());
        Order order = orderService.findByChatId(String.valueOf(message.getChatId()));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        messageService.saveContact(message, order);
        sendMessage.setText(this.currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
        return sendMessage;
    }

    public List<SendMessage> myAllOrders(Message message) {
        setCurrentLanguage(message.getChatId());
        String chatId = String.valueOf(message.getChatId());
        List<SendMessage> sendMessageList = new ArrayList<>();
        for (Order order : orderService.myAllOrders(chatId)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            String text = "";
            Seller supplier = order.getSupplier();
            if (this.currentLanguage) {
                text = text + "Заказ ид:" + order.getId() + ", заказ статус:" + order.getOrderStatus() + "\n";
                if (supplier != null && order.getOrderStatus().equals(OrderStatus.TAKING_AWAY)) {
                    text = text + "Номер доставки заказа <<" + supplier.getPhoneNumber() + ">>, имя " + supplier.getLastName() + "\n";
                } else {
                    text = text + "Ваш заказ в настоящее время не связан с поставщиком\n";
                }
            } else {
                text = text + "Buyurtma id:" + order.getId() + ", buyurtma holati:" + order.getOrderStatus() + "\n";
                if (supplier != null && order.getOrderStatus().equals(OrderStatus.TAKING_AWAY)) {
                    text = text + "Buyurtma yetkazib beruvchi telefon raqami <<" + supplier.getPhoneNumber() + ">>, ismi " + supplier.getLastName() + "\n";
                } else {
                    text = text + "Sizning buyurtmangiz hozircha yetkazib beruvchi bilan biriktirilmagan";
                }
            }

            for (Messages messages : messageService.getMessages(order.getId())) {
                if (message.getText() != null) {
                    text = text + "\n" + messages.getText();
                }
            }
            sendMessage.setText(text);
            sendMessageList.add(sendMessage);
        }

        return sendMessageList;
    }

    public SendMessage newOrder(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (!userSevice.existsByChatId(String.valueOf(message.getChatId()))) {
            sendMessage.setText(this.currentLanguage ? KeyWords.NOT_FOUND_USER_RUS : KeyWords.NOT_FOUND_USER_UZB);
            return sendMessage;
        }
        orderService.createOrder(message);
        sendMessage.setText(this.currentLanguage ? KeyWords.SAVE_ORDER_RESPONSE_RUS : KeyWords.SAVE_ORDER_RESPONSE_UZB);
        return sendMessage;
    }

    public SendMessage text(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        String chatId = String.valueOf(message.getChatId());
        sendMessage.setChatId(chatId);
        if (!userSevice.existsByChatId(chatId)) {
            sendMessage.setText(currentLanguage ? KeyWords.NOT_FOUND_USER_RUS : KeyWords.NOT_FOUND_USER_UZB);
            return sendMessage;
        }

        Order order = orderService.findByChatId(chatId);

        if (messageService.save(message, order)) {
            sendMessage.setText(this.currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
        } else {
            sendMessage.setText("");
        }
        return sendMessage;
    }

    public SendMessage closeOrder(Message message) {
        setCurrentLanguage(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        if (orderService.closeOrder(message))
            sendMessage.setText(this.currentLanguage ? KeyWords.CLOSE_ORDER_RESPONSE_RUS : KeyWords.CLOSE_ORDER_RESPONSE_UZB);
        else
            sendMessage.setText(this.currentLanguage ? KeyWords.NOT_FOUND_ORDER_RUS : KeyWords.NOT_FOUND_ORDER_UZB);
        return sendMessage;
    }

    public SendMessage saveOrder(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        if (orderService.saveOrder(message))
            sendMessage.setText(this.currentLanguage ? KeyWords.SAVE_ORDER_RESPONSE_RUS : KeyWords.SAVE_ORDER_RESPONSE_UZB);
        else
            sendMessage.setText(this.currentLanguage ? KeyWords.NOT_FOUND_ORDER_RUS : KeyWords.NOT_FOUND_ORDER_UZB);
        return sendMessage;
    }


    public Boolean video(Message message, String path) {
        Order byChatId = orderService.findByChatId(String.valueOf(message.getChatId()));
        if (byChatId == null)
            return false;

        messageService.save(message, byChatId, path);

        return true;
    }


    public void addPhotos(Message message, String path) {
        messageService.addPhoots(message, path, orderService.findByChatId(String.valueOf(message.getChatId())));
    }

    public void location(Message message) {
        Order byChatId = orderService.findByChatId(String.valueOf(message.getChatId()));
        Location location = new Location(message.getLocation());
        location = locationRepository.save(location);
        messageService.addLocation(message, byChatId, location);
        if (KeyWords.lastRequestSeller.get(message.getChatId()).equals("INPUT_SELLER_LOCATION")) {
            orderService.setSellerPoint(message, location);
        }
    }


    public SendMessage callbackQuery(CallbackQuery callbackQuery) {
        return callbackQueryService.callBackQuery(callbackQuery);
    }

    public SendMessage acceptedOrder(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(this.currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(this.currentLanguage ? KeyWords.CONFIRMATION_ORDER_RUS : KeyWords.CONFIRMATION_ORDER_UZB);

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(this.currentLanguage ? KeyWords.GO_BACK_RUS : KeyWords.GO_BACK_UZB);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(Collections.singletonList(keyboardRow));
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return sendMessage;
    }

    public SendMessage chekOrderSize(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(currentLanguage ? KeyWords.Warning_ORDER_RUS : KeyWords.Warning_ORDER_UZB);

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(currentLanguage ? KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_RUS : KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_UZB);

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(currentLanguage ? KeyWords.CHEKING_STOP_ORDER_MESSAGE_RUS : KeyWords.CHEKING_STOP_ORDER_MESSAGE_UZB);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(Collections.singletonList(keyboardRow));
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return sendMessage;
    }

    public SendMessage acceptedSupplierOrder(Message message) {
        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (orderService.acceptedSupplierOrder(message)) {
            sendMessage.setText(currentLanguage ? "Ваш заказ был размещен. Пожалуйста, доставьте заказ быстро и в хорошем качестве." : "Buyurtma sizga berildi. Iltimos byurtmani tez va sifatli yetkazib bering.");
        } else {
            sendMessage.setText(this.currentLanguage ? "Заказ Вам не был отправлен. Будьте более активны" : "Buyurtma sizga yo'naltirilmadi. Ilimos faolroq bo'ling");
        }
        return sendMessage;
    }

    public void setCurrentLanguage(Long chatId) {
        currentLanguage = KeyWords.userLanguage.get(chatId);
    }
}
