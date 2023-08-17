package uz.tirgo.delivery.bot.supplier.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.tirgo.delivery.bot.supplier.service.SupplierBotService;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Order;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.service.MyBotService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class SupplierBot extends TelegramLongPollingBot {
    public static final double RADIUS_OF_EARTH = 6371.0; // Earth's radius in kilometers

    private final MyBotService myBotService;
    private final SupplierBotService supplierBotService;
    //    private final String USER_NAME = "delivery_supplier_bot";
//    private final String BOT_TOKEN = "6529333260:AAGuB3DUDnNxeGTaveL1JZrvVyhkcIh3df4";
    private final String USER_NAME = "m_tirgo_bot";
    private final String BOT_TOKEN = "5947355802:AAHnbh6ZWwQAO8qYJb6IJ1BcWaa7azD3eqg";
    private final String ADD_INFO = "Ma'lumot qo'shildi";

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
            this.chatId = String.valueOf(message.getChatId());
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
        }
    }

    //TODO shuni yozishligim kerak

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
            TreeMap<Location, Double> locationDoubleTreeMap = new TreeMap<>();

            for (Order order : inprogressOrders) {
                Location sellerPoint = order.getSellerPoint();
                org.telegram.telegrambots.meta.api.objects.Location location = message.getLocation();
                if (sellerPoint.getLongitude() != null && sellerPoint.getLatitude() != null) {
                    double distance = calculateDistance(sellerPoint.getLatitude(), sellerPoint.getLongitude(), location.getLatitude(), location.getLongitude());

                }
            }
        }
    }

    private void hasContact(Message message) {
        if (!message.getContact().getUserId().equals(message.getChatId()))
            return;

        supplierBotService.start(message);
        menyu();
    }

    private void menyu() {
        KeyboardButton keyboardButton = new KeyboardButton();

        KeyboardButton keyboardButton1 = new KeyboardButton();
        if (this.currentLanguage) {
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
        sendMessage.setText(this.currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage(sendMessage);
    }


    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void inputContat(Message message) {
        currentLanguage = message.getText().equals(KeyWords.LANGUAGE_RUS);
        KeyWords.userLanguage.put(message.getChatId(), currentLanguage);

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

    public void text(Message message) {

        String text = message.getText();

        switch (text) {
//            Custmoerning buyurtmalarini ko'rish
            case KeyWords.MY_ORDERS_RUS, KeyWords.MY_ORDERS_UZB -> {
                myOrders();
            }
            case KeyWords.SUPPLIER_FINISHED_ORDER_RUS, KeyWords.SUPPLIER_FINISHED_ORDER_UZB -> {
                for (SendMessage sendMessage : supplierBotService.myFinishedOrders(chatId, currentLanguage)) {
                    sendMessage(sendMessage);
                }
                menyu();
            }
            case KeyWords.NEW_ORDER_SUPPLIER_UZB, KeyWords.NEW_ORDER_SUPPLIER_RUS -> {
                KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), KeyWords.SUBMIT_LOACTION_UZB);
                KeyboardRow keyboardRow = new KeyboardRow();
                KeyboardButton keyboardButton = new KeyboardButton();
                keyboardButton.setRequestLocation(true);
                keyboardButton.setText(currentLanguage ? KeyWords.SUBMIT_LOACTION_RUS : KeyWords.SUBMIT_LOACTION_UZB);
                keyboardRow.add(keyboardButton);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setReplyMarkup(new ReplyKeyboardMarkup(Collections.singletonList(keyboardRow)));
                sendMessage.setChatId(chatId);
                sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
                sendMessage(sendMessage);
            }
//            Yangi buyurtma yaratish
            case KeyWords.NEW_ORDER_RUS, KeyWords.NEW_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.chekOrderSize(message);
                sendMessage(sendMessage);
            }
//            Yaratayotgan buyurtmasini bekor qilish
            case KeyWords.CLOSE_ORDER_RUS, KeyWords.CLOSE_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.closeOrder(message);
                sendMessage(sendMessage);
                menyu();
            }
            case KeyWords.SAVE_ORDER_RUS, KeyWords.SAVE_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.acceptedOrder(message);
                sendMessage(sendMessage);
            }
            case KeyWords.CONFIRMATION_ORDER_RUS, KeyWords.CONFIRMATION_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.saveOrder(message);
                sendMessage(sendMessage);
                menyu();
            }
            case KeyWords.GO_BACK_RUS, KeyWords.GO_BACK_UZB -> {
                newOrder(this.chatId);
            }
            case KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_RUS, KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_UZB -> {
//                myBotService.newOrder(message);
                newOrder(this.chatId);
            }
            case KeyWords.CHEKING_STOP_ORDER_MESSAGE_RUS, KeyWords.CHEKING_STOP_ORDER_MESSAGE_UZB -> {
                menyu();
            }
            case KeyWords.ACEPTED_ORDER_RUS, KeyWords.ACEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_RUS -> {
                SendMessage sendMessage = myBotService.acceptedSupplierOrder(message);
                sendMessage(sendMessage);
            }
            default -> sendMessage(myBotService.text(message));
        }

    }


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


    private void newOrder(String chatId) {
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
        sendMessage(sendMessage);
    }


    public void sendMessage(SendLocation sendLocation) {
        try {
            execute(sendLocation);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(SendContact sendContact) {
        try {
            execute(sendContact);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendMessage(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void start(Message message) {
        sendMessage(new SendMessage(this.chatId, "Assalomu alaykum botimizga hush kelibsiz"));
        sendMessage(new SendMessage(this.chatId, "Привет и добро пожаловать в наш бот"));
        supplierBotService.start(message);
        selectLanguage(message);
    }

    public void selectLanguage(Message message) {
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
        sendMessage.setChatId(this.chatId);
        sendMessage.setText("Tilni tanlang.\n Выберите язык");
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(sendMessage);
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = RADIUS_OF_EARTH * c;
        return distance;
    }
}
