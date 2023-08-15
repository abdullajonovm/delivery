package uz.tirgo.delivery.bot.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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

    public void start(Message message) {
        suplierService.saveUser(message);
    }

    public List<SendMessage> myFinishedOrders(String chatId, boolean currentLanguage) {
        List<Order> orders = orderService.getOrders(Long.valueOf(chatId), OrderStatus.COMPLETE);
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
        }
        return sendMessageList;
    }

    public List<Order> getInprogressOrders() {
        return orderService.getAllOrders(OrderStatus.IN_PROGRESS);
    }
}
