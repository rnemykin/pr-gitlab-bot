package ru.rnemykin.gitlab.prtbot.service.client.telegram;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import ru.rnemykin.gitlab.prtbot.config.properties.TelegramProperties;

import javax.annotation.PostConstruct;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Component
@RequiredArgsConstructor
public class TelegramServiceClient {
    private final TelegramProperties properties;
    private LongPollingBot telegramApi;


    @SneakyThrows
    @PostConstruct
    private void initBotApi() {
        ApiContextInitializer.init();

        DefaultBotOptions options = ApiContext.getInstance(DefaultBotOptions.class);
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProxyUser(), properties.getProxyPassword().toCharArray());
            }
        });
        options.setProxyHost(properties.getProxyHost());
        options.setProxyPort(properties.getProxyPort());
        options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        telegramApi = new TelegramLongPollingBot(options) {
            @Override
            public void onUpdateReceived(Update update) {

            }

            @Override
            public String getBotUsername() {
                return properties.getBotName();
            }

            @Override
            public String getBotToken() {
                return properties.getToken();
            }
        };

        new TelegramBotsApi().registerBot(telegramApi);
    }
}
