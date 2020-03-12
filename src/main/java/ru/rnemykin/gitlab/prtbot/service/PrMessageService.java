package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import ru.rnemykin.gitlab.prtbot.model.PrMessageRepository;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;

import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PrMessageService {
    private final PrMessageRepository repository;

    public PullRequestMessage createMessage(@NotNull Integer prId, int messageId, long chatId) {
        PullRequestMessage entity = new PullRequestMessage();
        entity.setChatId(chatId);
        entity.setMessageId(messageId);
        entity.setPullRequestId(prId);
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
