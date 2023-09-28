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
            sellerBot.botToken = "6417165435:AAG9mWfwbWMRPJ160BnsLxAzZlq2GbvHGp8";
            sellerBot.userName = "delivery_m_bot";
            supplierBot.botToken = "6529333260:AAGuB3DUDnNxeGTaveL1JZrvVyhkcIh3df4";
            supplierBot.userName = "delivery_supplier_bot";
/*
            sellerBot.botToken = "6362180263:AAG2onFzaKljU2rHYwJTIYKA1eK8Iz64HxI";
            sellerBot.userName = "delivery_customer_test_bot";
            supplierBot.botToken = "6606301905:AAGGT4_H40u52CtL13R2VDvzqSedRSIHo5o";
            supplierBot.userName = "delivery_supplier_test_bot";*/

            telegramBotsApi1.registerBot(supplierBot);
            telegramBotsApi.registerBot(sellerBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }

    }
}
