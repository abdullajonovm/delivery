package uz.tirgo.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.tirgo.bot.payload.MyBot1;

@Component
public class BotInit {
    @Autowired
    private MyBot myBot;

    @Autowired
    private MyBot1 myBot1;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {


        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        TelegramBotsApi telegramBotsApi1 = new TelegramBotsApi(DefaultBotSession.class);

        try {
            telegramBotsApi1.registerBot(myBot1);
            telegramBotsApi.registerBot(myBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }

    }
}
