package uz.tirgo.delivery.bot.customer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.bot.customer.service.SellerService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SellerBot extends TelegramLongPollingBot {
    @Autowired
    private SellerService sellerService;

//        private final String USER_NAME = "delivery_customer_bot";
//    private final String BOT_TOKEN = "6488104556:AAFbKt2CiNqhSqTYDkwiwA33Q-RofZiLflA";

    public String userName;
    public String botToken;
    private final String ADD_INFO = "Ma'lumot qo'shildi";

    private boolean currentLanguage = false;
    private String chatId = "";


    @Override
    public String getBotUsername() {
        return this.userName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

//        KeyWords.lastRequestSeller.forEach((key, value) -> System.out.println("id:" + key + "   value: " + value));

        Message message = update.getMessage();

//        System.out.println("message.getDate() = " + message.getDate());
//        Date date = new Date(message.getDate() * 1000L);
//        System.out.println("date = " + date);

        if (update.hasCallbackQuery()) {
            SendMessage sendMessage = sellerService.callbackQuery(update.getCallbackQuery());
            sendMessage(sendMessage);
        } else {
            chatId = String.valueOf(message.getChatId());
            if (KeyWords.userLanguage.get(message.getChatId()) != null && KeyWords.userLanguage.get(message.getChatId())) {
                currentLanguage = true;
            } else {
                currentLanguage = false;
            }
            sellerService.currentLanguage = currentLanguage;
            sellerService.chatId = chatId;
            if (message.hasText()) {

                if (message.getText().equals("/start")) {
                    start(message);
                } else {
                    text(message);
                }
            } else if (message.hasContact()) {
                contact(message);
            } else if (message.hasLocation()) {
                location(message);
            } else if (message.hasVideo()) {
                video(message);
                sendMessage(new SendMessage(String.valueOf(message.getChatId()), ADD_INFO));
            } else if (message.hasPhoto()) {
                photo(message);
                sendMessage(new SendMessage(String.valueOf(message.getChatId()), ADD_INFO));
            }
        }

    }


    private void photo(Message message) {
        String fileId = message.getPhoto().get(message.getPhoto().size() - 1).getFileId(); // Eng katta rasm
        String path = "src/main/resources/files/photos/" + fileId + ".jpg";
        uploadFile(fileId, path);
        sellerService.addPhotos(message, path);
    }

    public void uploadFile(String fileId, String path) {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            File file = execute(getFile);
            String filePath = file.getFilePath();

            // Form the download URL using the file_path
            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
//            System.out.println(path);
            // Download and save the video
            saveVideoToFile(fileUrl, path);

        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }

    private void video(Message message) {
        Video video = message.getVideo();
        String fileId = video.getFileId();

        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            File file = execute(getFile);
            String filePath = file.getFilePath();

            // Form the download URL using the file_path
            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
            String path = "src/main/resources/files/videos/" + video.getFileUniqueId() + ".mp4";
//            System.out.println(path);
            // Download and save the video
            saveVideoToFile(fileUrl, path);

            sellerService.video(message, path);
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }


    private void saveVideoToFile(String fileUrl, String fileName) throws IOException {
        try (InputStream inputStream = new URL(fileUrl).openStream();
             FileOutputStream outputStream = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

    }


    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            menu();
            throw new RuntimeException(e);
        }
    }


    public void text(Message message) {

        String text = message.getText();

        switch (text) {
            case KeyWords.LANGUAGE_RUS, KeyWords.LANGUAGE_UZB -> inputContat(message);
            case KeyWords.MY_ORDERS_RUS, KeyWords.MY_ORDERS_UZB -> myOrders();
            case KeyWords.NEW_ORDER_RUS, KeyWords.NEW_ORDER_UZB -> creteOrder(message);
            case KeyWords.MY_IN_PROGRESS_ORDERS_RUS, KeyWords.MY_TAKING_AWAY_ORDERS_RUS, KeyWords.MY_COMPLETE_ORDERS_RUS, KeyWords.MY_IN_PROGRESS_ORDERS_UZB, KeyWords.MY_TAKING_AWAY_ORDERS_UZB, KeyWords.MY_COMPLETE_ORDERS_UZB ->
                    myOrders(message);
            case KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_RUS, KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_UZB ->
                    inputSelerPoint();
            case KeyWords.INPUT_SELLER_LOCATION_MESSAGE_RUS, KeyWords.INPUT_SELLER_LOCATION_MESSAGE_UZB ->
                    locationText();
            case KeyWords.CONFIRMATION_LOCATION_RUS, KeyWords.CONFIRMATION_LOCATION_UZB, KeyWords.REENTER_CONFIRMATION_LOCATION_RUS, KeyWords.REENTER_CONFIRMATION_LOCATION_UZB ->
                    acceptedLocation(message);
            case KeyWords.CLOSE_ORDER_RUS, KeyWords.CLOSE_ORDER_UZB -> {
                SendMessage sendMessage = sellerService.closeOrder(message);
                sendMessage(sendMessage);
                menu();
            }
            case KeyWords.SAVE_ORDER_RUS, KeyWords.SAVE_ORDER_UZB -> {
                sendMessage(sellerService.calculetePrice(message));
                SendMessage sendMessage = sellerService.acceptedOrder(message);
                sendMessage(sendMessage);
            }
            case KeyWords.CONFIRMATION_ORDER_RUS, KeyWords.CONFIRMATION_ORDER_UZB -> {
                SendMessage sendMessage = sellerService.saveOrder(message);
                sendMessage(sendMessage);
                menu();
            }
            case KeyWords.GO_BACK_RUS, KeyWords.GO_BACK_UZB -> newOrder(String.valueOf(message.getChatId()));
            case KeyWords.CHEKING_STOP_ORDER_MESSAGE_RUS, KeyWords.CHEKING_STOP_ORDER_MESSAGE_UZB -> menu();
            case KeyWords.ACEPTED_ORDER_RUS, KeyWords.ACEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_RUS -> {
                SendMessage sendMessage = sellerService.acceptedSupplierOrder(message);
                sendMessage(sendMessage);
            }
            default -> sendMessage(sellerService.text(message));
        }

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

    // 1) /start bosilganda
    public void start(Message message) {
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "start(Message message)");
        sendMessage(new SendMessage(this.chatId, "Assalomu alaykum, botimizga hush kelibsiz) \n" +
                "Bizning tariflarimiz: \n" +
                "0-5km -> 10.000 so'm\n" +
                "5-10km -> 15.000 so'm\n" +
                "10-15km -> 20.000 so'm\n" +
                "15-25km -> 30.000 so'm\n" +
                "25-30km -> 40.000 so'm"));
        sendMessage(new SendMessage(this.chatId, "Здравствуйте и добро пожаловать в наш бот)\n" +
                "Наши тарифы:\n" +
                "0-5km -> 10.000 сум\n" +
                "5-10km -> 15.000 сум\n" +
                "10-15km -> 20.000 сум\n" +
                "15-25km -> 30.000 сум\n" +
                "25-30km -> 40.000 сум"));
        selectLanguage(message);
    }


    // 2)  tilni tanlash
    public void selectLanguage(Message message) {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("start(Message message)"))
            return;
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "selectLanguage(Message message)");

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
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("Tilni tanlang.\n Выберите язык");
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(sendMessage);
    }

    // 3) Contactni kiritish uchun button chiqarish
    public void inputContat(Message message) {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("selectLanguage(Message message)"))
            return;
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "inputContat(Message message)");

