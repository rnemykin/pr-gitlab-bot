package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.service.MessageStrategy;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegularMessageStrategy implements MessageStrategy {

    private final RegularMessageService regularMessageService;
    private final PullRequestMessageService pullRequestMessageService;

    @Override
    public void process(Message message) {
        save(message);
    }

    private void save(Message message) {
        RegularMessage msg = new RegularMessage();
        msg.setChatId(message.getChatId());
        msg.setMessageId(message.getMessageId());
        msg.setPullRequestId(extractPullRequestId(message));
        regularMessageService.save(msg);
    }

    private Integer extractPullRequestId(Message message) {
        String prUrl = Optional.ofNullable(message.getEntities()).map(a -> a.get(0).getUrl()).orElseThrow();
        return pullRequestMessageService.findByPullRequestUrl(prUrl).orElse(new PullRequestMessage()).getPullRequestId();
    }

    @Override
    public boolean isApplicable(long senderId) {
        return Objects.equals(senderId, MESSAGE_FROM_TELEGRAM_SENDER_ID);
    }
}
