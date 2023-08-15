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
//        if (callbackQuery.getData().startsWith(KeyWords.ACEPTED_ORDER)) {
//            String substring = callbackQuery.getData().substring(0, KeyWords.ACEPTED_ORDER.length());
//            Long orderId = Long.valueOf(substring);
//            orderService.existOrderById(orderId);
//        }
        return null;
    }
}
