package ru.rnemykin.gitlab.prtbot.service.client.telegram;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.References;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.config.properties.TelegramProperties;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestUpdateMessage;
import ru.rnemykin.gitlab.prtbot.service.impl.RegularMessageService;

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
    private static final String REFACTORING_LABEL = "refactoring";
    private static final Pattern MARKUP_ESCAPE_PATTERN = Pattern.compile("[`*_]");
    private static final String APPROVERS_MESSAGE_TEMPLATE = "\n\n\uD83D\uDC4D - {0} by {1}";
    private static final String UNRESOLVED_THREADS_MESSAGE_TEMPLATE = "\n\n*Unresolved threads*\n{0}";
    private static final String PIPELINE_MESSAGE_TEMPLATE = "\n\n[Last pipeline]({0}) {1}";
    private static final String PR_MESSAGE_TEMPLATE = "{8}[Pull request !{0}]({1}) `({9})`\n`{2}`  \uD83D\uDC49  `{3}` {7}\n\n{4}\nOpened __{5}__ by {6}";
    private static final String PR_COMMENT_TEMPLATE = "`{0}:`{1} \nat {2}";
    private static final String UPDATE_TIME_TEMPLATE = "\n\nLast check: {0}";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final long MESSAGE_FROM_TELEGRAM_SENDER_ID = 777000L;
    private final TelegramProperties properties;
    private final CheckPullRequestProperties checkPrProperties;
    private final RegularMessageService regularMessageService;
    private TelegramLongPollingBot bot;


    @SneakyThrows
    @PostConstruct
    private void initBotApi() {

        bot = new TelegramLongPollingBot(botOptions()) {
            @Override
            public void onUpdateReceived(Update update) {
                Message message = update.getMessage();
                if (isPrMessage(message)) {
                    regularMessageService.save(message);
                }
            }

            private boolean isPrMessage(Message message) {
                return message != null && message.getFrom().getId().equals(MESSAGE_FROM_TELEGRAM_SENDER_ID);
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

        new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
    }

    private DefaultBotOptions botOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        if (StringUtils.hasText(properties.getProxyHost())) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(properties.getProxyUser(), properties.getProxyPassword().toCharArray());
                }
            });
            options.setProxyHost(properties.getProxyHost());
            options.setProxyPort(properties.getProxyPort());
            options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }
        return options;
    }

    public Optional<Message> newPrNotification(MergeRequest pr, boolean silent) {
        SendMessage message = new SendMessage();
        message.setDisableNotification(silent);
        message.setChatId(String.valueOf(properties.getChatId()));
        message.setText(makePrMessageText(pr));
        message.disableWebPagePreview();
        message.setParseMode(ParseMode.MARKDOWN);
        return Optional.ofNullable(executeMethod(message));
    }

    public Optional<Message> newPrComment(CommentMessage comment) {
        SendMessage message = new SendMessage();
        message.setReplyToMessageId(comment.getReplyMessageId());
        message.setChatId(String.valueOf(comment.getChatId()));
        message.setText(makePrCommentText(comment));
        message.disableWebPagePreview();
        message.setParseMode(ParseMode.MARKDOWN);
        return Optional.ofNullable(executeMethod(message));
    }

    private String makePrMessageText(MergeRequest pr) {
        return MessageFormat.format(
                PR_MESSAGE_TEMPLATE,
                pr.getIid(),
                pr.getWebUrl(),
                pr.getSourceBranch(),
                pr.getTargetBranch(),
                escapeMarkupChars(pr.getTitle()),
                getPassDaysText(pr.getCreatedAt()),
                pr.getAuthor().getName(),
                getRefactoringFlag(pr.getLabels()),
                getAttentionTitle(pr.getAuthor().getId()),
                getProjectName(pr)
        );
    }

    private String makePrCommentText(CommentMessage comment) {
        return MessageFormat.format(
                PR_COMMENT_TEMPLATE,
                comment.getAuthor(),
                comment.getText(),
                comment.getCreatedAt().format(DTF));
    }

    private String getProjectName(MergeRequest pr) {
        return Optional.ofNullable(pr.getReferences())
                .map(References::getFull)
                .map(name -> name.replace(pr.getReferences().getRelative(), ""))
                .orElse("");
    }

    private String getRefactoringFlag(List<String> labels) {
        return labels != null && labels.stream().anyMatch(REFACTORING_LABEL::equalsIgnoreCase) ? " \u2699" : "";
    }

    private String getAttentionTitle(int authorId) {
        String result = "";
        if (checkPrProperties.isJuniorDeveloper(authorId)) result += "\u203C Made by Junior \u203C \n";
        if (checkPrProperties.isFreshMeat(authorId)) result += "\u26A0 Made by Fresh Meat \u26A0 \n";
        return result;
    }

    private String getPassDaysText(Date createdDate) {
        long daysCount = DAYS.between(LocalDate.ofInstant(createdDate.toInstant(), systemDefault()), LocalDate.now());
        return daysCount == 0 ? "today" : (daysCount == 1 ? "1 day" : daysCount + " days") + " ago \uD83D\uDE31";
    }

    public boolean deleteMessage(int messageId, Long chatId) {
        Boolean result = executeMethod(new DeleteMessage(chatId.toString(), messageId));
        return Boolean.TRUE.equals(result);
    }

    private <T extends Serializable> T executeMethod(BotApiMethod<T> method) {
        try {
            return bot.execute(method);
        } catch (TelegramApiException ex) {
            log.error("can't execute method {}", method, ex);
            return null;
        }
    }

    public void updatePrMessage(PullRequestUpdateMessage data) {
        EditMessageText editMsg = new EditMessageText();
        editMsg.setText(makeUpdatePrMessageText(data));
        editMsg.setChatId(String.valueOf(data.getTelegramChatId()));
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

        List<String> approverNames = data.getApproverNames();
        if (!CollectionUtils.isEmpty(approverNames)) {
            text += MessageFormat.format(APPROVERS_MESSAGE_TEMPLATE, approverNames.size(), String.join(", ", approverNames));
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
