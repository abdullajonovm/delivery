package uz.tirgo.delivery.bot.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.tirgo.delivery.bot.customer.config.SellerBot;
import uz.tirgo.delivery.entity.*;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.service.MessageService;
import uz.tirgo.delivery.service.OrderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierBotService {

    private final SellerBot sellerBot;

    public static final double RADIUS_OF_EARTH = 6371.0; // Earth's radius in kilometers
    private final SuplierService suplierService;
    private final OrderService orderService;
    private final MessageService messageService;

    public String chatId;

    public boolean currentLanguage;

    public void start(Message message) {
        suplierService.saveUser(message);
    }


    public List<Order> getInprogressOrders() {
        return orderService.getAllOrders(OrderStatus.IN_PROGRESS);
    }

    public List<SendMessage> supllerOrders(OrderStatus status) {
        List<Order> orders = orderService.getOrdersSupplier(Long.valueOf(chatId), status);
        List<SendMessage> sendMessageList = new ArrayList<>();
        if (orders.isEmpty() || orders == null)
            return Collections.singletonList(new SendMessage(chatId, currentLanguage ? "У вас нет заказов" : "Buyurtmalaringiz mavjud emas"));
        for (Order order : orders) {
            SendMessage sendMessage = new SendMessage();
            String text = currentLanguage ? "Заказ ид:" + order.getId() + "\nИмя Клиента: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Телефон заказчика: " + order.getCustomer().getPhoneNumber()
                    : "Buyurtma id:" + order.getId() + "\nBuyurtmachining ismi: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Buyurtmachining telefon raqami: " + order.getCustomer().getPhoneNumber() + "\n";
            for (Messages message : messageService.getMessages(order.getId())) {
                if (message.getText() != null) {
                    text = text + "\n" + message.getText();
                }
            }
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            sendMessageList.add(sendMessage);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currentLanguage ? "Посмотреть местоположение" : "Joylashuvni ko'rish");
            button.setCallbackData("orderId/" + order.getId());

            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            buttons.add(Collections.singletonList(button));

            if (status.equals(OrderStatus.TAKING_AWAY)) {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(currentLanguage ? "Я успешно доставил заказ ✅" : "Men buyurtmani muvaffaqiyatli topshirdim ✅");
                inlineKeyboardButton.setCallbackData("complateOrderId/" + order.getId());
                buttons.add(Collections.singletonList(inlineKeyboardButton));
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);

            sendMessage.setReplyMarkup(inlineKeyboardMarkup);


        }
        return sendMessageList;
    }

    public SendLocation getSellerLocation(Long orderId) {
        Order order = orderService.getById(orderId);
        Location sellerPoint = order.getSellerPoint();
        SendLocation sendLocation = new SendLocation();
        if (sellerPoint.getName() != null) {
            sendLocation.setChatId(sellerPoint.getName());
        } else {
            sendLocation.setLongitude(sellerPoint.getLongitude());
            sendLocation.setLatitude(sellerPoint.getLatitude());
            sendLocation.setHorizontalAccuracy(sellerPoint.getHorizontalAccuracy());
        }
        return sendLocation;
    }

    public SendLocation getBuyerLocation(Long orderId) {
        Order order = orderService.getById(orderId);
        Location buyerPoint = order.getBuyerPoint();
        SendLocation sendLocation1 = new SendLocation();
        if (buyerPoint.getName() != null) {
            sendLocation1.setChatId(buyerPoint.getName());
        } else {
            sendLocation1.setLongitude(buyerPoint.getLongitude());
            sendLocation1.setLatitude(buyerPoint.getLatitude());
            sendLocation1.setHorizontalAccuracy(buyerPoint.getHorizontalAccuracy());
        }
        return sendLocation1;
    }

    public List<SendMessage> getOrders(Message message) {
        List<SendMessage> sendMessageList = new ArrayList<>();
        Order[] order = new Order[6];
        double min1 = 0, min2 = 0, min3 = 0;
        List<Order> inprogressOrders = getInprogressOrders();
        List<Order> orderList = new ArrayList<>();
        if (inprogressOrders.size() > 3) {
            for (Order inprogressOrder : inprogressOrders) {
                Location sellerPoint = inprogressOrder.getSellerPoint();
                if (sellerPoint.getLongitude() != null && sellerPoint.getLatitude() != null) {
                    double distance = calculateDistance(sellerPoint.getLatitude(), sellerPoint.getLongitude(), message.getLocation().getLatitude(), message.getLocation().getLongitude());
//                    System.out.println("distance = " + distance);
                    if (distance > 5)
                        continue;
                    if (distance > min1) {
                        min3 = min2;
                        min2 = min1;
                        min1 = distance;
                        order[2] = order[1];
                        order[1] = order[0];
                        order[0] = inprogressOrder;
                    } else if (distance > min2 || distance == min1) {
                        min3 = min2;
                        min2 = distance;
                        order[2] = order[1];
                        order[1] = inprogressOrder;
                    } else if (distance > min3 || distance == min2) {
                        min3 = distance;
                        order[2] = inprogressOrder;
                    }
                } else {
                    orderList.add(inprogressOrder);
                }
            }
        } else {
            for (Order inprogressOrder : inprogressOrders) {
                orderList.add(inprogressOrder);
                if (inprogressOrder != null)
                    sendMessageList.add(getMessage(inprogressOrder));
            }
            return sendMessageList;
        }

        for (Order order1 : order) {
            if (order1 != null)
                sendMessageList.add(getMessage(order1));
        }
        for (Order order1 : orderList) {
            if (order1 != null)
                sendMessageList.add(getMessage(order1));
        }
        return sendMessageList;
    }

    public SendMessage getMessage(Order order) {
        SendMessage sendMessage = new SendMessage();
        String text = currentLanguage ? "Заказ ид:" + order.getId() + "\nИмя Клиента: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Телефон заказчика: " + order.getCustomer().getPhoneNumber()
                : "Buyurtma id:" + order.getId() + "\nBuyurtmachining ismi: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Buyurtmachining telefon raqami: " + order.getCustomer().getPhoneNumber() + "\n";
        for (Messages message : messageService.getMessages(order.getId())) {
            if (message.getText() != null) {
                text = text + "\n" + message.getText();
            }
        }
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        List<List<InlineKeyboardButton>> inlieniKeybord = new ArrayList<>();
        if (order.getSellerPoint().getName() != null) {
            sendMessage.setText(text + "\n" + (currentLanguage ? "Адрес: " : "Manzil: ") + order.getSellerPoint().getName());
        } else {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currentLanguage ? "Посмотреть местоположение" : "Joylashuvni ko'rish");
            button.setCallbackData("orderId/" + order.getId());
            inlieniKeybord.add(Collections.singletonList(button));
        }
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText(currentLanguage ? "Принятие груза" : "Yukni qabul qilish");
        inlineKeyboardButton1.setCallbackData("acceptOrder/" + order.getId());
        inlieniKeybord.add(Collections.singletonList(inlineKeyboardButton1));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(inlieniKeybord);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
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

    public SendMessage getMessage(Long orderId) {
        Order order = orderService.getById(orderId);
        SendMessage sendMessage = new SendMessage();
        String text = currentLanguage ? "Заказ ид:" + order.getId() + "\nИмя Клиента: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Телефон заказчика: " + order.getCustomer().getPhoneNumber()
                : "Buyurtma id:" + order.getId() + "\nBuyurtmachining ismi: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n Buyurtmachining telefon raqami: " + order.getCustomer().getPhoneNumber() + "\n";
        for (Messages message : messageService.getMessages(order.getId())) {
            if (message.getText() != null) {
                text = text + "\n" + message.getText();
            }
        }
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        if (order.getSellerPoint().getName() != null) {
            sendMessage.setText(text + "\n" + (currentLanguage ? "Адрес: " : "Manzil: ") + order.getSellerPoint().getName());
        }
        return sendMessage;
    }

    // TODO o'zgartir
    public SendMessage accepedOrder(Long orderId) {

        orderService.acceptedSupplierOrder(orderId, suplierService.getById(Long.valueOf(chatId)));
        SendMessage sendMessage = new SendMessage();
        KeyboardButton keyboardButton1 = new KeyboardButton(currentLanguage ? KeyWords.ACEPTED_ORDER_RUS : KeyWords.ACEPTED_ORDER_UZB);
        KeyboardButton keyboardButton2 = new KeyboardButton(currentLanguage ? KeyWords.DONT_ACCEPTED_ORDER_RUS : KeyWords.DONT_ACCEPTED_ORDER_UZB);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(keyboardButton1);
        keyboardRow.add(keyboardButton2);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(keyboardRow));
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(markup);
        sendMessage.setText(currentLanguage ? KeyWords.SELECT_MESSAGE_RUS : KeyWords.SELECT_MESSAGE_UZB);
        return sendMessage;
    }

    public SendMessage acceptedSupplierOrder(Message message) {
//        setCurrentLanguage(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        Order order = orderService.acceptedSupplier(message);
        if (order != null) {
            sendMessage.setText(currentLanguage ? "Ваш заказ был размещен. Пожалуйста, доставьте заказ быстро и в хорошем качестве." : "Buyurtma sizga berildi. Iltimos byurtmani tez va sifatli yetkazib bering.");
            sendMessageSeller(order);
        } else {
            orderService.dontAcceptedOrder(chatId);
            sendMessage.setText(this.currentLanguage ? "Заказ Вам не был отправлен. Будьте более активны" : "Buyurtma sizga yo'naltirilmadi. Ilimos faolroq bo'ling");
        }
        return sendMessage;
    }

    void sendMessageSeller(Order order) {
        Seller customer = order.getCustomer();
        Supplier supplier = order.getSupplier();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(customer.getId()));
        sendMessage.setText(KeyWords.userLanguage.get(customer.getId()) ? "ID заказа: " + order.getId() + "\nВаш заказ был получен. Поставщик свяжется с вами в ближайшее время."
                : "Buyurtma id : " + order.getId() + "\nBuyurtmangizni qabul qilib olindi. Tez orada yetkazib beruvchi siz bilan bog'lanadi.");
        sellerBot.sendMessage(sendMessage);
        sendMessage.setText(KeyWords.userLanguage.get(customer.getId()) ? "Имя курьера, который получил ваш заказ: " + supplier.getLastName() + " " + supplier.getFirstName() + "\nЕго номер телефона: " + supplier.getPhoneNumber()
                : "Buyrtmangizni qabul qilib olgan kuryerning ismi" + supplier.getLastName() + " " + supplier.getFirstName() + "\nUning telefon raqami: " + supplier.getPhoneNumber());
        sellerBot.sendMessage(sendMessage);
    }

    public SendMessage complateOrder(Long orderId) {
        Order order = orderService.complateOrder(orderId);
        Seller customer = order.getCustomer();
        Supplier supplier = order.getSupplier();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(customer.getId()));
        sendMessage.setText(KeyWords.userLanguage.get(customer.getId()) ? "Id: " + order.getId() + " Ваш заказ успешно выполнен." : "Id: " + order.getId() + " buyurtmangiz muvaffaqiyatli yakunlandi");
        sellerBot.sendMessage(sendMessage);

        sendMessage.setText(KeyWords.userLanguage.get(customer.getId()) ? "Имя курьера, который получил ваш заказ: " + supplier.getLastName() + " " + supplier.getFirstName() + "\nЕго номер телефона: " + supplier.getPhoneNumber()
                : "Buyrtmangizni qabul qilib olgan kuryerning ismi" + supplier.getLastName() + " " + supplier.getFirstName() + "\nUning telefon raqami: " + supplier.getPhoneNumber());
        sellerBot.sendMessage(sendMessage);

        sendMessage.setChatId(String.valueOf(supplier.getId()));
        sendMessage.setText(currentLanguage ? "Id: " + order.getId() + " Ваш заказ успешно выполнен." : "Id: " + order.getId() + " buyurtmangiz muvaffaqiyatli yakunlandi");
        return sendMessage;
    }

    public void editSupplierLocation(Message editedMessage) {
        Location location = new Location(editedMessage.getLocation(), editedMessage.getEditDate());
//        System.out.println("editedMessage.getLocation() = " + editedMessage.getLocation());
//        System.out.println("1.KeyWords.userLocation.get(editedMessage.getChatId()) = " + KeyWords.supplierLocation.get(editedMessage.getChatId()));
        KeyWords.supplierLocation.put(editedMessage.getChatId(), location);
//        System.out.println("2.KeyWords.userLocation.get(editedMessage.getChatId()) = " + KeyWords.supplierLocation.get(editedMessage.getChatId()));
    }
}
