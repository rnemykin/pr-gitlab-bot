package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageRepository;

import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PullRequestMessageService {
    private final PullRequestMessageRepository repository;

    public PullRequestMessage createMessage(MergeRequest pr, Message msg) {
        PullRequestMessage entity = new PullRequestMessage();
        entity.setChatId(msg.getChatId());
        entity.setMessageId(msg.getMessageId());
        entity.setPullRequestId(pr.getId());
        entity.setPullRequestNumber(pr.getIid());
        entity.setStatus(PullRequestMessage.Status.NEW);
        return repository.save(entity);
    }

    public void archiveMessage(@NotEmpty UUID id) {
        PullRequestMessage entity = repository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("not found, id = " + id));

        entity.setStatus(PullRequestMessage.Status.DELETED);
        entity.setDeleteTime(LocalDateTime.now());
        repository.save(entity);
    }

    public Optional<PullRequestMessage> findByPrId(@NotNull Integer pullRequestId) {
        PullRequestMessage message = new PullRequestMessage();
        message.setPullRequestId(pullRequestId);
        return repository.findOne(Example.of(message));
    }
}
