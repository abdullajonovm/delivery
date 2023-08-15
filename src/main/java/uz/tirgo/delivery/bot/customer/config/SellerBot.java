package uz.tirgo.delivery.bot.customer.config;

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
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.service.MyBotService;

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
    private MyBotService myBotService;
    private final String USER_NAME = "delivery_m_bot";
    private final String BOT_TOKEN = "6417165435:AAG9mWfwbWMRPJ160BnsLxAzZlq2GbvHGp8";
    //   private final String USER_NAME = "tirgo_muhammadqodir_bot";
//    private final String BOT_TOKEN = "6256000891:AAEcDEc3hwesGVQVN-ZPalbYv0QuuFq4XSw";
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

        Message message = update.getMessage();
        if (update.hasCallbackQuery()) {
            SendMessage sendMessage = myBotService.callbackQuery(update.getCallbackQuery());
            sendMessage(sendMessage);
        } else {
            chatId = String.valueOf(message.getChatId());
            if (KeyWords.userLanguage.get(message.getChatId()) != null && KeyWords.userLanguage.get(message.getChatId())) {
                currentLanguage = true;
            }
//            currentLanguage(String.valueOf(message.getChatId()));
            if (message.hasText()) {

                if (message.getText().equals("/start")) {
                    start(message);
                } else if (message.getText().equals(KeyWords.LANGUAGE_RUS) || message.getText().equals(KeyWords.LANGUAGE_UZB)) {
                } else {
                    text(message);
                }
            } else if (message.hasContact()) {
                SendMessage sendMessage1 = myBotService.saveUser(message);
                if (sendMessage1 == null) {
                    sendMessage1 = new SendMessage();
                    sendMessage1.setChatId(String.valueOf(message.getChatId()));
                    sendMessage1.setText(this.currentLanguage ? KeyWords.ADD_INFO_RUS : KeyWords.ADD_INFO_UZB);
                    sendMessage(sendMessage1);
                } else {
                    sendMessage(sendMessage1);
                    menyu(String.valueOf(message.getChatId()));
                }
            } else if (message.hasVideo()) {

                video(message);
                sendMessage(new SendMessage(String.valueOf(message.getChatId()), ADD_INFO));
            } else if (message.hasPhoto()) {
                photo(message);
                sendMessage(new SendMessage(String.valueOf(message.getChatId()), ADD_INFO));
            } else if (message.hasLocation()) {
                myBotService.location(message);
                sendMessage(new SendMessage(String.valueOf(message.getChatId()), ADD_INFO));
            }
        }

    }

    private void photo(Message message) {

        String path = "";
        for (PhotoSize photoSize : message.getPhoto()) {
            String fileId1 = photoSize.getFileUniqueId();
            if (fileId1.lastIndexOf("-") != fileId1.length() - 1)
                continue;

            String fileId = photoSize.getFileId();
            path = "src/main/resources/files/photos/" + photoSize.getFileUniqueId() + ".jpg";
            uploadFile(fileId, path);
        }
        myBotService.addPhotos(message, path);


    }

    public void uploadFile(String fileId, String path) {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            File file = execute(getFile);
            String filePath = file.getFilePath();

            // Form the download URL using the file_path
            String fileUrl = "https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath;
            System.out.println(path);
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
            String fileUrl = "https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath;
            String path = "src/main/resources/files/videos/" + video.getFileUniqueId() + ".mp4";
            System.out.println(path);
            // Download and save the video
            saveVideoToFile(fileUrl, path);

            myBotService.video(message, path);
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }

    private void menyu(String chatId) {
        KeyboardButton keyboardButton = new KeyboardButton();

        KeyboardButton keyboardButton1 = new KeyboardButton();
        if (this.currentLanguage) {
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
        sendMessage.setText(this.currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        sendMessage(sendMessage);
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


    private void sendDocUploadingAFile(Long chatId, java.io.File save, String caption) throws TelegramApiException {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(save);

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(String.valueOf(chatId));
        sendDocumentRequest.setDocument(inputFile);
        sendDocumentRequest.setCaption(caption);
        execute(sendDocumentRequest);
    }


    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void text(Message message) {

        String text = message.getText();

        switch (text) {
//            tilni kiritgandan so'ng
            case KeyWords.LANGUAGE_RUS, KeyWords.LANGUAGE_UZB -> inputContat(message);

//            Custmoerning buyurtmalarini ko'rish
            case KeyWords.MY_ORDERS_RUS, KeyWords.MY_ORDERS_UZB -> {
                for (SendMessage sendMessage : myBotService.myAllOrders(message)) {
                    sendMessage(sendMessage);
                }
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
                menyu(String.valueOf(message.getChatId()));
            }
            case KeyWords.SAVE_ORDER_RUS, KeyWords.SAVE_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.acceptedOrder(message);
                sendMessage(sendMessage);
            }
            case KeyWords.CONFIRMATION_ORDER_RUS, KeyWords.CONFIRMATION_ORDER_UZB -> {
                SendMessage sendMessage = myBotService.saveOrder(message);
                sendMessage(sendMessage);
                menyu(String.valueOf(message.getChatId()));
            }
            case KeyWords.GO_BACK_RUS, KeyWords.GO_BACK_UZB -> {
                newOrder(String.valueOf(message.getChatId()));
            }
            case KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_RUS, KeyWords.CHEKING_COUNTINUE_ORDER_MESSAGE_UZB -> {
                inputSelerPoint();
                myBotService.newOrder(message);
//                newOrder(String.valueOf(message.getChatId()));

            }
            case KeyWords.CHEKING_STOP_ORDER_MESSAGE_RUS, KeyWords.CHEKING_STOP_ORDER_MESSAGE_UZB -> {
                menyu(String.valueOf(message.getChatId()));
            }
            case KeyWords.ACEPTED_ORDER_RUS, KeyWords.ACEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_UZB, KeyWords.DONT_ACCEPTED_ORDER_RUS -> {
                SendMessage sendMessage = myBotService.acceptedSupplierOrder(message);
                sendMessage(sendMessage);
            }
            default -> sendMessage(myBotService.text(message));
        }

    }

    private void inputSelerPoint() {

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(currentLanguage ? KeyWords.INPUT_SELLER_LOCATION_MESSAGE_RUS : KeyWords.INPUT_SELLER_LOCATION_MESSAGE_UZB);

        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), "INPUT_SELLER_LOCATION");

        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(currentLanguage ? "Нажмите на кнопку или поделитесь своим местоположением, войдя в меню телеграммы \uD83D\uDD79" : "Tugmaning ustiga bosing yoki, telegram menyusiga kirib joylashuvingizni ulashing \uD83D\uDD79");
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardRow keyboardRow1 = new KeyboardRow();

        keyboardRow.add(keyboardButton);
        keyboardRow1.add(keyboardButton1);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRow1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(currentLanguage ? "Укажите место, где поставщик должен забрать заказ" : "Yetkazib beruvchi buyurtmani olishi kerak bo'lgan joyni belgilang");
        sendMessage.setChatId(chatId);
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

    // 1) /start bosilganda
    public void start(Message message) {
        sendMessage(new SendMessage(String.valueOf(message.getChatId()), "Assalomu alaykum botimizga hush kelibsiz"));
        sendMessage(new SendMessage(String.valueOf(message.getChatId()), "Привет и добро пожаловать в наш бот"));
        selectLanguage(message);
    }


    // 2)  tilni tanlash
    public void selectLanguage(Message message) {

        KeyWords.lastRequestSupplier.put(Long.valueOf(chatId), KeyWords.LANGUAGE_UZB);

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
        currentLanguage = message.getText().equals(KeyWords.LANGUAGE_RUS);
//        sellerning javob qaytadigon tilni saqlash
        KeyWords.userLanguage.put(message.getChatId(), currentLanguage);
        KeyWords.lastRequestSeller.put(Long.valueOf(chatId), KeyWords.CONTACT_INPUT_UZB);

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
}
