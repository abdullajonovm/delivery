package uz.tirgo.delivery.bot.supplier.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.tirgo.delivery.bot.supplier.service.SupplierBotService;
import uz.tirgo.delivery.entity.Order;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplierBot extends TelegramLongPollingBot {
    private final SupplierBotService supplierBotService;
    private final String USER_NAME = "delivery_supplier_bot";
    private final String BOT_TOKEN = "6529333260:AAGuB3DUDnNxeGTaveL1JZrvVyhkcIh3df4";
    //    private final String USER_NAME = "m_tirgo_bot";
//    private final String BOT_TOKEN = "5947355802:AAHnbh6ZWwQAO8qYJb6IJ1BcWaa7azD3eqg";
    private boolean currentLanguage = false;

    private String chatId = "";


    @Override
    public String getBotUsername() {
        return this.USER_NAME;
    }

    @Override
    public String getBotToken() {
        return this.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            chatId = String.valueOf(message.getChatId());
            supplierBotService.chatId = chatId;
//            System.out.println("KeyWords.supplierLanguage.get(message.getChatId()) = " + KeyWords.supplierLanguage.get(message.getChatId()));
            currentLanguage = (KeyWords.supplierLanguage.get(message.getChatId()) != null) ? KeyWords.supplierLanguage.get(message.getChatId()) : false;
            supplierBotService.currentLanguage = currentLanguage;

            if (message.hasText()) {
                if (message.getText().equals("/start"))
                    start(message);
                else if (message.getText().equals(KeyWords.LANGUAGE_RUS) || message.getText().equals(KeyWords.LANGUAGE_UZB))
                    inputContat(message);
                else
                    text(message);
            } else if (message.hasContact()) {
                hasContact(message);
            } else if (message.hasLocation()) {
                location(message);
            }
        } else if (update.hasCallbackQuery()) {
            cllbackQuery(update.getCallbackQuery());
        } else if (update.hasEditedMessage()) {
            if (update.getEditedMessage().hasLocation()) {
                supplierBotService.editSupplierLocation(update.getEditedMessage());
            }
        }
    }

    private void cllbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long orderId = null;
        if (data.startsWith("orderId/")) {
            orderId = Long.valueOf(data.replace("orderId/", ""));

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            SendLocation sendLocation;

            sendLocation = supplierBotService.getSellerLocation(orderId);
            if (sendLocation.getChatId() != null) {
                sendMessage.setText((currentLanguage ? "Место загрузки" : "Yuk yuklanadigon joy") + "\n" + sendLocation.getChatId());
                sendMessage(sendMessage);
            } else {
                sendMessage.setText(currentLanguage ? "Место загрузки" : "Yuk yuklanadigon joy");
                sendMessage(sendMessage);
                sendLocation.setChatId(chatId);
                sendMessage(sendLocation);
            }

            sendLocation = supplierBotService.getBuyerLocation(orderId);
            if (sendLocation.getChatId() != null) {
                sendMessage.setText((currentLanguage ? "Место выгрузки" : "Yuk tushiriladigon joy") + "\n" + sendLocation.getChatId());
                sendMessage(sendMessage);
            } else {
                sendMessage.setText(currentLanguage ? "Место выгрузки" : "Yuk tushiriladigon joy");
                sendLocation.setChatId(chatId);
                sendMessage(sendMessage);
                sendMessage(sendLocation);
            }

            sendMessage(supplierBotService.getMessage(orderId));
        } else if (data.startsWith("acceptOrder/")) {
            orderId = Long.valueOf(data.replace("acceptOrder/", ""));
            SendMessage sendMessage = supplierBotService.accepedOrder(orderId);
            sendMessage(sendMessage);
        } else if (data.startsWith("complateOrderId/")) {
            orderId = Long.valueOf(data.replace("complateOrderId/", ""));
            SendMessage sendMessage = supplierBotService.complateOrder(orderId);
            sendMessage(sendMessage);
            menyu();
        }
    }

    // 1)
    public void start(Message message) {
        sendMessage(new SendMessage(this.chatId, "Assalomu alaykum botimizga hush kelibsiz"));
        sendMessage(new SendMessage(this.chatId, "Привет и добро пожаловать в наш бот"));
        supplierBotService.start(message);
        selectLanguage();
        KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), "start(Message message)");
    }

    // 2)
    public void selectLanguage() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(KeyWords.LANGUAGE_UZB);

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(KeyWords.LANGUAGE_RUS);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Tilni tanlang.\n Выберите язык");
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(sendMessage);
        KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), "selectLanguage()");
    }

    // 3)
    public void inputContat(Message message) {
        currentLanguage = message.getText().equals(KeyWords.LANGUAGE_RUS);
        KeyWords.supplierLanguage.put(message.getChatId(), currentLanguage);

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText(currentLanguage ? KeyWords.CONTACT_INPUT_RUS : KeyWords.CONTACT_INPUT_UZB);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(this.chatId);
        sendMessage.setText(currentLanguage ? KeyWords.CONTACT_INPUT_RUS : KeyWords.CONTACT_INPUT_UZB);
        sendMessage(sendMessage);
    }


    // 4)
    private void hasContact(Message message) {
        if (!message.getContact().getUserId().equals(message.getChatId()))
            return;
        supplierBotService.start(message);
        menyu();
        KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), "hasContact(Message message)");
    }

    // 5)
    private void menyu() {
        KeyboardButton keyboardButton = new KeyboardButton();

        KeyboardButton keyboardButton1 = new KeyboardButton();
        if (currentLanguage) {
            keyboardButton.setText(KeyWords.MY_ORDERS_RUS);
            keyboardButton1.setText(KeyWords.NEW_ORDER_SUPPLIER_RUS);
        } else {
            keyboardButton.setText(KeyWords.MY_ORDERS_UZB);
            keyboardButton1.setText(KeyWords.NEW_ORDER_SUPPLIER_UZB);
        }


        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage(sendMessage);
    }

    // 6)
    private void myOrders() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(currentLanguage ? KeyWords.SUPPLIER_FINISHED_ORDER_RUS : KeyWords.SUPPLIER_FINISHED_ORDER_UZB);

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(currentLanguage ? KeyWords.SUPPLIER_INPROGRESS_ORDER_RUS : KeyWords.SUPPLIER_INPROGRESS_ORDER_UZB);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(sendMessage);
    }

    // 7)
    private void myOrders(Message message) {
        boolean chek = message.getText().equals(KeyWords.SUPPLIER_INPROGRESS_ORDER_UZB) || message.getText().equals(KeyWords.SUPPLIER_INPROGRESS_ORDER_RUS);
        List<SendMessage> sendMessageList;
        if (chek) {
            sendMessageList = supplierBotService.supllerOrders(OrderStatus.TAKING_AWAY);
        } else {
            sendMessageList = supplierBotService.supllerOrders(OrderStatus.COMPLETE);
        }
        for (SendMessage sendMessage : sendMessageList) {
            sendMessage(sendMessage);
        }
        menyu();
    }


    // 8)
    public void requestOrder() {
        KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), KeyWords.SUBMIT_LOACTION_UZB);
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestLocation(true);
        keyboardButton.setText(currentLanguage ? KeyWords.SUBMIT_LOACTION_RUS : KeyWords.SUBMIT_LOACTION_UZB);
        keyboardRow.add(keyboardButton);
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(Collections.singletonList(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(chatId);
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage(sendMessage);
    }

    private void location(Message message) {
        if (KeyWords.lastRequestSupplier.get(message.getChatId()).equals(KeyWords.SUBMIT_LOACTION_UZB) || KeyWords.lastRequestSupplier.get(message.getChatId()).equals(KeyWords.SUBMIT_LOACTION_RUS)) {
            List<Order> inprogressOrders = supplierBotService.getInprogressOrders();
            if (inprogressOrders.isEmpty()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(currentLanguage ? "На данный момент нет доступных заказов" : "Hozirda buyurtmalar mavjud emas");
                sendMessage.setChatId(String.valueOf(message.getChatId()));
                sendMessage(sendMessage);
                return;
            }
            for (SendMessage order : supplierBotService.getOrders(message)) {
                sendMessage(order);
            }
        }
        menyu();
    }

    public void text(Message message) {
        String text = message.getText();
        switch (text) {
            case KeyWords.ACEPTED_ORDER_RUS, KeyWords.ACEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_RUS -> {
                SendMessage sendMessage = supplierBotService.acceptedSupplierOrder(message);
                sendMessage(sendMessage);
                menyu();
            }
            case KeyWords.MY_ORDERS_RUS, KeyWords.MY_ORDERS_UZB -> myOrders();
            case KeyWords.SUPPLIER_FINISHED_ORDER_RUS, KeyWords.SUPPLIER_FINISHED_ORDER_UZB, KeyWords.SUPPLIER_INPROGRESS_ORDER_UZB, KeyWords.SUPPLIER_INPROGRESS_ORDER_RUS ->
                    myOrders(message);
            case KeyWords.NEW_ORDER_SUPPLIER_UZB, KeyWords.NEW_ORDER_SUPPLIER_RUS -> requestOrder();
            case KeyWords.CHEKING_STOP_ORDER_MESSAGE_RUS, KeyWords.CHEKING_STOP_ORDER_MESSAGE_UZB -> menyu();
        }
    }


    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(SendLocation sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}
