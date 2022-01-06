package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.service.MessageStrategy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentMessageStrategy implements MessageStrategy {

    private final CommentMessageService commentMessageService;
    private final RegularMessageService regularMessageService;
    private final PullRequestMessageService pullRequestMessageService;

    @Override
    public void process(Message message) {
        if (message.getReplyToMessage() != null)
            save(message);
    }

    private void save(Message message) {
        CommentMessage comment = new CommentMessage();
        comment.setChatId(message.getChatId());
        comment.setMessageId(message.getMessageId());
        comment.setText(message.getText());
        comment.setAuthor(fillAuthor(message));
        comment.setIsProcessed(Boolean.FALSE);

        comment.setSource(CommentMessage.Source.TELEGRAM);
        comment.setCreatedAt(LocalDateTime.now());

        setPullRequestData(message, comment);
        commentMessageService.save(comment);
    }

    private String fillAuthor(Message message) {
        String author = message.getFrom().getUserName() == null ?
                String.join(" ", message.getFrom().getFirstName(), message.getFrom().getLastName()) :
                message.getFrom().getUserName();
        return author.isBlank() ? message.getFrom().getId().toString() : author;
    }

    private void setPullRequestData(Message message, CommentMessage comment) {
        Integer replyMessageId = Optional.ofNullable(message.getReplyToMessage()).map(Message::getMessageId).orElse(null);
        Integer pullRequestId = regularMessageService.findByMessageId(replyMessageId).map(RegularMessage::getPullRequestId).orElseThrow();

        PullRequestMessage pullRequestMessage = pullRequestMessageService.findByPullRequestId(pullRequestId).orElse(new PullRequestMessage());

        comment.setReplyMessageId(replyMessageId);
        comment.setPullRequestIid(pullRequestMessage.getPullRequestIid());
        comment.setProjectId(pullRequestMessage.getProjectId());
        comment.setPullRequestId(pullRequestId);
    }

    @Override
    public boolean isApplicable(long senderId) {
        return !Objects.equals(senderId, TELEGRAM_SENDER_ID);
    }
}
