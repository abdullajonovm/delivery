package uz.tirgo.delivery.bot.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Messages;
import uz.tirgo.delivery.entity.Order;
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
        List<Order> orders = orderService.getOrders(Long.valueOf(chatId), status);
        List<SendMessage> sendMessageList = new ArrayList<>();
        if (orders.isEmpty() || orders == null)
            return Collections.singletonList(new SendMessage(chatId, currentLanguage ? "В настоящее время у вас нет доставленных заказов" : "Hozirda sizning yetkazib berilgan buyurtmalaringiz mavjud emas"));
        for (Order order : orders) {
            SendMessage sendMessage = new SendMessage();
            String text = currentLanguage ? "Заказ ид:" + order.getId() + "\nИмя Клиента: " + order.getCustomer().getPhoneNumber() + ",\n Телефон заказчика: " + order.getCustomer().getPhoneNumber()
                    : "Buyurtma id:" + order.getId() + "\nBuyurtmachining ismi: " + order.getCustomer().getPhoneNumber() + ",\n Buyurtmachining telefon raqami: " + order.getCustomer().getPhoneNumber() + "\n";
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
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(button)));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        return sendMessageList;
    }

    public SendLocation getSellerLocation(Long orderId) {
        Order order = orderService.getById(orderId);
        Location sellerPoint = order.getSellerPoint();
        SendLocation sendLocation = new SendLocation();
        sendLocation.setLongitude(sellerPoint.getLongitude());
        sendLocation.setLatitude(sellerPoint.getLatitude());
        sendLocation.setHorizontalAccuracy(sellerPoint.getHorizontalAccuracy());
        sendLocation.setChatId(chatId);
        return sendLocation;
    }

    public SendLocation getBuyerLocation(Long orderId) {
        Order order = orderService.getById(orderId);
        Location buyerPoint = order.getBuyerPoint();
        SendLocation sendLocation1 = new SendLocation();
        sendLocation1.setLongitude(buyerPoint.getLongitude());
        sendLocation1.setLatitude(buyerPoint.getLatitude());
        sendLocation1.setHorizontalAccuracy(buyerPoint.getHorizontalAccuracy());
        sendLocation1.setChatId(chatId);
        return sendLocation1;
    }
}
