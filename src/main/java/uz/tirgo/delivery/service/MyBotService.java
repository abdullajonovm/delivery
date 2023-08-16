package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.tirgo.delivery.entity.*;
import uz.tirgo.delivery.entity.enums.Language;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.repository.LocationRepository;

import javax.management.openmbean.KeyAlreadyExistsException;
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

    public boolean currentLanguage;
    public String chatId = "";

    public boolean saveUser(Message message) {
        if (message.getChatId().equals(message.getContact().getUserId())) {
            userSevice.saveSeller(message);
            return true;
        }
        messageService.saveContact(message, orderService.findOverdueOrder(message.getChatId()));
        return false;
    }

    public List<SendMessage> myOrders(OrderStatus orderStatus) {
        List<SendMessage> sendMessageList = new ArrayList<>();
        for (Order order : orderService.getOrders(Long.valueOf(chatId), orderStatus)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            String text = "";
            Supplier supplier = order.getSupplier();

            if (currentLanguage) {
                text = text + "Заказ ид:" + order.getId() + ", заказ статус:" + order.getOrderStatus() + "\n";
                if (orderStatus.equals(OrderStatus.COMPLETE)) {
                    text = text + "Заказ доставлен" + "\n";
                    text = text + "Номер доставки заказа <<" + supplier.getPhoneNumber() + ">>, имя " + supplier.getLastName() + "\n";
                } else if (supplier != null && orderStatus.equals(OrderStatus.TAKING_AWAY)) {
                    text = text + "Номер доставки заказа <<" + supplier.getPhoneNumber() + ">>, имя " + supplier.getLastName() + "\n";
                } else {
                    text = text + "Ваш заказ в настоящее время не связан с поставщиком\n";
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText("Отмена заказа");
                    inlineKeyboardButton.setCallbackData("deletOrder/" + order.getId());
                    sendMessage.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(inlineKeyboardButton))));
                }
            } else {
                text = text + "Buyurtma id:" + order.getId() + ", buyurtma holati:" + order.getOrderStatus() + "\n";
                if (orderStatus.equals(OrderStatus.COMPLETE)) {
                    text = text + "Buyurtma yetkazib berilgan" + "\n";
                    text = text + "Buyurtma yetkazib beruvchi telefon raqami <<" + supplier.getPhoneNumber() + ">>, ismi " + supplier.getLastName() + "\n";
                } else if (supplier != null && orderStatus.equals(OrderStatus.TAKING_AWAY)) {
                    text = text + "Buyurtma yetkazib beruvchi telefon raqami <<" + supplier.getPhoneNumber() + ">>, ismi " + supplier.getLastName() + "\n";
                } else {
                    text = text + "Sizning buyurtmangiz hozircha yetkazib beruvchi bilan biriktirilmagan";
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText("Buyurtmani bekor qilish");
                    inlineKeyboardButton.setCallbackData("deletOrder/" + order.getId());
                    sendMessage.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(inlineKeyboardButton))));
                }
            }

            for (Messages messages : messageService.getMessages(order.getId())) {
                if (messages.getText() != null) {
                    text = text + "\n" + messages.getText();
                }
            }
            sendMessage.setText(text);
            sendMessageList.add(sendMessage);
        }


        return sendMessageList;
    }

    public SendMessage chekOrderSize(Message message) {
//        setCurrentLanguage(message.getChatId());

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

    public SendMessage newOrder() {
//        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (!userSevice.existsById(chatId)) {
            sendMessage.setText(this.currentLanguage ? KeyWords.NOT_FOUND_USER_RUS : KeyWords.NOT_FOUND_USER_UZB);
            return sendMessage;
        }
        orderService.createOrder(chatId);
        sendMessage.setText(this.currentLanguage ? KeyWords.SAVE_ORDER_RESPONSE_RUS : KeyWords.SAVE_ORDER_RESPONSE_UZB);
        return sendMessage;
    }


    public SendMessage location(Message message) {
        Order byChatId = orderService.findOverdueOrder(message.getChatId());
        Location location = new Location(message.getLocation());
        location = locationRepository.save(location);
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) != null && KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("inputSelerPoint()")) {
            orderService.setSellerPoint(message, location);
        } else if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) != null && KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("buyerPoint()")) {
            orderService.setBuyerPoint(message, location);
        } else {
            messageService.addLocation(message, byChatId, location);
            return new SendMessage(chatId, currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
        }
        return confirmationLocation();
    }

    public SendMessage confirmationLocation() {
        KeyboardButton keyboardButton = new KeyboardButton(currentLanguage ? KeyWords.CONFIRMATION_LOCATION_RUS : KeyWords.CONFIRMATION_LOCATION_UZB);
        KeyboardButton keyboardButton1 = new KeyboardButton(currentLanguage ? KeyWords.REENTER_CONFIRMATION_LOCATION_RUS : KeyWords.REENTER_CONFIRMATION_LOCATION_UZB);
        KeyboardButton keyboardButton2 = new KeyboardButton(currentLanguage ? KeyWords.CLOSE_ORDER_RUS : KeyWords.CLOSE_ORDER_UZB);

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(keyboardButton2);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRow1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage locationText() {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("inputSelerPoint()")) {
            return null;
        }
        SendMessage sendMessage = new SendMessage(chatId, currentLanguage ? "Пожалуйста, введите местоположение полностью и четко\n (проспект Амира Темура, 115)"
                : "Iltimos, manzilingizni to'liq va aniq kiriting\n (Amir Temur shoh ko'chasi, 115)");

        KeyboardButton keyboardButton = new KeyboardButton(currentLanguage ? KeyWords.CLOSE_ORDER_RUS : KeyWords.CLOSE_ORDER_UZB);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(Collections.singletonList(keyboardRow));
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "locationText()");
        return sendMessage;
    }

    private SendMessage locationText(Message message) {
        Location location = new Location(message.getText());
        locationRepository.save(location);
        KeyWords.lastRequestSeller.put(message.getChatId(), "locationText(Message message)");
        return confirmationLocation();
    }

    public SendMessage acceptedLocation(Message message) {
        String text = message.getText();
        if (KeyWords.lastRequestSeller.get(message.getChatId()) == null)
            return new SendMessage();
        else {
            boolean chek = text.equals(KeyWords.CONFIRMATION_LOCATION_RUS) || text.equals(KeyWords.CONFIRMATION_LOCATION_UZB);
            if (KeyWords.lastRequestSeller.get(message.getChatId()).equals("setSellerPoint(Message message, Location location)")) {
                if (chek) {
                    return null;
                } else if (text.equals(KeyWords.REENTER_CONFIRMATION_LOCATION_RUS) || text.equals(KeyWords.REENTER_CONFIRMATION_LOCATION_UZB)) {
                    if (KeyWords.lastRequestSeller.get(message.getChatId()).equals("buyerPoint()")) {
                        return null;
                    }
                    confirmationLocation();
                }
            } else if (KeyWords.lastRequestSeller.get(message.getChatId()).equals("setBuyerPoint(Message message, Location location)")) {
                if (chek) {
                    KeyboardRow keyboardRow = new KeyboardRow();

                    KeyboardButton keyboardButton = new KeyboardButton();
                    keyboardRow.add(keyboardButton);

                    KeyboardButton keyboardButton1 = new KeyboardButton();
                    keyboardRow.add(keyboardButton1);

                    keyboardButton.setText(currentLanguage ? KeyWords.SAVE_ORDER_RUS : KeyWords.SAVE_ORDER_UZB);
                    keyboardButton1.setText(currentLanguage ? KeyWords.CLOSE_ORDER_RUS : KeyWords.CLOSE_ORDER_UZB);


                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(this.currentLanguage ? KeyWords.NEW_ORDER_RESPONSE_RUS : KeyWords.NEW_ORDER_RESPONSE_UZB);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);
                    return sendMessage;
                }
            }
        }
        return new SendMessage();
    }

    public SendMessage contact(Message message) {
//        setCurrentLanguage(message.getChatId());
        Order order = orderService.findOverdueOrder(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        messageService.saveContact(message, order);
        sendMessage.setText(this.currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
        return sendMessage;
    }

    public List<SendMessage> myAllOrders(Message message) {
        List<SendMessage> sendMessageList = new ArrayList<>();
        for (Order order : orderService.myAllOrders(chatId)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            String text = "";
            Supplier supplier = order.getSupplier();
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


    public SendMessage text(Message message) {
        if (KeyWords.lastRequestSeller.get(message.getChatId()) != null && KeyWords.lastRequestSeller.get(message.getChatId()).equals("locationText()")) {
            return locationText(message);
        }
//        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        String chatId = String.valueOf(message.getChatId());
        sendMessage.setChatId(chatId);
        if (!userSevice.existsById(chatId)) {
            sendMessage.setText(currentLanguage ? KeyWords.NOT_FOUND_USER_RUS : KeyWords.NOT_FOUND_USER_UZB);
            return sendMessage;
        }

        Order order = orderService.findOverdueOrder(message.getChatId());

        if (messageService.save(message, order)) {
            sendMessage.setText(this.currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
        } else {
            sendMessage.setText("");
        }
        return sendMessage;
    }


    public SendMessage closeOrder(Message message) {
//        setCurrentLanguage(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        orderService.deletOverdueOrder(message.getChatId());
        sendMessage.setText(this.currentLanguage ? KeyWords.CLOSE_ORDER_RESPONSE_RUS : KeyWords.CLOSE_ORDER_RESPONSE_UZB);
        return sendMessage;
    }

    public SendMessage saveOrder(Message message) {
//        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        if (orderService.saveOrder(message))
            sendMessage.setText(this.currentLanguage ? KeyWords.SAVE_ORDER_RESPONSE_RUS : KeyWords.SAVE_ORDER_RESPONSE_UZB);
        else
            sendMessage.setText(this.currentLanguage ? KeyWords.NOT_FOUND_ORDER_RUS : KeyWords.NOT_FOUND_ORDER_UZB);
        return sendMessage;
    }


    public Boolean video(Message message, String path) {
        Order byChatId = orderService.findOverdueOrder(message.getChatId());
        if (byChatId == null)
            return false;

        messageService.save(message, byChatId, path);

        return true;
    }


    public void addPhotos(Message message, String path) {
        messageService.addPhoots(message, path, orderService.findOverdueOrder(message.getChatId()));
    }


    public SendMessage callbackQuery(CallbackQuery callbackQuery) {
        return callbackQueryService.callBackQuery(callbackQuery);
    }

    public SendMessage acceptedOrder(Message message) {
//        setCurrentLanguage(message.getChatId());

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


    public SendMessage acceptedSupplierOrder(Message message) {
//        setCurrentLanguage(message.getChatId());

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
        currentLanguage = KeyWords.userLanguage.get(chatId) != null ? KeyWords.userLanguage.get(chatId) : false;
    }


}