//        sellerning languageni saqlash
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
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(this.currentLanguage ? KeyWords.CONTACT_INPUT_RUS : KeyWords.CONTACT_INPUT_UZB);
        sendMessage(sendMessage);
    }

    // 4) Kontakt yuborilgan bo'lsa
    private void contact(Message message) {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("inputContat(Message message)"))
            return;
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "contact(Message message)");

        boolean saveSeller = sellerService.saveUser(message);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (saveSeller) {
            sendMessage.setText(currentLanguage ? KeyWords.HELLO_RUS : KeyWords.HELLO_UZB);
            sendMessage(sendMessage);
            menu();
        } else {
            sendMessage.setText(currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
            sendMessage(sendMessage);
        }
    }

    // 5) menu
    private void menu() {
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "menu()");

        KeyboardButton keyboardButton = new KeyboardButton();

        KeyboardButton keyboardButton1 = new KeyboardButton();

        if (currentLanguage) {
            keyboardButton.setText(KeyWords.MY_ORDERS_RUS);
            keyboardButton1.setText(KeyWords.NEW_ORDER_RUS);
        } else {
            keyboardButton.setText(KeyWords.MY_ORDERS_UZB);
            keyboardButton1.setText(KeyWords.NEW_ORDER_UZB);
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

    // 6) seller orders
    private void myOrders() {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("menu()"))
            return;
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "myOrders()");

        KeyboardButton keyboardButton = new KeyboardButton();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        KeyboardButton keyboardButton2 = new KeyboardButton();
        if (currentLanguage) {
            keyboardButton.setText(KeyWords.MY_IN_PROGRESS_ORDERS_RUS);
            keyboardButton1.setText(KeyWords.MY_TAKING_AWAY_ORDERS_RUS);
            keyboardButton2.setText(KeyWords.MY_COMPLETE_ORDERS_RUS);
        } else {
            keyboardButton.setText(KeyWords.MY_IN_PROGRESS_ORDERS_UZB);
            keyboardButton1.setText(KeyWords.MY_TAKING_AWAY_ORDERS_UZB);
            keyboardButton2.setText(KeyWords.MY_COMPLETE_ORDERS_UZB);
        }
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton);
        keyboardRow.add(keyboardButton1);
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(keyboardButton2);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        keyboardRowList.add(keyboardRow);
        keyboardRowList.add(keyboardRow1);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardMarkup(keyboardRowList, true, true, true, null));
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage(sendMessage);
    }

    // 6) 1) seller orders
    private void myOrders(Message message) {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("myOrders()"))
            return;
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "myOrders(Message message)");

        List<SendMessage> sendMessageList = new ArrayList<>();
        switch (message.getText()) {
            case KeyWords.MY_IN_PROGRESS_ORDERS_RUS, KeyWords.MY_IN_PROGRESS_ORDERS_UZB ->
                    sendMessageList = sellerService.myOrders(OrderStatus.IN_PROGRESS);
            case KeyWords.MY_TAKING_AWAY_ORDERS_RUS, KeyWords.MY_TAKING_AWAY_ORDERS_UZB ->
                    sendMessageList = sellerService.myOrders(OrderStatus.TAKING_AWAY);
            case KeyWords.MY_COMPLETE_ORDERS_RUS, KeyWords.MY_COMPLETE_ORDERS_UZB ->
                    sendMessageList = sellerService.myOrders(OrderStatus.COMPLETE);
        }
        if (sendMessageList.isEmpty())
            sendMessage(new SendMessage(chatId, currentLanguage ? KeyWords.NOT_FOUND_ORDER_RUS : KeyWords.NOT_FOUND_ORDER_UZB));
        sendMessageList.forEach(sendmessage -> sendMessage(sendmessage));
        menu();
    }

    // 7) buyurtma yaratish
    private void creteOrder(Message message) {
        if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("menu()"))
            return;
        SendMessage sendMessage = sellerService.chekOrderSize(message);
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "creteOrder(Message message)");
        sendMessage(sendMessage);
    }

    // 7) 1) yuk yuklanadigon joyning manzilini olish
    private void inputSelerPoint() {
        inputPoint(true);
    }

    // 7) 2) locatsiya jo'natsa saqlash
    private void location(Message message) {
        sendMessage(sellerService.location(message));
    }

    // 7) 2) 1) location ni text ko'rinishida yuborish
    private void locationText() {
        sendMessage(sellerService.locationText());
    }

    // 7) 3)
    private void acceptedLocation(Message message) {
        if (message.getText().equals(KeyWords.REENTER_CONFIRMATION_LOCATION_RUS) || message.getText().equals(KeyWords.REENTER_CONFIRMATION_LOCATION_UZB)) {
            reEntrLocation(message);
            return;
        }
        SendMessage sendMessage = sellerService.acceptedLocation(message);
        if (sendMessage == null)
            buyerPoint();
        else
            sendMessage(sendMessage);
    }

    // 7) 4)
    public void reEntrLocation(Message message) {
        boolean equals = KeyWords.lastRequestSeller.get(message.getChatId()).equals("setSellerPoint(Message message, Location location)");
        KeyWords.lastRequestSeller.put(message.getChatId(), "creteOrder(Message message)");
        sellerService.reEntrLocation(message.getChatId());
        inputPoint(equals);
    }

    // 8)
    public void buyerPoint() {
        inputPoint(false);
    }

    // {7, 8}
    public void inputPoint(boolean chek) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        String messageUz, messageRu;
        if (chek) {
            if (KeyWords.lastRequestSeller.get(Long.valueOf(chatId)) == null || !KeyWords.lastRequestSeller.get(Long.valueOf(chatId)).equals("creteOrder(Message message)"))
                return;
            KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "inputSelerPoint()");
            sellerService.newOrder();

            messageUz = "Yuk qayerdan olib ketiladi?";
            messageRu = "Откуда забрать?";
            KeyboardButton keyboardButton1 = new KeyboardButton(currentLanguage ? KeyWords.INPUT_SELLER_LOCATION_RUS : KeyWords.INPUT_SELLER_LOCATION_UZB);
            keyboardButton1.setRequestLocation(true);
            KeyboardRow keyboardRow1 = new KeyboardRow();
            keyboardRow1.add(keyboardButton1);
            keyboardRows.add(keyboardRow1);
        } else {
            KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "buyerPoint()");
            messageUz = "Qayerga yetkaziladi?";
            messageRu = "Куда доставить?";
        }


        KeyboardButton keyboardButton2 = new KeyboardButton(currentLanguage ? KeyWords.CLOSE_ORDER_RUS : KeyWords.CLOSE_ORDER_UZB);
        KeyboardRow keyboardRow2 = new KeyboardRow();

        keyboardRow2.add(keyboardButton2);

        keyboardRows.add(keyboardRow2);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(currentLanguage ? messageRu : messageUz);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage(sendMessage);
        SendVideo sendVideo = new SendVideo();
        File file = new File();
//        sendVideo.setVideo(new InputFile(new java.io.File("src/main/resources/video/video_2023-09-20_15-17-18.mp4")));
        sendVideo.setVideo(new InputFile("https://t.me/haisudhaihsdiuhsadiuh/2"));
        sendVideo.setChatId(chatId);
        sendVideo.setCaption(currentLanguage ? "Чтобы отправить адрес, вы можете отметить его на карте или переслать местоположение, отправленное в Telegram" : "Manzilni yuborishlik uchun xaritadan beligilashligingiz yoki telegram orqali yuborilgan lokatsiyani forward qilishligingiz mumkin");
        sendMessage(sendVideo);
    }

    private void sendMessage(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
