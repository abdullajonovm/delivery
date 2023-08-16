package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Service
@RequiredArgsConstructor
public class CallbackQueryService {

    private final OrderService orderService;

    public SendMessage callBackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long orderId;
        if (data.startsWith("deletOrder/")) {
            orderId = Long.valueOf(data.replace("deletOrder/", ""));
            orderService.deletOrder(orderId);
            return new SendMessage(String.valueOf(callbackQuery.getFrom().getId()), "order deleted");
        }
        return null;
    }
}
