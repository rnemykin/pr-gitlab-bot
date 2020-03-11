package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rnemykin.gitlab.prtbot.model.PrMessage;
import ru.rnemykin.gitlab.prtbot.model.PrMessageRepository;

import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PrMessageService {
    private final PrMessageRepository repository;

    public PrMessage createMessage(@NotEmpty String prId, long messageId, long chatId) {
        PrMessage entity = new PrMessage();
        entity.setChatId(chatId);
        entity.setMessageId(messageId);
        entity.setPullRequestId(prId);
        entity.setStatus(PrMessage.Status.NEW);
        return repository.save(entity);
    }

    public void archiveMessage(@NotEmpty String id) {
        PrMessage entity = repository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("not found, id = " + id));

        entity.setStatus(PrMessage.Status.DELETED);
        entity.setDeleteTime(LocalDateTime.now());
        repository.save(entity);
    }
}
