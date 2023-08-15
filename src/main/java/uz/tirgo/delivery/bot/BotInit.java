package uz.tirgo.delivery.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.tirgo.delivery.bot.customer.config.SellerBot;
import uz.tirgo.delivery.bot.supplier.config.SupplierBot;

@Component
public class BotInit {
    @Autowired
    private SellerBot sellerBot;

    @Autowired
    private SupplierBot supplierBot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {


        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        TelegramBotsApi telegramBotsApi1 = new TelegramBotsApi(DefaultBotSession.class);

        try {
            telegramBotsApi1.registerBot(supplierBot);
            telegramBotsApi.registerBot(sellerBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }

    }
}
