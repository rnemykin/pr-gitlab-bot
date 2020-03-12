package ru.rnemykin.gitlab.prtbot.service.client.telegram;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rnemykin.gitlab.prtbot.config.properties.TelegramProperties;

import javax.annotation.PostConstruct;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramServiceClient {
    private static final String PR_MESSAGE_TEMPLATE = "[Pull request !{0}]({1})\n*{2}*: {3}\nOpened {4} by {5}";
    private static final DateTimeFormatter RU_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TelegramProperties properties;
    private TelegramLongPollingBot telegramApi;


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

    public Optional<Message> newPrNotification(MergeRequest pr) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(properties.getChatId());
            message.setText(MessageFormat.format(
                    PR_MESSAGE_TEMPLATE,
                    pr.getIid(),
                    pr.getWebUrl(),
                    pr.getTitle(),
                    pr.getDescription(),
                    RU_DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(pr.getCreatedAt().toInstant(), systemDefault())),
                    pr.getAuthor().getName()
            ));
            message.disableWebPagePreview();
            message.setParseMode(ParseMode.MARKDOWN);
            return Optional.of(telegramApi.execute(message));
        } catch (Exception ex) {
            log.error("can't sent message for pr {}", pr, ex);
            return Optional.empty();
        }
    }

    public Boolean deleteMessage(int messageId, long chatId) {
        DeleteMessage method = new DeleteMessage(chatId, messageId);
        Boolean result;
        try {
            result = telegramApi.execute(method);
        } catch (TelegramApiException ex) {
            log.error("can't delete message[id={}, chatId={}]", messageId, chatId, ex);
            result = false;
        }
        return Boolean.TRUE.equals(result);
    }

    // todo implement
    public void updatePrMessage() {

    }
}
