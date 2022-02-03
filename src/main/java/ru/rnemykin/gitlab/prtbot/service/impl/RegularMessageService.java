package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.model.repository.RegularMessageRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegularMessageService extends DeletableEntityService<RegularMessage, RegularMessageRepository> {

    private final PullRequestMessageService pullRequestMessageService;

    public void save(Message message) {
        RegularMessage msg = new RegularMessage();
        msg.setChatId(message.getChatId());
        msg.setMessageId(message.getMessageId());
        msg.setPullRequestId(extractPullRequestId(message));
        save(msg);
    }

    private Integer extractPullRequestId(Message message) {
        String prUrl = Optional.ofNullable(message.getEntities()).map(a -> a.get(0).getUrl()).orElseThrow();
        return pullRequestMessageService.findByPullRequestUrl(prUrl).orElse(new PullRequestMessage()).getPullRequestId();
    }

    public Optional<RegularMessage> findByPrId(@NotNull Integer pullRequestId) {
        return repository.findOne(RegularMessageRepository.Specifications.findByPullRequestId(pullRequestId));
    }
}
