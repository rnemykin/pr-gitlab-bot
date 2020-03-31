package ru.rnemykin.gitlab.prtbot.service.client.telegram;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Pipeline;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rnemykin.gitlab.prtbot.config.properties.TelegramProperties;
import ru.rnemykin.gitlab.prtbot.model.PullRequestUpdateMessage;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramServiceClient {
    private static final Pattern MARKUP_ESCAPE_PATTERN = Pattern.compile("[`*_]");
    private static final String UP_VOTERS_MESSAGE_TEMPLATE = "\n\n\uD83D\uDC4D - {0} by {1}";
    private static final String UNRESOLVED_THREADS_MESSAGE_TEMPLATE = "\n\n*Unresolved threads*\n{0}";
    private static final String PIPELINE_MESSAGE_TEMPLATE = "\n\n[Last pipeline]({0}) {1}";
    private static final String PR_MESSAGE_TEMPLATE = "[Pull request !{0}]({1})\n`{2}`  \uD83D\uDC49  `{3}`\n\n{4}\nOpened __{5}__ by {6}";
    private static final String UPDATE_TIME_TEMPLATE = "\n\nLast check: {0}";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
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
        SendMessage message = new SendMessage();
        message.setChatId(properties.getChatId());
        message.setText(makePrMessageText(pr));
        message.disableWebPagePreview();
        message.setParseMode(ParseMode.MARKDOWN);
        return Optional.ofNullable(executeMethod(message));
    }

    private String makePrMessageText(MergeRequest pr) {
        return MessageFormat.format(
                PR_MESSAGE_TEMPLATE,
                pr.getIid(), pr.getWebUrl(), pr.getSourceBranch(), pr.getTargetBranch(),
                escapeMarkupChars(pr.getTitle()), getPassDaysText(pr.getCreatedAt()), pr.getAuthor().getName()
        );
    }

    private String getPassDaysText(Date createdDate) {
        long daysCount = DAYS.between(LocalDate.ofInstant(createdDate.toInstant(), systemDefault()), LocalDate.now());
        return daysCount == 0 ? "today" : (daysCount == 1 ? "1 day" : daysCount + " days") + " ago \uD83D\uDE31";
    }

    public boolean deleteMessage(int messageId, long chatId) {
        Boolean result = executeMethod(new DeleteMessage(chatId, messageId));
        return Boolean.TRUE.equals(result);
    }

    private <T extends Serializable> T executeMethod(BotApiMethod<T> method) {
        try {
            return telegramApi.execute(method);
        } catch (TelegramApiException ex) {
            log.error("can't execute method {}", method, ex);
            return null;
        }
    }

    public void updatePrMessage(PullRequestUpdateMessage data) {
        EditMessageText editMsg = new EditMessageText();
        editMsg.setText(makeUpdatePrMessageText(data));
        editMsg.setChatId(data.getTelegramChatId());
        editMsg.setMessageId(data.getTelegramMessageId());
        editMsg.setParseMode(ParseMode.MARKDOWN);
        editMsg.disableWebPagePreview();
        executeMethod(editMsg);
    }

    private String makeUpdatePrMessageText(PullRequestUpdateMessage data) {
        String text = makePrMessageText(data.getRequest());

        if (data.getLastPipeline() != null) {
            text += MessageFormat.format(
                    PIPELINE_MESSAGE_TEMPLATE,
                    data.getLastPipeline().getWebUrl(),
                    getPipelineStatusIcon(data.getLastPipeline())
            );
        }

        if (!CollectionUtils.isEmpty(data.getUnresolvedThreadsMap())) {
            text += MessageFormat.format(
                    UNRESOLVED_THREADS_MESSAGE_TEMPLATE,
                    data.getUnresolvedThreadsMap().entrySet().stream()
                            .map(e -> "\t\t" + e.getKey() + " - " + e.getValue())
                            .collect(Collectors.joining("\n"))
            );
        }

        List<String> upVoterNames = data.getUpVoterNames();
        if(!CollectionUtils.isEmpty(upVoterNames)) {
            text += MessageFormat.format(UP_VOTERS_MESSAGE_TEMPLATE, upVoterNames.size(), String.join(", ", upVoterNames));
        }

        text += MessageFormat.format(UPDATE_TIME_TEMPLATE, DTF.format(LocalDateTime.now()));
        return text;
    }

    private String getPipelineStatusIcon(Pipeline pipeline) {
        switch (pipeline.getStatus()) {
            case FAILED:
                return "\uD83D\uDD34";

            case SUCCESS:
                return "\uD83D\uDFE2";

            case RUNNING:
                return "\uD83D\uDD35";
            case CANCELED:
                return "\uD83D\uDFE1";

            default:
                return "\uD83E\uDD37\u200D";
        }

    }

    private String escapeMarkupChars(@NotEmpty String source) {
        return MARKUP_ESCAPE_PATTERN.matcher(source).replaceAll(m -> "\\\\\\\\" + m.group());
    }
}
